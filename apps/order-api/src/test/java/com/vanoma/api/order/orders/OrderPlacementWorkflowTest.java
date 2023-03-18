package com.vanoma.api.order.orders;

import com.vanoma.api.order.businesshours.BusinessHourRepository;
import com.vanoma.api.order.charges.Charge;
import com.vanoma.api.order.charges.ChargeRepository;
import com.vanoma.api.order.charges.ChargeStatus;
import com.vanoma.api.order.charges.ChargeType;
import com.vanoma.api.order.contacts.Address;
import com.vanoma.api.order.contacts.Contact;
import com.vanoma.api.order.contacts.ContactAddress;
import com.vanoma.api.order.contacts.ContactAddressRepository;
import com.vanoma.api.order.events.EventName;
import com.vanoma.api.order.events.PackageEvent;
import com.vanoma.api.order.events.PackageEventRepository;
import com.vanoma.api.order.packages.Package;
import com.vanoma.api.order.packages.PackageRepository;
import com.vanoma.api.order.packages.PackageSize;
import com.vanoma.api.order.packages.PackageStatus;
import com.vanoma.api.order.tests.OrderFactory;
import com.vanoma.api.order.tests.TimeTestUtils;
import com.vanoma.api.utils.exceptions.InvalidParameterException;
import com.vanoma.api.utils.exceptions.ResourceNotFoundException;
import com.vanoma.api.utils.input.TimeUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * {@link OrderPlacementWorkflow} is used indirectly through {@link com.vanoma.api.order.payment.PaymentController}
 * while processing payment-api response and in {@link OrderController} while creating a delivery order as an API
 * user as well as placing the delivery. To avoid duplicating tests to validate order placement, we are just adding
 * unit tests for that class instead.
 */
@ActiveProfiles("test")
@SpringBootTest
public class OrderPlacementWorkflowTest {

    @Autowired
    private OrderPlacementWorkflow orderPlacementWorkflow;

    @Autowired
    private BusinessHourRepository businessHourRepository;
    @Autowired
    private PackageRepository packageRepository;
    @Autowired
    private ChargeRepository chargeRepository;
    @Autowired
    private PackageEventRepository packageEventRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ContactAddressRepository contactAddressRepository;

    @Autowired
    private OrderFactory orderFactory;

    @Test
    public void testPlaceDeliveryOrder_withNullPickUpStart() {
        // Create business hours for today
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Create test data
        DeliveryOrder order = this.orderFactory.createOrder(OrderStatus.STARTED);
        Package pkg = this.orderFactory.createPackage(order, PackageSize.SMALL);
        Contact fromContact = pkg.getFromContact();
        Contact toContact = pkg.getToContact();
        Address fromAddress = pkg.getFromAddress();
        Address toAddress = pkg.getToAddress();
        Charge charge = this.orderFactory.createCharge(pkg, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);

        // Place order
        this.orderPlacementWorkflow.placeDeliveryOrder(order);

        // Validate order
        order = this.orderRepository.getById(order.getDeliveryOrderId());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(order.getPlacedAt()).isNotNull();

        // Validate package & pickUpStart
        pkg = this.packageRepository.getById(pkg.getPackageId());
        assertThat(pkg.getStatus()).isEqualTo(PackageStatus.PLACED);
        assertThat(pkg.getPickUpStart()).isNotNull();
        assertThat(pkg.getPickUpEnd()).isNotNull();

        // Validate contact & address duplication
        assertThat(pkg.getFromContact().getParentContactId()).isEqualTo(fromContact.getContactId());
        assertThat(pkg.getFromAddress().getParentAddressId()).isEqualTo(fromAddress.getAddressId());
        assertThat(pkg.getToContact().getParentContactId()).isEqualTo(toContact.getContactId());
        assertThat(pkg.getToAddress().getParentAddressId()).isEqualTo(toAddress.getAddressId());

        // Validate order placed event
        List<PackageEvent> packageEvents = this.packageEventRepository.findByPkg(pkg);
        assertThat(packageEvents.size()).isEqualTo(1);
        assertThat(packageEvents.get(0).getEventName()).isEqualTo(EventName.ORDER_PLACED);
    }

