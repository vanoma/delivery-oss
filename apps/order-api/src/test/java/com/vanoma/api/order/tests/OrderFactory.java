package com.vanoma.api.order.tests;

import com.vanoma.api.order.businesshours.BusinessHour;
import com.vanoma.api.order.businesshours.BusinessHourRepository;
import com.vanoma.api.order.charges.*;
import com.vanoma.api.order.contacts.*;
import com.vanoma.api.order.customers.*;
import com.vanoma.api.order.events.EventDescription;
import com.vanoma.api.order.events.EventName;
import com.vanoma.api.order.events.PackageEvent;
import com.vanoma.api.order.events.PackageEventRepository;
import com.vanoma.api.order.maps.KigaliDistrict;
import com.vanoma.api.order.orders.*;
import com.vanoma.api.order.packages.Package;
import com.vanoma.api.order.packages.*;
import com.vanoma.api.order.payment.PaymentRequest;
import com.vanoma.api.order.payment.PaymentRequestRepository;
import com.vanoma.api.order.pricing.CustomPricingRepository;
import com.vanoma.api.utils.input.TimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Optional;
import java.util.Set;

/**
 * A class to create order objects to use in tests. It bypasses controllers and services to work
 * directly with the order repository to allow us to create orders in the desired & testable state.
 * <p>
 * Each object (i.e. order, package, packageEvent, etc) is created individually. This provides greater
 * flexibility than for example creating an order with packages, which would make it really hard to customize
 * attributes of the package. However, by creating an order (without package) then create a package (passing
 * the order as a parameter), we can set package attributes to the desired values.
 * <p>
 *
 * <b>Note</b>: Because of the way objects are created, test classes which use this factory should avoid using
 * {@link org.springframework.transaction.annotation.Transactional} annotation. Doing so, for example, would
 * mean that creating packages through {@link OrderRepository} will not make them available when querying
 * {@link PackageRepository} because queries are never persisted to the database.
 */
@Service
public class OrderFactory { // TODO: Rename this class to a generic name as it creates other objects besides an order.
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PackageRepository packageRepository;
    @Autowired
    private PackageEventRepository packageEventRepository;
    @Autowired
    private ChargeRepository chargeRepository;
    @Autowired
    private ContactAddressRepository contactAddressRepository;
    @Autowired
    private PaymentRequestRepository paymentRequestRepository;
    @Autowired
    private CustomPricingRepository customPricingRepository;
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private BusinessHourRepository businessHourRepository;
    @Autowired
    private DiscountRepository discountRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private BranchRepository branchRepository;
    @Autowired
    private AgentRepository agentRepository;

    public Customer createCustomer() {
        return createCustomer(ObjectFactory.getRandomString(), ObjectFactory.getRandomPhoneNumber());
    }

    public Customer createCustomer(String businessName, String phoneNumber) {
        Customer customer = new Customer()
                .setBusinessName(businessName)
                .setPhoneNumber(phoneNumber);
        return this.customerRepository.save(customer);
    }

    public Customer createCustomer(OffsetDateTime postpaidExpiry) {
        return this.customerRepository.save(createCustomer().setPostpaidExpiry(postpaidExpiry));
    }

    public Customer createCustomer(BigDecimal weightingFactor) {
        return this.customerRepository.save(createCustomer().setWeightingFactor(weightingFactor));
    }

    public Customer createCustomer(BigDecimal fixedPriceAmount, OffsetDateTime fixedPriceExpiry) {
        return this.customerRepository.save(createCustomer()
                .setFixedPriceAmount(fixedPriceAmount)
                .setFixedPriceExpiry(fixedPriceExpiry));
    }

    public Branch createBranch() {
        return createBranch(createCustomer());
    }

    public Branch createBranch(Customer customer) {
        return createBranch(customer, false);
    }

    public Branch createBranch(Customer customer, boolean isDeleted) {
        Branch branch = new Branch(customer)
                .setBranchName(ObjectFactory.getRandomString())
                .setIsDeleted(isDeleted)
                .setContact(createContact(customer.getCustomerId()))
                .setAddress(createAddress(customer.getCustomerId()));
        return this.branchRepository.save(branch);
    }

