package com.vanoma.api.order.packages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vanoma.api.order.businesshours.IBusinessHourService;
import com.vanoma.api.order.charges.Charge;
import com.vanoma.api.order.charges.ChargeRepository;
import com.vanoma.api.order.contacts.*;
import com.vanoma.api.order.events.*;
import com.vanoma.api.order.external.IAuthApiCaller;
import com.vanoma.api.order.external.ICommunicationApiCaller;
import com.vanoma.api.order.external.IDeliveryApiCaller;
import com.vanoma.api.order.orders.DeliveryOrder;
import com.vanoma.api.order.orders.OrderRepository;
import com.vanoma.api.order.orders.OrderStatus;
import com.vanoma.api.order.orders.OrderUtils;
import com.vanoma.api.order.utils.LanguageUtils;
import com.vanoma.api.utils.exceptions.ExceptionUtils;
import com.vanoma.api.utils.exceptions.InvalidParameterException;
import com.vanoma.api.utils.exceptions.ResourceNotFoundException;
import com.vanoma.api.utils.exceptions.UnauthorizedAccessException;
import com.vanoma.api.utils.httpwrapper.HttpResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vanoma.api.order.utils.AccessValidationUtils.isStaffOrService;


@Repository
public class PackageService implements IPackageService {

    @Autowired
    private PackageRepository packageRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ChargeRepository chargeRepository;
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private ContactAddressRepository contactAddressRepository;

    @Autowired
    private IContactAddressService contactAddressService;
    @Autowired
    private IBusinessHourService businessHourService;
    @Autowired
    private IPackageEventService packageEventService;

    @Autowired
    private ICommunicationApiCaller communicationApiCaller;
    @Autowired
    private IAuthApiCaller authApiCaller;
    @Autowired
    private IDeliveryApiCaller deliveryApiCaller;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private LanguageUtils languageUtils;

