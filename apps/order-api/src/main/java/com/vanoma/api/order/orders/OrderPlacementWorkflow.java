package com.vanoma.api.order.orders;

import com.vanoma.api.order.businesshours.IBusinessHourService;
import com.vanoma.api.order.charges.Charge;
import com.vanoma.api.order.charges.ChargeRepository;
import com.vanoma.api.order.charges.ChargeType;
import com.vanoma.api.order.contacts.ContactAddress;
import com.vanoma.api.order.contacts.IContactAddressService;
import com.vanoma.api.order.events.*;
import com.vanoma.api.order.packages.*;
import com.vanoma.api.order.packages.Package;
import com.vanoma.api.order.pricing.CustomPricingRepository;
import com.vanoma.api.utils.input.TimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderPlacementWorkflow {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private IBusinessHourService businessHourService;
    @Autowired
    private IContactAddressService contactAddressService;
    @Autowired
    private IPackageEventService packageEventService;

    @Autowired
    private PackageRepository packageRepository;
    @Autowired
    private ChargeRepository chargeRepository;

    @Autowired
    private IPackageService packageService;
    @Autowired
    private CurrentTimeWrapper currentTimeWrapper;
    @Autowired
    private CustomPricingRepository customPricingRepository;


    /* Public methods section for easier code reading. */

    public DeliveryOrder placeDeliveryOrder(DeliveryOrder order) {
        List<Package> packages = this.packageRepository.findByDeliveryOrder(order);
        List<Charge> charges = this.chargeRepository.findByDeliveryOrderAndType(order, ChargeType.DELIVERY_FEE);
        PackageUtils.validateDeliveryOrderPackages(packages, charges);

        this.setDeliveryTime(packages);
        this.businessHourService.validateBusinessHours(packages, order.getCustomerId());
        // Saving notes should happen before creating copies of contacts/addresses
        this.saveDeliveryNotesOnContactAddresses(packages);
        this.createCopiesOfContacts(packages);
        this.createCopiesOfAddresses(packages);
        this.savePackageChanges(packages);
        this.orderRepository.save(order);

        this.setPackageStatusToPlaced(packages);
        this.createOrderPlacedEvents(packages);
        return this.setOrderStatusToPlaced(order);
    }


    /* Private methods section for easier code reading. */

    private void setDeliveryTime(List<Package> packages) {
        for (Package pkg : packages) {
            OffsetDateTime pickUpStart = PackageUtils.getPickUpStart(this.currentTimeWrapper, pkg.getPickUpStart());
            pkg.setPickUpStart(pickUpStart);
        }
    }

    private void saveDeliveryNotesOnContactAddresses(List<Package> packages) {
        List<ContactAddress> updatedContactAddresses = new ArrayList<>();
        for (Package p : packages) {
            updatedContactAddresses.add(getUpdatedPickUpContactAddress(p));
            updatedContactAddresses.add(getUpdatedDropOffContactAddress(p));
        }
        this.contactAddressService.saveContactAddressAll(updatedContactAddresses);
    }

    private ContactAddress getUpdatedPickUpContactAddress(Package p) {
        ContactAddress fromContactAddress = this.contactAddressService
                .getFirstContactAddressByContactAndAddress(
                        p.getFromContact(), p.getFromAddress()
                );
        fromContactAddress.setLastNote(p.getFromNote());
        return fromContactAddress;
    }

    private ContactAddress getUpdatedDropOffContactAddress(Package p) {
        ContactAddress toContactAddress = this.contactAddressService
                .getFirstContactAddressByContactAndAddress(
                        p.getToContact(), p.getToAddress()
                );
        toContactAddress.setLastNote(p.getToNote());
        return toContactAddress;
    }

    private void createCopiesOfContacts(List<Package> packages) {
        for (Package p : packages) {
            p.setFromContact(p.getFromContact().buildUnsavedCopy());
            p.setToContact(p.getToContact().buildUnsavedCopy());
        }
    }

    private void createCopiesOfAddresses(List<Package> packages) {
        for (Package p : packages) {
            p.setFromAddress(p.getFromAddress().buildCopy(false));
            p.setToAddress(p.getToAddress().buildCopy(false));
        }
    }

    private void savePackageChanges(List<Package> packages) {
        packages.forEach(p -> this.packageRepository.save(p));
    }

    private DeliveryOrder setOrderStatusToPlaced(DeliveryOrder order) {
        order.setStatus(OrderStatus.PLACED);
        order.setPlacedAt(TimeUtils.getUtcNow());
        return this.orderRepository.save(order);
    }

    private void setPackageStatusToPlaced(List<Package> packages) {
        packages.forEach(pkg -> pkg.setStatus(PackageStatus.PLACED));
        this.packageRepository.saveAll(packages);
    }

    private void createOrderPlacedEvents(List<Package> packages) {
        for (Package pkg : packages) {
            this.packageEventService.createPackageEvent(pkg, EventName.ORDER_PLACED, null);
        }
    }
}