    public Agent createAgent() {
        return createAgent(createCustomer());
    }

    public Agent createAgent(Branch branch) {
        Agent agent = createAgent(branch.getCustomer());
        return this.agentRepository.save(agent.setBranch(branch));
    }

    public Agent createAgent(Customer customer) {
        return createAgent(customer, false);
    }

    public Agent createAgent(Customer customer, boolean isDeleted) {
        Agent agent = new Agent(customer)
                .setFullName(ObjectFactory.getRandomString())
                .setPhoneNumber(ObjectFactory.getRandomPhoneNumber())
                .setIsDeleted(isDeleted);
        return this.agentRepository.save(agent);
    }

    public DeliveryOrder createOrder() {
        return createOrder(createCustomer());
    }

    public DeliveryOrder createOrder(Customer customer) {
        return createOrder(customer, OrderStatus.STARTED);
    }

    public DeliveryOrder createOrder(OrderStatus orderStatus) {
        return createOrder(createCustomer(), orderStatus);
    }

    public DeliveryOrder createOrder(Customer customer, OrderStatus orderStatus) {
        return createOrder(customer, orderStatus, getPlacedAt(orderStatus));
    }

    public DeliveryOrder createOrder(Agent agent, OrderStatus orderStatus) {
        DeliveryOrder order = new DeliveryOrder(agent)
                .setStatus(orderStatus)
                .setClientType(ClientType.WEB_APP);
        return this.orderRepository.save(order);
    }

    public DeliveryOrder createOrder(Customer customer, OrderStatus orderStatus, OffsetDateTime placedAt) {
        DeliveryOrder order = new DeliveryOrder(customer)
                .setStatus(orderStatus)
                .setPlacedAt(placedAt)
                .setClientType(ClientType.WEB_APP);
        return this.orderRepository.save(order);
    }

    public DeliveryOrder createOrderWithPackage(Customer customer, PackageSize packageSize) {
        return createOrderWithPackage(customer, OrderStatus.STARTED, PackageSize.SMALL);
    }

    public DeliveryOrder createOrderWithPackage(OrderStatus orderStatus) {
        return createOrderWithPackage(createCustomer(), orderStatus, PackageSize.SMALL);
    }

    public DeliveryOrder createOrderWithPackage(Customer customer, OrderStatus orderStatus, PackageSize packageSize) {
        DeliveryOrder order = createOrder(customer, orderStatus);
        createPackage(order, packageSize);
        // Instead of returning the order instance above, return one from the repository instead so that packages
        // can be automatically populated. The instance above does not have them, even if the relationship declares
        // an eager fetch type! An interesting case of spring where, for some weird reason, they don't make a database
        // call to fetch related objects on an instance that was already created with such objects.
        return this.orderRepository.getById(order.getDeliveryOrderId());
    }

    public Package createPackage() {
        return createPackage(createCustomer());
    }

    public Package createPackage(Customer customer) {
        return createPackage(customer, PackageStatus.STARTED);
    }

    public Package createPackage(DeliveryOrder order, PackageSize packageSize) {
        return createPackage(order, resolvePackageStatus(order.getStatus()), packageSize);
    }

    public Package createPackage(PackageStatus status) {
        return createPackage(createCustomer(), status);
    }

    public Package createPackage(PackageStatus status, OffsetDateTime pickUpStart) {
        Package pkg = createPackage(createCustomer(), status).setPickUpStart(pickUpStart);
        return this.packageRepository.save(pkg);
    }

    public Package createPackage(DeliveryOrder order, PackageStatus status, PackageSize packageSize) {
        Package pkg = new Package(order)
                .setSize(packageSize)
                .setPriority(PackagePriority.NORMAL)
                .setStatus(status)
                .setFromContact(createContact(order.getCustomerId()))
                .setToContact(createContact(order.getCustomerId()))
                .setFromAddress(createAddress(order.getCustomerId()))
                .setToAddress(createAddress(order.getCustomerId()));
        pkg = this.packageRepository.save(pkg);

        ContactAddress from = new ContactAddress()
                .setCustomerId(order.getCustomerId())
                .setContact(pkg.getFromContact())
                .setAddress(pkg.getFromAddress());
        this.contactAddressRepository.save(from);

        ContactAddress to = new ContactAddress()
                .setCustomerId(order.getCustomerId())
                .setContact(pkg.getToContact())
                .setAddress(pkg.getToAddress());
        this.contactAddressRepository.save(to);

        return pkg;
    }