    @Test
    public void testPlaceDeliveryOrder_withNonNullPickUpStart() {
        // Create business hours for today
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Create test data
        OffsetDateTime pickUpStart = TimeUtils.getUtcNow().plusHours(1);
        DeliveryOrder order = this.orderFactory.createOrder(OrderStatus.STARTED);
        Package pkg = this.orderFactory.createPackage(order, pickUpStart);
        Contact fromContact = pkg.getFromContact();
        Contact toContact = pkg.getToContact();
        Address fromAddress = pkg.getFromAddress();
        Address toAddress = pkg.getToAddress();
        Charge charge = this.orderFactory.createCharge(pkg, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);

        // Place order
        this.orderPlacementWorkflow.placeDeliveryOrder(order);

        // Validate order
        order = this.orderRepository.getById(order.getDeliveryOrderId());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(order.getPlacedAt()).isNotNull();

        // Validate package & pickUpStart
        pkg = this.packageRepository.getById(pkg.getPackageId());
        assertThat(pkg.getStatus()).isEqualTo(PackageStatus.PLACED);
        assertThat(pkg.getPickUpStart()).isEqualTo(pickUpStart);
        assertThat(pkg.getPickUpEnd()).isEqualTo(pickUpStart.plusMinutes(15));

        // Validate contact & address duplication
        assertThat(pkg.getFromContact().getParentContactId()).isEqualTo(fromContact.getContactId());
        assertThat(pkg.getFromAddress().getParentAddressId()).isEqualTo(fromAddress.getAddressId());
        assertThat(pkg.getToContact().getParentContactId()).isEqualTo(toContact.getContactId());
        assertThat(pkg.getToAddress().getParentAddressId()).isEqualTo(toAddress.getAddressId());

        // Validate order placed event
        List<PackageEvent> packageEvents = this.packageEventRepository.findByPkg(pkg);
        assertThat(packageEvents.size()).isEqualTo(1);
        assertThat(packageEvents.get(0).getEventName()).isEqualTo(EventName.ORDER_PLACED);
    }

    @Test
    public void testPlaceDeliveryOrder_savesStopNoteOnContactAddress() {
        // Create business hours for today
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Create test data
        DeliveryOrder order = this.orderFactory.createOrder(OrderStatus.STARTED);
        Package pkg = this.orderFactory.createPackage(order, PackageSize.SMALL);
        pkg.setFromNote("Test from notes");
        pkg.setToNote("Test to notes");
        this.packageRepository.save(pkg);

        Contact fromContact = pkg.getFromContact();
        Contact toContact = pkg.getToContact();
        Address fromAddress = pkg.getFromAddress();
        Address toAddress = pkg.getToAddress();
        Charge charge = this.orderFactory.createCharge(pkg, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);

        // Place order
        this.orderPlacementWorkflow.placeDeliveryOrder(order);

        // Validate order
        order = this.orderRepository.getById(order.getDeliveryOrderId());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(order.getPlacedAt()).isNotNull();

        // Validate package
        pkg = this.packageRepository.getById(pkg.getPackageId());
        assertThat(pkg.getStatus()).isEqualTo(PackageStatus.PLACED);
        assertThat(pkg.getPickUpStart()).isNotNull();
        assertThat(pkg.getPickUpEnd()).isNotNull();

        // Validate notes duplication
        ContactAddress fromContactAddress = this.contactAddressRepository.findFirstByContactAndAddress(fromContact, fromAddress);
        ContactAddress toContactAddress = this.contactAddressRepository.findFirstByContactAndAddress(toContact, toAddress);
        assertThat(fromContactAddress.getLastNote()).isEqualTo("Test from notes");
        assertThat(toContactAddress.getLastNote()).isEqualTo("Test to notes");
    }