    @Override
    public Page<Package> getPackages(PackageFilter filter, Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            return this.packageRepository.findAll(filter.getSpec(), pageable);
        } else {
            // TODO: Remove this default sorting and change all apps to request packages with their own sorting provided.
            // If the caller did not provide sorting, apply our own. All apps do not sort packages except delivery-api.
            // So they fall in this category.
            boolean isPlaced = filter.getStatus() != null
                    && filter.getStatus().stream().anyMatch(status -> !this.isPackageUpdatable(PackageStatus.create(status)));

            Sort sort = isPlaced
                    ? Sort.by("deliveryOrder_placedAt").descending()
                    : Sort.by("createdAt").descending();

            Pageable pageable1 = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
            return this.packageRepository.findAll(filter.getSpec(), pageable1);
        }
    }

    @Override
    public Package createPackage(String deliveryOrderId, PackageJson packageJson) {
        return createPackage(this.orderRepository.getById(deliveryOrderId), packageJson, PackageStatus.STARTED);
    }

    @Override
    public Package createPackage(DeliveryOrder order, PackageJson json, PackageStatus status) {
        json.validate();
        Package pkg = getPackageFromJson(order, json, status);
        this.businessHourService.validateBusinessHours(pkg, order.getCustomerId());

        if (!OrderUtils.isOrderUpdatable(order.getStatus())) {
            throw new UnauthorizedAccessException("crud.deliveryOrder.notUpdatable");
        }

        try {
            pkg = this.packageRepository.save(pkg);
            this.createContactAddresses(pkg, order.getCustomerId());
            return pkg;
        } catch (JpaObjectRetrievalFailureException ex) {
            String entityName = ExceptionUtils.getEntityNameFromEntityNotFoundException(ex.getMessage());
            throw new ResourceNotFoundException("crud." + entityName.toLowerCase() + ".notFound");
        }
    }

    private Package getPackageFromJson(DeliveryOrder order, PackageJson packageJson, PackageStatus status) {
        Package orderPackage = new Package(order)
                .setSize(packageJson.getSize())
                .setStatus(status)
                .setPriority(packageJson.getPriority())
                .setFragileContent(packageJson.getFragileContent())
                .setEventCallback(packageJson.getEventCallback())
                .setPickUpStart(packageJson.getPickUpStart());
        try {
            addPickUpStop(order, orderPackage, packageJson);
            addDropOffStop(order, orderPackage, packageJson);
        } catch (JpaObjectRetrievalFailureException ex) {
            String entityName = ExceptionUtils.getEntityNameFromEntityNotFoundException(ex.getMessage());
            throw new ResourceNotFoundException("crud." + entityName.toLowerCase() + ".notFound");
        }

        return orderPackage;
    }

    private void addPickUpStop(DeliveryOrder order, Package orderPackage, PackageJson packageJson) {
        if (packageJson.hasPickUp()) {
            Contact contact = this.getContactFromContactJson(order.getCustomerId(), packageJson.getFromContact());
            orderPackage.setFromContact(contact);

            Address address = this.getAddressFromAddressJson(order.getCustomerId(), packageJson.getFromAddress());
            orderPackage.setFromAddress(address);
            orderPackage.setFromNote(packageJson.getFromNote());
        }
    }

    private Contact getContactFromContactJson(String customerId, ContactJson contactJson) {
        if (StringUtils.isNotEmpty(contactJson.getContactId())) {
            return this.contactRepository.getById(contactJson.getContactId());
        }
        Contact existingContact = this.contactRepository
                .findFirstByCustomerIdAndPhoneNumberOneAndIsSaved(
                        customerId, contactJson.getPhoneNumberOne(), true);
        if (existingContact != null) return existingContact;
        return Contact.create(customerId, contactJson);
    }

    private Address getAddressFromAddressJson(String customerId, AddressJson addressJson) {
        if (StringUtils.isEmpty(addressJson.getAddressId())) {
            return this.contactAddressService.buildAddress(customerId, addressJson);
        }
        return this.contactAddressService.getAddressById(addressJson.getAddressId());
    }

    private void addDropOffStop(DeliveryOrder order, Package orderPackage, PackageJson packageJson) {
        if (packageJson.hasDropOffContact()) {
            Contact contact = this.getContactFromContactJson(order.getCustomerId(), packageJson.getToContact());
            orderPackage.setToContact(contact);
        }
        if (packageJson.hasDropOffAddress()) {
            Address address = this.getAddressFromAddressJson(order.getCustomerId(), packageJson.getToAddress());
            orderPackage.setToAddress(address);
            orderPackage.setToNote(packageJson.getToNote());
        }
    }

    private void createContactAddresses(Package pkg, String customerId) {
        if (pkg.getFromContact() != null && pkg.getFromAddress() != null) {
            ContactAddress contactAddress = this.contactAddressService
                    .getFirstContactAddressByContactAndAddress(pkg.getFromContact(), pkg.getFromAddress());
            if (contactAddress == null) {
                contactAddress = new ContactAddress(customerId, pkg.getFromContact(), pkg.getFromAddress());
                this.contactAddressRepository.save(contactAddress);
            }
        }
        if (pkg.getToContact() != null && pkg.getToAddress() != null) {
            ContactAddress contactAddress = this.contactAddressService
                    .getFirstContactAddressByContactAndAddress(pkg.getToContact(), pkg.getToAddress());
            if (contactAddress == null) {
                contactAddress = new ContactAddress(customerId, pkg.getToContact(), pkg.getToAddress());
                this.contactAddressRepository.save(contactAddress);
            }
        }
    }

    @Override
    public Package updatePackage(String packageId, PackageJson packageJson, String authHeader) {
        Package pkg = this.packageRepository.getById(packageId);
        this.updatePackage(pkg, packageJson, authHeader);

        if (this.isPackageUpdatable(pkg.getStatus())) {
            this.businessHourService.validateBusinessHours(pkg, pkg.getDeliveryOrder().getCustomerId());
            this.validateDifferentAddresses(pkg);
        }

        return this.saveWithTryCatch(pkg);
    }

    @Override
    public Package getPackageByTrackingNumber(String trackingNumber) {
        Package pkg = this.packageRepository.getByTrackingNumber(trackingNumber);
        if (pkg == null) {
            throw new InvalidParameterException("crud.package.notFound");
        }

        return pkg;
    }

    @Override
    public void deletePackage(String packageId) {
        Package pkg = this.packageRepository.getById(packageId);
        if (!this.isPackageUpdatable(pkg.getStatus())) {
            throw new UnauthorizedAccessException("crud.package.notDeletable");
        }

        // Manually delete charges first since spring boot does not support database-level CASCADE ON DELETE
        this.chargeRepository.deleteByPkg(pkg);
        this.packageRepository.delete(pkg);
    }

    @Override
    public void cancelPackage(String packageId, CancelPackageJson json) {
        json.validate();

        Package pkg = this.packageRepository.getById(packageId);
        if (!isStatusTransitional(pkg.getStatus())) {
            throw new InvalidParameterException("crud.package.cancellation.notAllowed");
        }

        cancelPackage(pkg, json.getNote());
    }

    @Override
    public void cancelPackage(Package pkg, String reason) {
        DeliveryOrder deliveryOrder = pkg.getDeliveryOrder();
        if (pkg.getStatus() == PackageStatus.PLACED) {
            if (pkg.getAssignmentId() != null) {
                HttpResult httpResult = this.deliveryApiCaller.cancelAssignment(pkg.getAssignmentId());
                if (!httpResult.isSuccess()) {
                    throw new RuntimeException(languageUtils.getLocalizedMessage("crud.package.cancellation.driverApiError"));
                }
            }

            pkg.setStatus(PackageStatus.CANCELED);
            pkg.setCancellationNote(reason);
            this.packageRepository.save(pkg);

            this.cancelDeliveryOrder(deliveryOrder);
            this.packageEventService.createPackageEvent(pkg, EventName.PACKAGE_CANCELLED, null);
        } else {
            pkg.setStatus(PackageStatus.INCOMPLETE);
            pkg.setCancellationNote(reason);
            this.packageRepository.save(pkg);

            this.incompleteDeliveryOrder(deliveryOrder);
        }
    }


    @Override
    public Package duplicatePackage(Package oldPackage, DeliveryOrder newOrder, OffsetDateTime pickUpStart) {
        Package newPkg = new Package(newOrder)
                .setSize(oldPackage.getSize())
                .setStatus(PackageStatus.STARTED)
                .setPriority(oldPackage.getPriority())
                .setFragileContent(oldPackage.getFragileContent())
                .setEventCallback(oldPackage.getEventCallback())
                .setPickUpStart(pickUpStart)
                .setFromNote(oldPackage.getFromNote())
                .setToNote(oldPackage.getToNote())
                .setFromContact(this.contactAddressService.getContactById(oldPackage.getFromContact().getParentContactId()))
                .setFromAddress(this.contactAddressService.getAddressById(oldPackage.getFromAddress().getParentAddressId()))
                .setToContact(this.contactAddressService.getContactById(oldPackage.getToContact().getParentContactId()))
                .setToAddress(this.contactAddressService.getAddressById(oldPackage.getToAddress().getParentAddressId()));
        this.packageRepository.save(newPkg);

        Set<Charge> charges = oldPackage.getCharges()
                .stream().filter(Charge::isDeliveryFee)
                .map(c -> c.duplicate(newPkg))
                .collect(Collectors.toSet());
        this.chargeRepository.saveAll(charges);
        newPkg.setCharges(charges); // Necessary for JPA queries.
        return this.packageRepository.save(newPkg);
    }

    private Package saveWithTryCatch(Package p) {
        try {
            return this.packageRepository.save(p);
        } catch (JpaObjectRetrievalFailureException ex) {
            String entityName = ExceptionUtils.getEntityNameFromEntityNotFoundException(ex.getMessage());
            throw new ResourceNotFoundException("crud." + entityName.toLowerCase() + ".notFound");
        }
    }

    private void validateDifferentAddresses(Package updatedPackaged) {
        Address from = updatedPackaged.getFromAddress();
        Address to = updatedPackaged.getToAddress();
        if (from != null && to != null && from.getAddressId().equals(to.getAddressId())) {
            throw new InvalidParameterException("crud.package.similarAddresses");
        }
    }

    private void updatePackage(Package pkg, PackageJson packageJson, String authHeader) {
        if (!this.isPackageUpdatable(pkg.getStatus()) && !isStaffOrService(authHeader)) {
            throw new UnauthorizedAccessException("crud.deliveryOrder.notUpdatable");
        }

        // Update public fields
        if (this.isPackageUpdatable(pkg.getStatus()) || isStaffOrService(authHeader)) {
            if (packageJson.getSize() != null) {
                pkg.setSize(packageJson.getSize());
            }
            if (packageJson.getPickUpStart() != null) {
                pkg.setPickUpStart(packageJson.getPickUpStart());
            }
            if (packageJson.getPriority() != null) {
                pkg.setPriority(packageJson.getPriority());
            }
            if (packageJson.getFromContact() != null) {
                pkg.setFromContact(this.getContactFromContactJson(pkg.getDeliveryOrder().getCustomerId(), packageJson.getFromContact()));
            }
            if (packageJson.getToContact() != null) {
                pkg.setToContact(this.getContactFromContactJson(pkg.getDeliveryOrder().getCustomerId(), packageJson.getToContact()));
            }
            if (packageJson.getFromAddress() != null) {
                pkg.setFromAddress(this.getAddressFromAddressJson(pkg.getDeliveryOrder().getCustomerId(), packageJson.getFromAddress()));
            }
            if (packageJson.getToAddress() != null) {
                pkg.setToAddress(this.getAddressFromAddressJson(pkg.getDeliveryOrder().getCustomerId(), packageJson.getToAddress()));
            }
            if (packageJson.getFromNote() != null) {
                pkg.setFromNote(packageJson.getFromNote());
            }
            if (packageJson.getToNote() != null) {
                pkg.setToNote(packageJson.getToNote());
            }
        }

        // Update restricted fields
        if (isStaffOrService(authHeader)) {
            if (packageJson.getStatus() != null) {
                pkg.setStatus(packageJson.getStatus());
            }
            if (!Objects.equals(packageJson.getDriverId(), PackageJson.NO_VALUE)) {
                pkg.setDriverId(packageJson.getDriverId());
            }
            if (!Objects.equals(packageJson.getAssignmentId(), PackageJson.NO_VALUE)) {
                pkg.setAssignmentId(packageJson.getAssignmentId());
            }
            if (packageJson.getStaffNote() != null) {
                pkg.setStaffNote(packageJson.getStaffNote());
            }
            if (packageJson.getIsAssignable() != null) {
                pkg.setIsAssignable(packageJson.getIsAssignable());
            }
            if (packageJson.getPickUpChangeNote() != null) {
                pkg.setPickUpChangeNote(packageJson.getPickUpChangeNote());
            }
            if (packageJson.getEnableNotifications() != null) {
                pkg.setEnableNotifications(packageJson.getEnableNotifications());
            }
        }
    }

    private boolean isPackageUpdatable(PackageStatus packageStatus) {
        return packageStatus == PackageStatus.STARTED
                || packageStatus == PackageStatus.REQUEST
                || packageStatus == PackageStatus.PENDING;
    }

    public boolean isStatusTransitional(PackageStatus status) {
        return status == PackageStatus.STARTED
                || status == PackageStatus.REQUEST
                || status == PackageStatus.PENDING
                || status == PackageStatus.PLACED;
    }

    public void cancelDeliveryOrder(DeliveryOrder deliveryOrder) {
        List<Package> packages = this.packageRepository.findByDeliveryOrder(deliveryOrder);

        // The sibling of the package we're cancelling must also be in placed status as well. So we just make
        // sure all of them have been cancelled before marking the order as cancelled. If not, we avoid marking
        // the order. In such case, either sibling package will be delivered (in which case the order will be
        // marked as complete in PackageEventService) or they will be cancelled (in which case this method will
        // be called on each package and eventually cancel the order as well).
        if (packages.stream().allMatch(p -> p.getStatus() == PackageStatus.CANCELED || p.getStatus() == PackageStatus.INCOMPLETE)) {
            deliveryOrder.setStatus(OrderStatus.CANCELED);
            this.orderRepository.save(deliveryOrder);
        }
    }

    public void incompleteDeliveryOrder(DeliveryOrder deliveryOrder) {
        List<Package> packages = this.packageRepository.findByDeliveryOrder(deliveryOrder);

        // Packages in this order are in any other non-final status besides placed (i.e. STARTED, REQUEST or PENDING).
        // So unless all packages have been marked as incomplete, we can't mark the order either. We just have to wait
        // either to cancel all such packages (in which case this method will be called for each package and
        // successively mark each package as incomplete as well as the order eventually) or the order will be placed
        // (in which case cancellation will be governed by the logic of cancelling a placed package/order).
        if (packages.stream().allMatch(p -> p.getStatus() == PackageStatus.INCOMPLETE)) {
            deliveryOrder.setStatus(OrderStatus.INCOMPLETE);
            this.orderRepository.save(deliveryOrder);
        }
    }
}