    public Package createPackage(DeliveryOrder order, OffsetDateTime pickUpStart) {
        Package pkg = createPackage(order, PackageSize.SMALL).setPickUpStart(pickUpStart);
        return this.packageRepository.save(pkg);
    }

    public Package createPackage(Customer customer, String eventCallback) {
        Package pkg = createPackage(customer).setEventCallback(eventCallback);
        return this.packageRepository.save(pkg);
    }

    public Package createPackage(Customer customer, PackageStatus status) {
        DeliveryOrder order = createOrder(customer, resolveOrderStatus(status));
        return createPackage(order, status, PackageSize.SMALL);
    }

    public PackageEvent createPackageEvent(Package pkg, EventName eventName) {
        return createPackageEvent(pkg, eventName, null);
    }

    public PackageEvent createPackageEvent(Package pkg, EventName eventName, String assignmentId) {
        PackageEvent packageEvent = new PackageEvent(pkg)
                .setEventName(eventName)
                .setAssignmentId(assignmentId)
                .setTextRW(EventDescription.getTemplateRW(eventName))
                .setTextEN(EventDescription.getTemplateEN(eventName))
                .setTextFR(EventDescription.getTemplateFR(eventName));

        return this.packageEventRepository.save(packageEvent);
    }

    public Charge createCharge(Package pkg, ChargeStatus chargeStatus) {
        return createCharge(pkg, ChargeType.DELIVERY_FEE, chargeStatus);
    }

    public Charge createCharge(Package pkg, ChargeType chargeType, ChargeStatus chargeStatus) {
        return createCharge(pkg, chargeType, chargeStatus, new BigDecimal("1000"));
    }

    public Charge createCharge(Package pkg, ChargeType chargeType, ChargeStatus chargeStatus, BigDecimal txAmount) {
        BigDecimal transactionAmount = chargeType == ChargeType.DELIVERY_FEE ? txAmount : ChargeUtils.computeTransactionAmountGivenTotalAmount(txAmount);
        BigDecimal actualTransactionAmount = chargeType == ChargeType.DELIVERY_FEE ? txAmount : null;

        Charge charge = new Charge(pkg)
                .setType(chargeType)
                .setTransactionAmount(transactionAmount)
                .setActualTransactionAmount(actualTransactionAmount)
                .setStatus(chargeStatus);
        return this.chargeRepository.save(charge);
    }

    public PaymentRequest createPaymentRequest(Set<Charge> charges, Set<Discount> discounts) {
        PaymentRequest paymentRequest = new PaymentRequest(charges, discounts);
        return this.paymentRequestRepository.save(paymentRequest);
    }
//
//    public CustomPricing createCustomPricing(String customerId, BigDecimal price) {
//        CustomPricing customPricing = new CustomPricing(customerId)
//                .setPrice(price)
//                .setCustomerName("Dummy name")
//                .setExpireAt(TimeUtils.getUtcNow().plusDays(1));
//        return this.customPricingRepository.save(customPricing);
//    }

    public Contact createContact(String customerId) {
        Contact contact = new Contact(customerId)
                .setPhoneNumberOne(ObjectFactory.getRandomPhoneNumber())
                .setPhoneNumberTwo(ObjectFactory.getRandomPhoneNumber());
        return this.contactRepository.save(contact);
    }

    public Address createAddress(String customerId) {
        Address address = new Address(customerId)
                .setCoordinates(1.23, 30.12)
                .setDistrict(KigaliDistrict.GASABO);
        return this.addressRepository.save((address));
    }