    @Test
    public void testPlaceDeliveryOrder_raisesErrorIfDeliveryFeeIsMissing() {
        // Create business hours for today
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Create test data
        DeliveryOrder order = this.orderFactory.createOrderWithPackage(OrderStatus.STARTED);

        // Place order
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            this.orderPlacementWorkflow.placeDeliveryOrder(order);
        });
        assertThat(exception.getMessage()).isEqualTo("crud.charge.deliveryFee.notFound");
    }

    @Test
    public void testPlaceDeliveryOrder_raisesErrorIfToContactIsMissing() {
        // Create business hours for today
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Create test data
        DeliveryOrder order = this.orderFactory.createOrder(OrderStatus.STARTED);
        Package pkg = this.orderFactory.createPackage(order, PackageSize.SMALL);
        pkg.setToContact(null);
        this.packageRepository.save(pkg);

        // Place order
        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            this.orderPlacementWorkflow.placeDeliveryOrder(order);
        });
        assertThat(exception.getMessage()).isEqualTo("crud.package.toContact.notFound");
    }

    @Test
    public void testPlaceDeliveryOrder_raisesErrorIfToAddressIsMissing() {
        // Create business hours for today
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Create test data
        DeliveryOrder order = this.orderFactory.createOrder(OrderStatus.STARTED);
        Package pkg = this.orderFactory.createPackage(order, PackageSize.SMALL);
        pkg.setToAddress(null);
        this.packageRepository.save(pkg);

        // Place order
        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            this.orderPlacementWorkflow.placeDeliveryOrder(order);
        });
        assertThat(exception.getMessage()).isEqualTo("crud.package.toAddress.notFound");
    }

    @Test
    public void testPlaceDeliveryOrder_raisesErrorIfPackageSizeIsMissing() {
        // Create business hours for today
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Create test data
        DeliveryOrder order = this.orderFactory.createOrder(OrderStatus.STARTED);
        Package pkg = this.orderFactory.createPackage(order, PackageSize.SMALL);
        pkg.setSize(null);
        this.packageRepository.save(pkg);

        // Place order
        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            this.orderPlacementWorkflow.placeDeliveryOrder(order);
        });
        assertThat(exception.getMessage()).isEqualTo("crud.package.size.notFound");
    }

    @Test
    public void testPlaceDeliveryOrder_raisesErrorIfPickUpTimeIsBeyond48Hours() {
        // Create business hours for today
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Create test data
        DeliveryOrder order = this.orderFactory.createOrder(OrderStatus.STARTED);
        Package pkg = this.orderFactory.createPackage(order, TimeUtils.getUtcNow().plusHours(50));
        Charge charge = this.orderFactory.createCharge(pkg, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);

        // Place order
        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            this.orderPlacementWorkflow.placeDeliveryOrder(order);
        });
        assertThat(exception.getMessage()).isEqualTo("crud.package.pickUpStart.beyond48Hours");
    }

    @Test
    public void testPlaceDeliveryOrder_raisesErrorIfPickUpTimeIsEarlierThanCurrentTime() {
        // Create business hours for today
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Create test data
        DeliveryOrder order = this.orderFactory.createOrder(OrderStatus.STARTED);
        Package pkg = this.orderFactory.createPackage(order, TimeUtils.getUtcNow().minusHours(1));
        Charge charge = this.orderFactory.createCharge(pkg, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);

        // Place order
        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            this.orderPlacementWorkflow.placeDeliveryOrder(order);
        });
        assertThat(exception.getMessage()).isEqualTo("crud.package.pickUpStart.inThePast");
    }

    @Test
    public void testPlaceDeliveryOrder_raisesErrorIfPickUpTimeIsBeforeOpenHours() {
        // Create business hours for today
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.now().plusHours(2), OffsetTime.MAX);

        // Create test data
        DeliveryOrder order = this.orderFactory.createOrder(OrderStatus.STARTED);
        Package pkg = this.orderFactory.createPackage(order, TimeUtils.getUtcNow().plusHours(1));
        Charge charge = this.orderFactory.createCharge(pkg, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);

        // Place order
        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            this.orderPlacementWorkflow.placeDeliveryOrder(order);
        });
        assertThat(exception.getMessage()).isEqualTo("crud.businessHour.noOrderBeforeOpenAt");
    }

    @Test
    public void testPlaceDeliveryOrder_raisesErrorIfPickUpTimeIsAfterClosingHours() {
        // Create business hours for today
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.now().plusHours(1));

        // Create test data
        DeliveryOrder order = this.orderFactory.createOrder(OrderStatus.STARTED);
        Package pkg = this.orderFactory.createPackage(order, TimeUtils.getUtcNow().plusHours(2));
        Charge charge = this.orderFactory.createCharge(pkg, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);

        // Place order
        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            this.orderPlacementWorkflow.placeDeliveryOrder(order);
        });
        assertThat(exception.getMessage()).isEqualTo("crud.businessHour.noOrderAfterCloseAt");
    }
}