    public BusinessHour createBusinessHour(int weekDay, OffsetTime openAt, OffsetTime closeAt) {
        BusinessHour businessHour = new BusinessHour()
                .setWeekDay(weekDay)
                .setOpenAt(openAt)
                .setCloseAt(closeAt);
        return this.businessHourRepository.save(businessHour);
    }

    public Discount createDiscount(DeliveryOrder order, DiscountType type) {
        return createDiscount(order, type, DiscountStatus.PENDING);
    }

    public Discount createDiscount(DeliveryOrder order, DiscountType type, DiscountStatus status) {
        return createDiscount(order, type, status, new BigDecimal("100"));
    }

    public Discount createDiscount(DeliveryOrder order, DiscountType type, DiscountStatus status, BigDecimal amount) {
        Discount discount = new Discount(order)
                .setAmount(amount)
                .setType(type)
                .setStatus(status);
        return this.discountRepository.save(discount);
    }

    public DeliveryOrder placeOrder(DeliveryOrder order) {
        order.getPackages().forEach(p -> {
            Optional<Charge> charge = p.getCharges().stream().filter(Charge::isDeliveryFee).findFirst();
            assert charge.isPresent();

            ContactAddress fromContactAddress = this.contactAddressRepository
                    .findFirstByContactAndAddress(p.getFromContact(), p.getFromAddress());
            fromContactAddress.setLastNote(p.getFromNote());
            this.contactAddressRepository.save(fromContactAddress);

            ContactAddress toContactAddress = this.contactAddressRepository
                    .findFirstByContactAndAddress(p.getToContact(), p.getToAddress());
            toContactAddress.setLastNote(p.getToNote());
            this.contactAddressRepository.save(toContactAddress);

            Contact fromContactCopy = p.getFromContact().buildUnsavedCopy();
            this.contactRepository.save(fromContactCopy);
            p.setFromContact(fromContactCopy);
            Contact toContactCopy = p.getToContact().buildUnsavedCopy();
            this.contactRepository.save(toContactCopy);
            p.setToContact(toContactCopy);

            Address fromAddressCopy = p.getFromAddress().buildCopy(false);
            this.addressRepository.save(fromAddressCopy);
            p.setFromAddress(fromAddressCopy);
            Address toAddressCopy = p.getToAddress().buildCopy(false);
            this.addressRepository.save(toAddressCopy);
            p.setToAddress(toAddressCopy);

            p.setStatus(PackageStatus.PLACED);
            this.packageRepository.save(p);
        });
        order.setStatus(OrderStatus.PLACED);
        this.orderRepository.save(order);
        return order;
    }


    private PackageStatus resolvePackageStatus(OrderStatus orderStatus) {
        switch (orderStatus) {
            case REQUEST:
                return PackageStatus.REQUEST;
            case STARTED:
                return PackageStatus.STARTED;
            case PENDING:
                return PackageStatus.PENDING;
            case PLACED:
                return PackageStatus.PLACED;
            case COMPLETE:
                return PackageStatus.COMPLETE;
            case CANCELED:
                return PackageStatus.CANCELED;
            case INCOMPLETE:
                return PackageStatus.INCOMPLETE;
            default:
                throw new IllegalArgumentException("Invalid order status " + orderStatus.name());
        }
    }

    private OrderStatus resolveOrderStatus(PackageStatus packageStatus) {
        switch (packageStatus) {
            case REQUEST:
                return OrderStatus.REQUEST;
            case STARTED:
                return OrderStatus.STARTED;
            case PENDING:
                return OrderStatus.PENDING;
            case PLACED:
                return OrderStatus.PLACED;
            case COMPLETE:
                return OrderStatus.COMPLETE;
            case CANCELED:
                return OrderStatus.CANCELED;
            case INCOMPLETE:
                return OrderStatus.INCOMPLETE;
            default:
                throw new IllegalArgumentException("Invalid package status " + packageStatus.name());
        }
    }

    private OffsetDateTime getPlacedAt(OrderStatus orderStatus) {
        if (orderStatus == OrderStatus.COMPLETE || orderStatus == OrderStatus.PLACED) {
            return TimeUtils.getUtcNow();
        }

        return null;
    }
}
