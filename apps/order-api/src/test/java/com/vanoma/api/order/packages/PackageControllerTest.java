package com.vanoma.api.order.packages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vanoma.api.order.businesshours.BusinessHour;
import com.vanoma.api.order.businesshours.BusinessHourRepository;
import com.vanoma.api.order.charges.Charge;
import com.vanoma.api.order.charges.ChargeRepository;
import com.vanoma.api.order.charges.ChargeStatus;
import com.vanoma.api.order.contacts.Address;
import com.vanoma.api.order.contacts.AddressRepository;
import com.vanoma.api.order.contacts.Contact;
import com.vanoma.api.order.customers.Agent;
import com.vanoma.api.order.customers.Branch;
import com.vanoma.api.order.customers.Customer;
import com.vanoma.api.order.events.EventName;
import com.vanoma.api.order.events.PackageEvent;
import com.vanoma.api.order.events.PackageEventRepository;
import com.vanoma.api.order.orders.DeliveryOrder;
import com.vanoma.api.order.orders.OrderRepository;
import com.vanoma.api.order.orders.OrderStatus;
import com.vanoma.api.order.payment.PaymentStatus;
import com.vanoma.api.order.tests.ObjectFactory;
import com.vanoma.api.order.tests.OrderFactory;
import com.vanoma.api.order.tests.ResourceMapper;
import com.vanoma.api.order.tests.TimeTestUtils;
import com.vanoma.api.utils.NullableValueMapBuilder;
import com.vanoma.api.utils.httpwrapper.HttpResult;
import com.vanoma.api.utils.httpwrapper.IHttpClientWrapper;
import com.vanoma.api.utils.input.PhoneNumberUtils;
import com.vanoma.api.utils.input.TimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.*;

import static com.vanoma.api.order.tests.ControllerTestUtils.parseResponseBody;
import static com.vanoma.api.order.tests.ControllerTestUtils.stringifyRequestBody;
import static java.util.Map.entry;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class PackageControllerTest {
    private final String driverId = UUID.randomUUID().toString();

    private Customer customer;

    @Autowired
    private OrderFactory orderFactory;

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PackageRepository packageRepository;
    @Autowired
    private PackageEventRepository packageEventRepository;
    @Autowired
    private ChargeRepository chargeRepository;

    @Autowired
    private MockMvc mvc;
    @MockBean
    private IHttpClientWrapper httpClientWrapper;

    @Autowired
    private BusinessHourRepository businessHourRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        // Delete existing packages to avoid polluting the next test. We have to delete events and
        // charges first because cascading deletes is not working :(
        this.packageEventRepository.deleteAll();
        this.chargeRepository.deleteAll();
        this.packageRepository.deleteAll();

        this.customer = this.orderFactory.createCustomer();

        reset(this.httpClientWrapper);
        when(this.httpClientWrapper.post(any(String.class), any(Map.class)))
                .thenReturn(new HttpResult(new HashMap<>(), HttpStatus.OK.value()));
    }

    @Test
    public void testGetPackages_filtersByPackageId() throws Exception {
        List<Package> packages = List.of(
                orderFactory.createPackage(customer),
                orderFactory.createPackage(customer),
                orderFactory.createPackage(customer));

        RequestBuilder requestBuilder = get(String.format("/packages?packageId=%s,%s,%s", packages.get(0).getPackageId(), packages.get(1).getPackageId(), UUID.randomUUID()));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = new NullableValueMapBuilder<String, Object>()
                .put("count", 2)
                .put("next", null)
                .put("previous", null)
                .put("results", List.of(ResourceMapper.createPackageMap(packages.get(1)), ResourceMapper.createPackageMap(packages.get(0))))
                .build();
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetPackages_filtersByPackageStatus() throws Exception {
        List<Package> packages = List.of(
                orderFactory.createPackage(customer, PackageStatus.PENDING),
                orderFactory.createPackage(customer, PackageStatus.PENDING),
                orderFactory.createPackage(customer, PackageStatus.COMPLETE));

        RequestBuilder requestBuilder = get(String.format("/packages?status=%s", PackageStatus.PENDING));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = new NullableValueMapBuilder<String, Object>()
                .put("count", 2)
                .put("next", null)
                .put("previous", null)
                .put("results", List.of(ResourceMapper.createPackageMap(packages.get(1)), ResourceMapper.createPackageMap(packages.get(0))))
                .build();
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetPackages_filtersByPaymentStatus() throws Exception {
        List<Package> packages = List.of(
                orderFactory.createPackage(customer, PackageStatus.PLACED),
                orderFactory.createPackage(customer, PackageStatus.PLACED));

        // Create two charges, one for each package
        orderFactory.createCharge(packages.get(0), ChargeStatus.UNPAID);
        orderFactory.createCharge(packages.get(1), ChargeStatus.PAID);

        RequestBuilder requestBuilder = get(String.format("/packages?paymentStatus=%s", PaymentStatus.PAID));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = new NullableValueMapBuilder<String, Object>()
                .put("count", 1)
                .put("next", null)
                .put("previous", null)
                .put("results", List.of(ResourceMapper.createPackageMap(this.packageRepository.getById(packages.get(1).getPackageId()))))
                .build();
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetPackages_filtersByIsAssigned() throws Exception {
        List<Package> packages = List.of(
                orderFactory.createPackage(customer, PackageStatus.PLACED),
                orderFactory.createPackage(customer, PackageStatus.PLACED),
                orderFactory.createPackage(customer, PackageStatus.PLACED));
        this.packageRepository.save(packages.get(0).setDriverId(UUID.randomUUID().toString()));
        this.packageRepository.save(packages.get(2).setDriverId(UUID.randomUUID().toString()));

        // Create two charges, one for each package
        orderFactory.createCharge(packages.get(0), ChargeStatus.UNPAID);
        orderFactory.createCharge(packages.get(1), ChargeStatus.PAID);

        RequestBuilder requestBuilder = get("/packages?isAssigned=false");
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = new NullableValueMapBuilder<String, Object>()
                .put("count", 1)
                .put("next", null)
                .put("previous", null)
                .put("results", List.of(ResourceMapper.createPackageMap(this.packageRepository.getById(packages.get(1).getPackageId()))))
                .build();
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetPackages_filtersByIsAssignable() throws Exception {
        List<Package> packages = List.of(
                orderFactory.createPackage(customer, PackageStatus.PLACED),
                orderFactory.createPackage(customer, PackageStatus.PLACED),
                orderFactory.createPackage(customer, PackageStatus.PLACED));
        this.packageRepository.save(packages.get(0).setIsAssignable(false));
        this.packageRepository.save(packages.get(2).setIsAssignable(false));

        // Create two charges, one for each package
        orderFactory.createCharge(packages.get(0), ChargeStatus.UNPAID);
        orderFactory.createCharge(packages.get(1), ChargeStatus.PAID);

        RequestBuilder requestBuilder = get("/packages?isAssignable=true");
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = new NullableValueMapBuilder<String, Object>()
                .put("count", 1)
                .put("next", null)
                .put("previous", null)
                .put("results", List.of(ResourceMapper.createPackageMap(this.packageRepository.getById(packages.get(1).getPackageId()))))
                .build();
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetPackages_filtersByFirstDigitsOfTrackingNumber() throws Exception {
        List<Package> packages = List.of(
                orderFactory.createPackage(customer, PackageStatus.PENDING),
                orderFactory.createPackage(customer, PackageStatus.PENDING),
                orderFactory.createPackage(customer, PackageStatus.COMPLETE));

        RequestBuilder requestBuilder = get(String.format("/packages?trackingNumber=%s", packages.get(1).getTrackingNumber().substring(0, 4)));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = new NullableValueMapBuilder<String, Object>()
                .put("count", 1)
                .put("next", null)
                .put("previous", null)
                .put("results", List.of(ResourceMapper.createPackageMap(packages.get(1))))
                .build();
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetPackages_filtersByPackageStatusAndTrackingNumber() throws Exception {
        List<Package> packages = List.of(
                orderFactory.createPackage(customer, PackageStatus.STARTED),
                orderFactory.createPackage(customer, PackageStatus.STARTED),
                orderFactory.createPackage(customer, PackageStatus.COMPLETE));

        RequestBuilder requestBuilder = get(String.format("/packages?status=%s&trackingNumber=%s&sort=createdAt", PackageStatus.STARTED, packages.get(0).getTrackingNumber()));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = new NullableValueMapBuilder<String, Object>()
                .put("count", 1)
                .put("next", null)
                .put("previous", null)
                .put("results", List.of(ResourceMapper.createPackageMap(packages.get(0))))
                .build();
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetPackages_filtersByPackageStatusAndFromPhoneNumber() throws Exception {
        List<Package> packages = List.of(
                orderFactory.createPackage(customer, PackageStatus.STARTED),
                orderFactory.createPackage(customer, PackageStatus.STARTED),
                orderFactory.createPackage(customer, PackageStatus.COMPLETE));

        RequestBuilder requestBuilder = get(String.format("/packages?status=%s&phoneNumber=%s&sort=createdAt", PackageStatus.STARTED, packages.get(0).getFromContact().getPhoneNumberOne()));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = new NullableValueMapBuilder<String, Object>()
                .put("count", 1)
                .put("next", null)
                .put("previous", null)
                .put("results", List.of(ResourceMapper.createPackageMap(packages.get(0))))
                .build();
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetPackages_filtersByPackageStatusAndToPhoneNumber() throws Exception {
        List<Package> packages = List.of(
                orderFactory.createPackage(customer, PackageStatus.STARTED),
                orderFactory.createPackage(customer, PackageStatus.STARTED),
                orderFactory.createPackage(customer, PackageStatus.COMPLETE));

        RequestBuilder requestBuilder = get(String.format("/packages?status=%s&phoneNumber=%s&sort=createdAt", PackageStatus.STARTED, packages.get(0).getToContact().getPhoneNumberOne()));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = new NullableValueMapBuilder<String, Object>()
                .put("count", 1)
                .put("next", null)
                .put("previous", null)
                .put("results", List.of(ResourceMapper.createPackageMap(packages.get(0))))
                .build();
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetPackages_filtersByPackageStatusAndBusinessPhoneNumber() throws Exception {
        List<Package> packages = List.of(
                orderFactory.createPackage(customer, PackageStatus.STARTED),
                orderFactory.createPackage(this.orderFactory.createCustomer(), PackageStatus.STARTED),
                orderFactory.createPackage(customer, PackageStatus.COMPLETE));

        RequestBuilder requestBuilder = get(String.format("/packages?status=%s&phoneNumber=%s&sort=createdAt", PackageStatus.STARTED, customer.getPhoneNumber()));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = new NullableValueMapBuilder<String, Object>()
                .put("count", 1)
                .put("next", null)
                .put("previous", null)
                .put("results", List.of(ResourceMapper.createPackageMap(packages.get(0))))
                .build();
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetPackages_filtersReturnedEventsByAssignmentId() throws Exception {
        String assignmentId = UUID.randomUUID().toString();

        // Create a package
        Package pkg = this.orderFactory.createPackage(customer);
        this.packageRepository.save(pkg.setAssignmentId(assignmentId));

        // Create events
        PackageEvent oldAssignmentEvent = this.orderFactory.createPackageEvent(pkg, EventName.DRIVER_ASSIGNED, UUID.randomUUID().toString());
        PackageEvent currentAssignmentEvent = this.orderFactory.createPackageEvent(pkg, EventName.DRIVER_ASSIGNED, assignmentId);

        RequestBuilder requestBuilder = get(String.format("/packages?packageId=%s", pkg.getPackageId()));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = new NullableValueMapBuilder<String, Object>()
                .put("count", 1)
                .put("next", null)
                .put("previous", null)
                .put("results", List.of(ResourceMapper.createPackageMap(this.packageRepository.getById(pkg.getPackageId()))))
                .build();
        assertThat(actualBody).isEqualTo(expectedBody);

        List<Map<String, Object>> actualEvents = (List<Map<String, Object>>) ((List<Map<String, Object>>) actualBody.get("results")).get(0).get("events");
        List<Map<String, Object>> expectedEvents = List.of(ResourceMapper.createPackageEventMap(currentAssignmentEvent));
        assertThat(actualEvents).isEqualTo(expectedEvents);
    }

    @Test
    public void testGetPackages_filtersByBranchId() throws Exception {
        Branch branch = this.orderFactory.createBranch(customer);
        Agent agent = this.orderFactory.createAgent(branch);
        DeliveryOrder order = this.orderFactory.createOrder(agent, OrderStatus.STARTED);
        List<Package> packages = List.of(
                orderFactory.createPackage(order, PackageSize.SMALL),
                orderFactory.createPackage(customer),
                orderFactory.createPackage(customer));

        RequestBuilder requestBuilder = get(String.format("/packages?branchId=%s", branch.getBranchId()));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = new NullableValueMapBuilder<String, Object>()
                .put("count", 1)
                .put("next", null)
                .put("previous", null)
                .put("results", List.of(ResourceMapper.createPackageMap(packages.get(0))))
                .build();
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetCustomerPackages() throws Exception {
        Customer targetCustomer = this.orderFactory.createCustomer();
        List<Package> packages = List.of(
                orderFactory.createPackage(customer),
                orderFactory.createPackage(customer),
                orderFactory.createPackage(targetCustomer));

        RequestBuilder requestBuilder = get(String.format("/customers/%s/packages?sort=createdAt", targetCustomer.getCustomerId()));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = new NullableValueMapBuilder<String, Object>()
                .put("count", 1)
                .put("next", null)
                .put("previous", null)
                .put("results", List.of(ResourceMapper.createPackageMap(packages.get(2))))
                .build();
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetCustomerPackages_filtersByPackageStatus() throws Exception {
        Customer targetCustomer = this.orderFactory.createCustomer();
        List<Package> packages = List.of(
                orderFactory.createPackage(customer),
                orderFactory.createPackage(customer),
                orderFactory.createPackage(targetCustomer, PackageStatus.PENDING),
                orderFactory.createPackage(targetCustomer, PackageStatus.STARTED));

        RequestBuilder requestBuilder = get(String.format("/customers/%s/packages?status=%s&sort=createdAt", targetCustomer.getCustomerId(), PackageStatus.PENDING));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = new NullableValueMapBuilder<String, Object>()
                .put("count", 1)
                .put("next", null)
                .put("previous", null)
                .put("results", List.of(ResourceMapper.createPackageMap(packages.get(2))))
                .build();
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetDeliveryOrderPackages() throws Exception {
        DeliveryOrder order = this.orderFactory.createOrder(customer);
        List<Package> packages = List.of(
                orderFactory.createPackage(order, PackageSize.SMALL),
                orderFactory.createPackage(customer));

        RequestBuilder requestBuilder = get(String.format("/delivery-orders/%s/packages?sort=createdAt", order.getDeliveryOrderId()));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = new NullableValueMapBuilder<String, Object>()
                .put("count", 1)
                .put("next", null)
                .put("previous", null)
                .put("results", List.of(ResourceMapper.createPackageMap(packages.get(0))))
                .build();
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testCreatePackage_withPickUpStart() throws Exception {
        // We need today's business hour
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Create test data
        DeliveryOrder order = this.orderFactory.createOrder(customer);
        Contact fromContact = this.orderFactory.createContact(customer.getCustomerId());
        Address fromAddress = this.orderFactory.createAddress(customer.getCustomerId());
        Contact toContact = this.orderFactory.createContact(customer.getCustomerId());
        Address toAddress = this.orderFactory.createAddress(customer.getCustomerId());

        // Request body
        OffsetDateTime pickUpStart = TimeUtils.getUtcNow().plusHours(1);
        Map<String, Object> requestBody = Map.of(
                "size", PackageSize.LARGE.name(),
                "priority", PackagePriority.EXPRESS.name(),
                "fromContact", Map.of(
                        "contactId", fromContact.getContactId()
                ),
                "toContact", Map.of(
                        "contactId", toContact.getContactId()
                ),
                "fromAddress", Map.of(
                        "addressId", fromAddress.getAddressId()
                ),
                "toAddress", Map.of(
                        "addressId", toAddress.getAddressId()
                ),
                "pickUpStart", pickUpStart.toString()
        );

        // Call endpoint
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/packages", order.getDeliveryOrderId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        // Validate pickUpStart
        Package pkg = this.packageRepository.findByDeliveryOrder(order).get(0);
        assertThat(pkg.getPickUpStart()).isEqualTo(pickUpStart);
        assertThat(pkg.getPickUpEnd()).isEqualTo(pickUpStart.plusMinutes(15));

        // Validate response
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createPackageMap(pkg);
        assertThat(actualBody).isEqualTo(expectedBody);
    }
    @Test
    public void testCreatePackage_withTomorrowPickUpStart() throws Exception {
        // We need today's business hour
        int dayOfWeek = TimeTestUtils.getDayOfWeek();
        this.orderFactory.createBusinessHour(dayOfWeek, OffsetTime.MIN, OffsetTime.MAX);
        int nextDay = dayOfWeek == 7 ? 1 : dayOfWeek + 1;
        this.orderFactory.createBusinessHour(nextDay, OffsetTime.MIN, OffsetTime.MAX);


        // Create test data
        DeliveryOrder order = this.orderFactory.createOrder(customer);
        Contact fromContact = this.orderFactory.createContact(customer.getCustomerId());
        Address fromAddress = this.orderFactory.createAddress(customer.getCustomerId());
        Contact toContact = this.orderFactory.createContact(customer.getCustomerId());
        Address toAddress = this.orderFactory.createAddress(customer.getCustomerId());

        // Request body
        OffsetDateTime pickUpStart = TimeUtils.getUtcNow().plusDays(1).plusHours(1); // Add a day
        Map<String, Object> requestBody = Map.of(
                "size", PackageSize.LARGE.name(),
                "priority", PackagePriority.EXPRESS.name(),
                "fromContact", Map.of(
                        "contactId", fromContact.getContactId()
                ),
                "toContact", Map.of(
                        "contactId", toContact.getContactId()
                ),
                "fromAddress", Map.of(
                        "addressId", fromAddress.getAddressId()
                ),
                "toAddress", Map.of(
                        "addressId", toAddress.getAddressId()
                ),
                "pickUpStart", pickUpStart.toString()
        );

        // Call endpoint
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/packages", order.getDeliveryOrderId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        // Validate pickUpStart
        Package pkg = this.packageRepository.findByDeliveryOrder(order).get(0);
        assertThat(pkg.getPickUpStart()).isEqualTo(pickUpStart);
        assertThat(pkg.getPickUpEnd()).isEqualTo(pickUpStart.plusMinutes(15));

        // Validate response
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createPackageMap(pkg);
        assertThat(actualBody).isEqualTo(expectedBody);
    }


    @Test
    public void testCreatePackage_withoutPickUpStart() throws Exception {
        // We need today's business hour
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Create test data
        DeliveryOrder order = this.orderFactory.createOrder(customer);
        Contact fromContact = this.orderFactory.createContact(customer.getCustomerId());
        Address fromAddress = this.orderFactory.createAddress(customer.getCustomerId());
        Contact toContact = this.orderFactory.createContact(customer.getCustomerId());
        Address toAddress = this.orderFactory.createAddress(customer.getCustomerId());

        // Request body
        Map<String, Object> requestBody = new NullableValueMapBuilder<String, Object>()
                .put("size", PackageSize.LARGE.name())
                .put("priority", PackagePriority.EXPRESS.name())
                .put("fromContact", Map.of(
                                "contactId", fromContact.getContactId()
                ))
                .put("toContact", Map.of(
                        "contactId", toContact.getContactId()
                ))
                .put("fromAddress", Map.of(
                        "addressId", fromAddress.getAddressId()
                ))
                .put("toAddress", Map.of(
                        "addressId", toAddress.getAddressId()
                ))
                .put("pickUpStart", null)
                .build();

        // Call endpoint
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/packages", order.getDeliveryOrderId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        // Validate pickUpStart
        Package pkg = this.packageRepository.findByDeliveryOrder(order).get(0);
        assertThat(pkg.getPickUpStart()).isNull();
        assertThat(pkg.getPickUpEnd()).isNull();

        // Validate response
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createPackageMap(pkg);
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testCreatePackage_createsOrReusesExistingContactAndAddress() throws Exception {
        // We need today's business hour
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Create test data
        DeliveryOrder order = this.orderFactory.createOrder(customer);
        Contact fromContact = this.orderFactory.createContact(customer.getCustomerId());
        Address fromAddress = this.orderFactory.createAddress(customer.getCustomerId());

        // Request body
        Map<String, Object> requestBody = Map.of(
                "size", PackageSize.LARGE.name(),
                "priority", PackagePriority.EXPRESS.name(),
                "fromContact", Map.of(
                        "contactId", fromContact.getContactId()
                ),
                "toContact", Map.of(
                        "phoneNumberOne", "250788221133"
                ),
                "fromAddress", Map.of(
                        "addressId", fromAddress.getAddressId()
                ),
                "toAddress", Map.of(
                        "houseNumber", "12",
                        "streetName", "KG 32 ST",
                        "district", "Gasabo",
                        "latitude", -1.94995,
                        "longitude", 30.05885,
                        "landmark", "MTN Nyarutarama",
                        "placeName", "MTN Rwanda",
                        "addressName", "Test address"
                )
        );

        // Call endpoint
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/packages", order.getDeliveryOrderId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        // Validate reusing existing contact & address
        Package pkg = this.packageRepository.findByDeliveryOrder(order).get(0);
        assertThat(pkg.getFromContact().getContactId()).isEqualTo(fromContact.getContactId());
        assertThat(pkg.getFromAddress().getAddressId()).isEqualTo(fromAddress.getAddressId());
        assertThat(pkg.getToContact().getPhoneNumberOne()).isEqualTo("250788221133");
        assertThat(pkg.getToAddress().getLatitude()).isEqualTo(-1.94995);
        assertThat(pkg.getToAddress().getLongitude()).isEqualTo(30.05885);

        // Validate response
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createPackageMap(pkg);
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testCreatePackage_returnsErrorWhenOrderIsNotUpdatable() throws Exception {
        // We need today's business hour
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Create test data
        DeliveryOrder order = this.orderFactory.createOrder(customer, OrderStatus.PLACED);
        Contact fromContact = this.orderFactory.createContact(customer.getCustomerId());
        Address fromAddress = this.orderFactory.createAddress(customer.getCustomerId());
        Contact toContact = this.orderFactory.createContact(customer.getCustomerId());
        Address toAddress = this.orderFactory.createAddress(customer.getCustomerId());

        // Request body
        Map<String, Object> requestBody = Map.of(
                "size", PackageSize.LARGE.name(),
                "priority", PackagePriority.EXPRESS.name(),
                "fromContact", Map.of(
                        "contactId", fromContact.getContactId()
                ),
                "toContact", Map.of(
                        "contactId", toContact.getContactId()
                ),
                "fromAddress", Map.of(
                        "addressId", fromAddress.getAddressId()
                ),
                "toAddress", Map.of(
                        "addressId", toAddress.getAddressId()
                )
        );

        // Call endpoint
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/packages", order.getDeliveryOrderId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate response
        assertThat(result.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "AUTHORIZATION_ERROR",
                "errorMessage", "You cannot modify this order (already placed)"
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testCreatePackage_returnsErrorWhenPickUpTimeIsBeyond48Hours() throws Exception {
        // We need today's business hour
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Create test data
        DeliveryOrder order = this.orderFactory.createOrder(customer);
        Contact fromContact = this.orderFactory.createContact(customer.getCustomerId());
        Address fromAddress = this.orderFactory.createAddress(customer.getCustomerId());
        Contact toContact = this.orderFactory.createContact(customer.getCustomerId());
        Address toAddress = this.orderFactory.createAddress(customer.getCustomerId());

        // Request body
        Map<String, Object> requestBody = Map.of(
                "size", PackageSize.LARGE.name(),
                "priority", PackagePriority.EXPRESS.name(),
                "fromContact", Map.of(
                        "contactId", fromContact.getContactId()
                ),
                "toContact", Map.of(
                        "contactId", toContact.getContactId()
                ),
                "fromAddress", Map.of(
                        "addressId", fromAddress.getAddressId()
                ),
                "toAddress", Map.of(
                        "addressId", toAddress.getAddressId()
                ),
                "pickUpStart", TimeUtils.getUtcNow().plusHours(50).toString()
        );

        // Call endpoint
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/packages", order.getDeliveryOrderId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate response
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "Pick-up time must be within 48 hours"
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testCreatePackage_returnsErrorWhenPickUpTimeIsEarlierThanCurrentTime() throws Exception {
        // We need today's business hour
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Create test data
        DeliveryOrder order = this.orderFactory.createOrder(customer);
        Contact fromContact = this.orderFactory.createContact(customer.getCustomerId());
        Address fromAddress = this.orderFactory.createAddress(customer.getCustomerId());
        Contact toContact = this.orderFactory.createContact(customer.getCustomerId());
        Address toAddress = this.orderFactory.createAddress(customer.getCustomerId());

        // Request body
        Map<String, Object> requestBody = Map.of(
                "size", PackageSize.LARGE.name(),
                "priority", PackagePriority.EXPRESS.name(),
                "fromContact", Map.of(
                        "contactId", fromContact.getContactId()
                ),
                "toContact", Map.of(
                        "contactId", toContact.getContactId()
                ),
                "fromAddress", Map.of(
                        "addressId", fromAddress.getAddressId()
                ),
                "toAddress", Map.of(
                        "addressId", toAddress.getAddressId()
                ),
                "pickUpStart", TimeUtils.getUtcNow().minusHours(1).toString()
        );

        // Call endpoint
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/packages", order.getDeliveryOrderId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate response
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "Pick-up time must not be in the past"
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testCreatePackage_returnsErrorWhenPickUpTimeIsBeforeOpenHours() throws Exception {
        // We need today's business hour
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.now().plusHours(2), OffsetTime.MAX);

        // Create test data
        DeliveryOrder order = this.orderFactory.createOrder(customer);
        Contact fromContact = this.orderFactory.createContact(customer.getCustomerId());
        Address fromAddress = this.orderFactory.createAddress(customer.getCustomerId());
        Contact toContact = this.orderFactory.createContact(customer.getCustomerId());
        Address toAddress = this.orderFactory.createAddress(customer.getCustomerId());

        // Request body
        Map<String, Object> requestBody = Map.of(
                "size", PackageSize.LARGE.name(),
                "priority", PackagePriority.EXPRESS.name(),
                "fromContact", Map.of(
                        "contactId", fromContact.getContactId()
                ),
                "toContact", Map.of(
                        "contactId", toContact.getContactId()
                ),
                "fromAddress", Map.of(
                        "addressId", fromAddress.getAddressId()
                ),
                "toAddress", Map.of(
                        "addressId", toAddress.getAddressId()
                ),
                "pickUpStart", TimeUtils.getUtcNow().plusHours(1).toString()
        );

        // Call endpoint
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/packages", order.getDeliveryOrderId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate response
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "Pick-up time is earlier than business hours"
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testCreatePackage_returnsErrorWhenPickUpTimeIsAfterClosingHours() throws Exception {
        // We need today's business hour
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.now().plusHours(1));

        // Create test data
        DeliveryOrder order = this.orderFactory.createOrder(customer);
        Contact fromContact = this.orderFactory.createContact(customer.getCustomerId());
        Address fromAddress = this.orderFactory.createAddress(customer.getCustomerId());
        Contact toContact = this.orderFactory.createContact(customer.getCustomerId());
        Address toAddress = this.orderFactory.createAddress(customer.getCustomerId());

        // Request body
        Map<String, Object> requestBody = Map.of(
                "size", PackageSize.LARGE.name(),
                "priority", PackagePriority.EXPRESS.name(),
                "fromContact", Map.of(
                        "contactId", fromContact.getContactId()
                ),
                "toContact", Map.of(
                        "contactId", toContact.getContactId()
                ),
                "fromAddress", Map.of(
                        "addressId", fromAddress.getAddressId()
                ),
                "toAddress", Map.of(
                        "addressId", toAddress.getAddressId()
                ),
                "pickUpStart", TimeUtils.getUtcNow().plusHours(2).toString()
        );

        // Call endpoint
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/packages", order.getDeliveryOrderId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate response
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "Pick-up time must be before our closing time"
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testUpdatePackage_updatesPublicFields() throws Exception {
        // Create business hours for today
        BusinessHour today = new BusinessHour()
                .setWeekDay(TimeUtils.getUtcNow().getDayOfWeek().getValue())
                .setOpenAt(OffsetTime.MIN)
                .setCloseAt(OffsetTime.MAX);
        this.businessHourRepository.save(today);

        Package existingPackage = this.orderFactory.createPackage(customer);
        Contact fromContact = this.orderFactory.createContact(customer.getCustomerId());
        Contact toContact = this.orderFactory.createContact(customer.getCustomerId());
        Address fromAddress = this.orderFactory.createAddress(customer.getCustomerId());
        Address toAddress = this.orderFactory.createAddress(customer.getCustomerId());

        // Request body
        OffsetDateTime pickUpStart = TimeUtils.getUtcNow().plusMinutes(5);
        Map<String, Object> requestBody = Map.ofEntries(
                entry("size", PackageSize.LARGE.name()),
                entry("priority", PackagePriority.EXPRESS.name()),
                entry("fromContact", Map.of(
                        "contactId", fromContact.getContactId()
                )),
                entry("toContact", Map.of(
                        "contactId", toContact.getContactId()
                )),
                entry("fromAddress", Map.of(
                        "addressId", fromAddress.getAddressId()
                )),
                entry("toAddress", Map.of(
                        "addressId", toAddress.getAddressId()
                )),
                entry("fromNote", "Pick up note"),
                entry("toNote", "Drop off note"),
                entry("pickUpStart", pickUpStart.toString()),
                entry("status", PackageStatus.PLACED.name()),
                entry("driverId", driverId),
                entry("staffNote", "Staff note"));

        // Update package
        RequestBuilder requestBuilder = patch("/packages/" + existingPackage.getPackageId())
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate that update happened
        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createPackageMap(this.packageRepository.getById(existingPackage.getPackageId()));
        assertThat(actualBody).isEqualTo(expectedBody);

        JsonNode jsonNode = this.objectMapper.valueToTree(actualBody);
        assertThat(jsonNode.get("size").asText()).isEqualTo(PackageSize.LARGE.name());
        assertThat(jsonNode.get("priority").asText()).isEqualTo(PackagePriority.EXPRESS.name());
        assertThat(jsonNode.at("/fromContact/contactId").asText()).isEqualTo(fromContact.getContactId());
        assertThat(jsonNode.at("/toContact/contactId").asText()).isEqualTo(toContact.getContactId());
        assertThat(jsonNode.at("/fromAddress/addressId").asText()).isEqualTo(fromAddress.getAddressId());
        assertThat(jsonNode.at("/toAddress/addressId").asText()).isEqualTo(toAddress.getAddressId());
        assertThat(jsonNode.get("fromNote").asText()).isEqualTo("Pick up note");
        assertThat(jsonNode.get("toNote").asText()).isEqualTo("Drop off note");
        assertThat(jsonNode.get("pickUpStart").asText()).isEqualTo(ResourceMapper.stringifyDateTime(pickUpStart));
        assertThat(jsonNode.get("pickUpEnd").asText()).isEqualTo(ResourceMapper.stringifyDateTime(pickUpStart.plusMinutes(15)));
        assertThat(jsonNode.get("status").asText()).isEqualTo(PackageStatus.STARTED.name());
        assertThat(jsonNode.get("driverId").asText()).isEqualTo("null");
        assertThat(jsonNode.get("staffNote").asText()).isEqualTo("null");
    }

    @Test
    public void testUpdatePackage_updatesRestrictedFieldsAsStaff() throws Exception {
        // Create business hours for today
        BusinessHour today = new BusinessHour()
                .setWeekDay(TimeUtils.getUtcNow().getDayOfWeek().getValue())
                .setOpenAt(OffsetTime.MIN)
                .setCloseAt(OffsetTime.MAX);
        this.businessHourRepository.save(today);

        Package existingPackage = this.orderFactory.createPackage(customer);
        Contact fromContact = this.orderFactory.createContact(customer.getCustomerId());
        Contact toContact = this.orderFactory.createContact(customer.getCustomerId());
        Address fromAddress = this.orderFactory.createAddress(customer.getCustomerId());
        Address toAddress = this.orderFactory.createAddress(customer.getCustomerId());

        // Request body
        OffsetDateTime pickUpStart = TimeUtils.getUtcNow().plusMinutes(5);
        Map<String, Object> requestBody = Map.ofEntries(
                entry("size", PackageSize.LARGE.name()),
                entry("priority", PackagePriority.EXPRESS.name()),
                entry("fromContact", Map.of(
                        "contactId", fromContact.getContactId()
                )),
                entry("toContact", Map.of(
                        "contactId", toContact.getContactId()
                )),
                entry("fromAddress", Map.of(
                        "addressId", fromAddress.getAddressId()
                )),
                entry("toAddress", Map.of(
                        "addressId", toAddress.getAddressId()
                )),
                entry("fromNote", "Pick up note"),
                entry("toNote", "Drop off note"),
                entry("pickUpStart", pickUpStart.toString()),
                entry("status", PackageStatus.PLACED.name()),
                entry("driverId", driverId),
                entry("staffNote", "Staff note"),
                entry("isAssignable", false),
                entry("pickUpChangeNote", "Customer gone"),
                entry("enableNotifications", false));

        // Update package
        RequestBuilder requestBuilder = patch("/packages/" + existingPackage.getPackageId())
                .contentType("application/json")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJyb2xlcyI6WyJTVEFGRl9TVVBQT1JUIl19.EPsIZ0N9lrCq_Y-zSUoe8frX39AiiR6zjHRhvSltWBI")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate that update happened
        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createPackageMap(this.packageRepository.getById(existingPackage.getPackageId()));
        assertThat(actualBody).isEqualTo(expectedBody);

        JsonNode jsonNode = this.objectMapper.valueToTree(actualBody);
        assertThat(jsonNode.get("size").asText()).isEqualTo(PackageSize.LARGE.name());
        assertThat(jsonNode.get("priority").asText()).isEqualTo(PackagePriority.EXPRESS.name());
        assertThat(jsonNode.at("/fromContact/contactId").asText()).isEqualTo(fromContact.getContactId());
        assertThat(jsonNode.at("/toContact/contactId").asText()).isEqualTo(toContact.getContactId());
        assertThat(jsonNode.at("/fromAddress/addressId").asText()).isEqualTo(fromAddress.getAddressId());
        assertThat(jsonNode.at("/toAddress/addressId").asText()).isEqualTo(toAddress.getAddressId());
        assertThat(jsonNode.get("fromNote").asText()).isEqualTo("Pick up note");
        assertThat(jsonNode.get("toNote").asText()).isEqualTo("Drop off note");
        assertThat(jsonNode.get("pickUpStart").asText()).isEqualTo(ResourceMapper.stringifyDateTime(pickUpStart));
        assertThat(jsonNode.get("pickUpEnd").asText()).isEqualTo(ResourceMapper.stringifyDateTime(pickUpStart.plusMinutes(15)));
        assertThat(jsonNode.get("status").asText()).isEqualTo(PackageStatus.PLACED.name());
        assertThat(jsonNode.get("driverId").asText()).isEqualTo(driverId);
        assertThat(jsonNode.get("staffNote").asText()).isEqualTo("Staff note");
        assertThat(jsonNode.get("isAssignable").asBoolean()).isFalse();
        assertThat(jsonNode.get("pickUpChangeNote").asText()).isEqualTo("Customer gone");
        assertThat(jsonNode.get("enableNotifications").asBoolean()).isFalse();
    }

    @Test
    public void testUpdatePackage_updatesRestrictedFieldsAsService() throws Exception {
        String assignmentId = UUID.randomUUID().toString();

        // Create business hours for today
        BusinessHour today = new BusinessHour()
                .setWeekDay(TimeUtils.getUtcNow().getDayOfWeek().getValue())
                .setOpenAt(OffsetTime.MIN)
                .setCloseAt(OffsetTime.MAX);
        this.businessHourRepository.save(today);

        Package existingPackage = this.orderFactory.createPackage(customer);
        Contact fromContact = this.orderFactory.createContact(customer.getCustomerId());
        Contact toContact = this.orderFactory.createContact(customer.getCustomerId());
        Address fromAddress = this.orderFactory.createAddress(customer.getCustomerId());
        Address toAddress = this.orderFactory.createAddress(customer.getCustomerId());

        // Request body
        OffsetDateTime pickUpStart = TimeUtils.getUtcNow().plusMinutes(5);
        Map<String, Object> requestBody = Map.ofEntries(
                entry("size", PackageSize.LARGE.name()),
                entry("priority", PackagePriority.EXPRESS.name()),
                entry("fromContact", Map.of(
                        "contactId", fromContact.getContactId()
                )),
                entry("toContact", Map.of(
                        "contactId", toContact.getContactId()
                )),
                entry("fromAddress", Map.of(
                        "addressId", fromAddress.getAddressId()
                )),
                entry("toAddress", Map.of(
                        "addressId", toAddress.getAddressId()
                )),
                entry("fromNote", "Pick up note"),
                entry("toNote", "Drop off note"),
                entry("pickUpStart", pickUpStart.toString()),
                entry("status", PackageStatus.PLACED.name()),
                entry("driverId", driverId),
                entry("assignmentId", assignmentId),
                entry("staffNote", "Staff note"),
                entry("isAssignable", false));

        // Update package
        RequestBuilder requestBuilder = patch("/packages/" + existingPackage.getPackageId())
                .contentType("application/json")
                .header("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0LWFwaSIsImlhdCI6MTY0ODk5MzEyNSwiZXhwIjoxNjQ4OTkzMTg1LCJyb2xlcyI6WyJzZXJ2aWNlIl19.m-OuIVt9CD13xlYZ2sA-C6i6AdbG-2gAPa38FNJc7iY")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate that update happened
        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createPackageMap(this.packageRepository.getById(existingPackage.getPackageId()));
        assertThat(actualBody).isEqualTo(expectedBody);

        JsonNode jsonNode = this.objectMapper.valueToTree(actualBody);
        assertThat(jsonNode.get("size").asText()).isEqualTo(PackageSize.LARGE.name());
        assertThat(jsonNode.get("priority").asText()).isEqualTo(PackagePriority.EXPRESS.name());
        assertThat(jsonNode.at("/fromContact/contactId").asText()).isEqualTo(fromContact.getContactId());
        assertThat(jsonNode.at("/toContact/contactId").asText()).isEqualTo(toContact.getContactId());
        assertThat(jsonNode.at("/fromAddress/addressId").asText()).isEqualTo(fromAddress.getAddressId());
        assertThat(jsonNode.at("/toAddress/addressId").asText()).isEqualTo(toAddress.getAddressId());
        assertThat(jsonNode.get("fromNote").asText()).isEqualTo("Pick up note");
        assertThat(jsonNode.get("toNote").asText()).isEqualTo("Drop off note");
        assertThat(jsonNode.get("pickUpStart").asText()).isEqualTo(ResourceMapper.stringifyDateTime(pickUpStart));
        assertThat(jsonNode.get("pickUpEnd").asText()).isEqualTo(ResourceMapper.stringifyDateTime(pickUpStart.plusMinutes(15)));
        assertThat(jsonNode.get("status").asText()).isEqualTo(PackageStatus.PLACED.name());
        assertThat(jsonNode.get("driverId").asText()).isEqualTo(driverId);
        assertThat(jsonNode.get("assignmentId").asText()).isEqualTo(assignmentId);
        assertThat(jsonNode.get("staffNote").asText()).isEqualTo("Staff note");
        assertThat(jsonNode.get("isAssignable").asBoolean()).isFalse();
    }

    @Test
    public void testUpdatePackage_updatesDriverIdAndAssignmentIdAsService() throws Exception {
        // Create business hours for today
        BusinessHour today = new BusinessHour()
                .setWeekDay(TimeUtils.getUtcNow().getDayOfWeek().getValue())
                .setOpenAt(OffsetTime.MIN)
                .setCloseAt(OffsetTime.MAX);
        this.businessHourRepository.save(today);

        Package existingPackage = this.orderFactory.createPackage(customer);
        existingPackage.setDriverId(UUID.randomUUID().toString());
        existingPackage.setAssignmentId(UUID.randomUUID().toString());
        this.packageRepository.save(existingPackage);

        // Request body
        Map<String, Object> requestBody = new NullableValueMapBuilder<String, Object>()
                .put("driverId", null)
                .put("assignmentId", null)
                .build();

        // Update package
        RequestBuilder requestBuilder = patch("/packages/" + existingPackage.getPackageId())
                .contentType("application/json")
                .header("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0LWFwaSIsImlhdCI6MTY0ODk5MzEyNSwiZXhwIjoxNjQ4OTkzMTg1LCJyb2xlcyI6WyJzZXJ2aWNlIl19.m-OuIVt9CD13xlYZ2sA-C6i6AdbG-2gAPa38FNJc7iY")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate that update happened
        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createPackageMap(this.packageRepository.getById(existingPackage.getPackageId()));
        assertThat(actualBody).isEqualTo(expectedBody);

        JsonNode jsonNode = this.objectMapper.valueToTree(actualBody);
        assertThat(jsonNode.get("driverId").asText()).isEqualTo("null");
        assertThat(jsonNode.get("assignmentId").asText()).isEqualTo("null");

        existingPackage = this.packageRepository.getById(existingPackage.getPackageId());
        assertThat(existingPackage.getDriverId()).isNull();
        assertThat(existingPackage.getAssignmentId()).isNull();
    }

    @Test
    public void testUpdatePackage_returnsErrorWhenPickUpIsNotOnTheSameDay() throws Exception {
        // Create business hours for today
        BusinessHour today = new BusinessHour()
                .setWeekDay(TimeUtils.getUtcNow().getDayOfWeek().getValue())
                .setOpenAt(OffsetTime.MIN)
                .setCloseAt(OffsetTime.MAX);
        this.businessHourRepository.save(today);

        // Create package that we will update later
        DeliveryOrder deliveryOrder = this.orderFactory.createOrder(customer);
        Package pkg = ObjectFactory.createRandomPackage(deliveryOrder, this.packageRepository);

        // Request body
        OffsetDateTime pickUpStart = TimeUtils.getUtcNow().minusDays(1); // Earlier than current time
        Map<String, Object> requestBody = Map.of(
                "pickUpStart", pickUpStart.toString()
        );

        // Update package
        RequestBuilder requestBuilder = patch("/packages/" + pkg.getPackageId())
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate that update happened
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Map<String, Object> responseBody = parseResponseBody(result);
        assertThat(responseBody.get("errorMessage")).isEqualTo("Pick-up time must not be in the past");
    }

    @Test
    public void testUpdatePackage_returnsErrorWhenPickUpTimeIsEarlierThanOpenHours() throws Exception {
        // Create business hours for today
        BusinessHour today = new BusinessHour()
                .setWeekDay(TimeUtils.getUtcNow().getDayOfWeek().getValue())
                .setOpenAt(OffsetTime.now().plusHours(2))
                .setCloseAt(OffsetTime.MAX);
        this.businessHourRepository.save(today);

        // Create package that we will update later
        DeliveryOrder deliveryOrder = this.orderFactory.createOrder(customer);
        Package pkg = ObjectFactory.createRandomPackage(deliveryOrder, this.packageRepository);

        // Request body
        OffsetDateTime pickUpStart = TimeUtils.getUtcNow().plusHours(1); // Earlier than current time
        Map<String, Object> requestBody = Map.of(
                "pickUpStart", pickUpStart.toString()
        );

        // Update package
        RequestBuilder requestBuilder = patch("/packages/" + pkg.getPackageId())
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate that update happened
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Map<String, Object> responseBody = parseResponseBody(result);
        assertThat(responseBody.get("errorMessage")).isEqualTo("Pick-up time is earlier than business hours");
    }

    @Test
    public void testUpdatePackage_savesPickUpStartAsNullWhenNotSpecifiedByClient() throws Exception {
        // Create business hours for today
        BusinessHour today = new BusinessHour()
                .setWeekDay(TimeUtils.getUtcNow().getDayOfWeek().getValue())
                .setOpenAt(OffsetTime.MIN)
                .setCloseAt(OffsetTime.MAX);
        this.businessHourRepository.save(today);

        // Create package that we will update later
        DeliveryOrder deliveryOrder = this.orderFactory.createOrder(customer);
        Package pkg = this.orderFactory.createPackage(deliveryOrder, PackageSize.SMALL);

        // Request body
        Map<String, Object> requestBody = new NullableValueMapBuilder<String, Object>()
                // Cannot explicitly set pickUpStart to null (JsonPathMapper)
                .put("size", PackageSize.MEDIUM.name()).build();

        // Update package
        RequestBuilder requestBuilder = patch("/packages/" + pkg.getPackageId())
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate that update happened
        assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());

        Package updatedPkg = this.packageRepository.findById(pkg.getPackageId()).orElse(null);
        assertThat(updatedPkg).isNotNull();
        assertThat(updatedPkg.getPickUpStart()).isNull();
    }

    @Test
    public void testUpdatePackage_returnsErrorWhenPickUpTimeIsLaterThanCloseHours() throws Exception {
        // Create business hours for today
        BusinessHour today = new BusinessHour()
                .setWeekDay(TimeUtils.getUtcNow().getDayOfWeek().getValue())
                .setOpenAt(OffsetTime.MIN)
                .setCloseAt(OffsetTime.now().plusHours(1));
        this.businessHourRepository.save(today);

        // Create package that we will update later
        DeliveryOrder deliveryOrder = this.orderFactory.createOrder(customer);
        Package pkg = ObjectFactory.createRandomPackage(deliveryOrder, this.packageRepository);

        // Request body
        OffsetDateTime pickUpStart = TimeUtils.getUtcNow().plusHours(2); // After close hours
        Map<String, Object> requestBody = Map.of(
                "pickUpStart", pickUpStart.toString()
        );

        // Update package
        RequestBuilder requestBuilder = patch("/packages/" + pkg.getPackageId())
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate that update happened
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Map<String, Object> responseBody = parseResponseBody(result);
        assertThat(responseBody.get("errorMessage")).isEqualTo("Pick-up time must be before our closing time");
    }

    @Test
    public void testUpdatePackage_returns400WhenAddressesAreTheSame() throws Exception {
        // Create business hours for today
        BusinessHour today = new BusinessHour()
                .setWeekDay(TimeUtils.getUtcNow().getDayOfWeek().getValue())
                .setOpenAt(OffsetTime.MIN)
                .setCloseAt(OffsetTime.MAX);
        this.businessHourRepository.save(today);

        Package existingPackage = this.orderFactory.createPackage(customer);
        Address address = this.orderFactory.createAddress(customer.getCustomerId());

        Map<String, Object> requestBody = Map.of(
                "fromAddress", Map.of(
                        "addressId", address.getAddressId()
                ),
                "toAddress", Map.of(
                        "addressId", address.getAddressId()
                )
        );

        // Update package
        RequestBuilder requestBuilder = patch("/packages/" + existingPackage.getPackageId())
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validation
        assertThat(result.getStatus()).isEqualTo(400);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "Use different addresses for pick up and drop off"
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testUpdatePackage_updatesPackageWithRequestStatus() throws Exception {
        // Create business hours for today
        BusinessHour today = new BusinessHour()
                .setWeekDay(TimeUtils.getUtcNow().getDayOfWeek().getValue())
                .setOpenAt(OffsetTime.MIN)
                .setCloseAt(OffsetTime.MAX);
        this.businessHourRepository.save(today);

        // Create package with Request status
        Package existingPackage = this.orderFactory.createPackage(customer, PackageStatus.REQUEST);

        Map<String, Object> requestBody = Map.of(
                "pickUpStart", TimeUtils.getUtcNow().plusMinutes(5).toString()
        );

        // Update package
        RequestBuilder requestBuilder = patch("/packages/" + existingPackage.getPackageId())
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validation
        assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void testUpdatePackage_returnsErrorWhenCustomerUpdatesPlacedPackage() throws Exception {
        // Create business hours for today
        BusinessHour today = new BusinessHour()
                .setWeekDay(TimeUtils.getUtcNow().getDayOfWeek().getValue())
                .setOpenAt(OffsetTime.MIN)
                .setCloseAt(OffsetTime.MAX);
        this.businessHourRepository.save(today);

        // Create package with Placed status
        Package existingPackage = this.orderFactory.createPackage(customer, PackageStatus.PLACED);

        Map<String, Object> requestBody = Map.of(
                "pickUpStart", TimeUtils.getUtcNow().withHour(10).withMinute(30).toString()
        );

        // Update package
        RequestBuilder requestBuilder = patch("/packages/" + existingPackage.getPackageId()) // Non-staff auth token
                .contentType("application/json")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJyb2xlcyI6WyJDVVNUT01FUl9DVVNUT01FUiJdfQ.b2HL1t2fErPvyl5JjLQMcmnZxPz9GAmLEzT9SYrtjeM")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validation
        assertThat(result.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void testGetPackageByPackageId() throws Exception {
        Package existingPackage = this.orderFactory.createPackage(customer);

        RequestBuilder requestBuilder = get("/packages/" + existingPackage.getPackageId());
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createPackageMap(existingPackage);
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testDeletePackage_deletesPackageIfStatusIsUpdatable() throws Exception {
        List<PackageStatus> updatableStatuses = List.of(
                PackageStatus.REQUEST, PackageStatus.STARTED, PackageStatus.PENDING);

        for (PackageStatus packageStatus : updatableStatuses) {
            Package pkg = this.orderFactory.createPackage(customer, packageStatus);
            Charge charge = this.orderFactory.createCharge(pkg, ChargeStatus.UNPAID);

            RequestBuilder requestBuilder = delete("/packages/" + pkg.getPackageId());
            MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

            assertThat(result.getStatus()).isEqualTo(204);
            assertThat(this.packageRepository.findById(pkg.getPackageId()).isEmpty()).isTrue();
            assertThat(this.chargeRepository.findById(charge.getChargeId()).isEmpty()).isTrue();
        }
    }

    @Test
    public void testDeletePackage_returnsErrorIfStatusIsNotUpdatable() throws Exception {
        List<PackageStatus> nonUpdatableStatuses = List.of(
                PackageStatus.PLACED, PackageStatus.COMPLETE, PackageStatus.CANCELED, PackageStatus.INCOMPLETE);

        for (PackageStatus packageStatus : nonUpdatableStatuses) {
            Package pkg = this.orderFactory.createPackage(customer, packageStatus);
            DeliveryOrder order = pkg.getDeliveryOrder();

            RequestBuilder requestBuilder = delete("/packages/" + pkg.getPackageId());
            MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

            assertThat(result.getStatus()).isEqualTo(403);
            Map<String, Object> actualBody = parseResponseBody(result);
            Map<String, Object> expectedBody = Map.of(
                    "errorCode", "AUTHORIZATION_ERROR",
                    "errorMessage", "You cannot delete this package (already placed)"
            );
            assertThat(actualBody).isEqualTo(expectedBody);
        }
    }

    @Test
    public void testCancelPackage_cancelsPlacedPackageWithAssignment() throws Exception {
        String assignmentId = UUID.randomUUID().toString();
        Package pkg = this.orderFactory.createPackage(customer, PackageStatus.PLACED);
        this.packageRepository.save(pkg.setAssignmentId(assignmentId));
        this.orderFactory.createPackageEvent(pkg, EventName.DRIVER_DEPARTING_DROP_OFF, assignmentId); // Needed to send cancelled package buyer SMS
        DeliveryOrder order = pkg.getDeliveryOrder();
        Map<String, Object> requestBody = Map.of(
                "note", "cancel package"
        );

        RequestBuilder requestBuilder = post("/packages/" + pkg.getPackageId() + "/cancellation")
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(204);

        // Validate order
        order = this.orderRepository.getById(order.getDeliveryOrderId());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);

        // Validate package
        pkg = this.packageRepository.getById(pkg.getPackageId());
        assertThat(pkg.getStatus()).isEqualTo(PackageStatus.CANCELED);
        assertThat(pkg.getCancellationNote()).isEqualTo("cancel package");

        // Validate created event
        assertThat(this.packageEventRepository.findFirstByPkgAndEventName(pkg, EventName.PACKAGE_CANCELLED)).isNotNull();

        // Validate delivery-api call
        ArgumentCaptor<Map> deliveryApiCaptor = ArgumentCaptor.forClass(Map.class);
        verify(this.httpClientWrapper).post(eq(String.format("null/current-assignments/%s/cancellation", assignmentId)), deliveryApiCaptor.capture());
        Map<String, Object> actualCancellationPayload = (Map<String, Object>) deliveryApiCaptor.getValue();
        Map<String, Object> expectedCancellationPayload = Map.of();
        assertThat(actualCancellationPayload).isEqualTo(expectedCancellationPayload);

        // Validate cancellation SMS to buyer
        ArgumentCaptor<Map> communicationApiCaptor = ArgumentCaptor.forClass(Map.class);
        verify(this.httpClientWrapper).post(eq("null/sms"), communicationApiCaptor.capture());
        Map<String, Object> actualSMSPayload = (Map<String, Object>) communicationApiCaptor.getValue();
        Map<String, Object> expectedSMSPayload = Map.of(
                "message", String.format(
                        "Your package from %s (%s) is cancelled.",
                        customer.getBusinessName(), PhoneNumberUtils.localize(pkg.getFromContact().getPhoneNumberOne())),
                "phoneNumbers", List.of(pkg.getToContact().getPhoneNumberOne()),
                "serviceName", "DELIVERY_NOTIFICATION",
                "isUnicode", false
        );
        assertThat(actualSMSPayload).isEqualTo(expectedSMSPayload);
    }

    @Test
    public void testCancelPackage_cancelsPlacedPackageWithoutAssignment() throws Exception {
        Package pkg = this.orderFactory.createPackage(customer, PackageStatus.PLACED);
        DeliveryOrder order = pkg.getDeliveryOrder();
        Map<String, Object> requestBody = Map.of(
                "note", "cancel package"
        );

        RequestBuilder requestBuilder = post("/packages/" + pkg.getPackageId() + "/cancellation")
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(204);

        // Validate order
        order = this.orderRepository.getById(order.getDeliveryOrderId());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);

        // Validate package
        pkg = this.packageRepository.getById(pkg.getPackageId());
        assertThat(pkg.getStatus()).isEqualTo(PackageStatus.CANCELED);
        assertThat(pkg.getCancellationNote()).isEqualTo("cancel package");

        // Validate events
        List<PackageEvent> events = this.packageEventRepository.findByPkg(pkg);
        assertThat(events.size()).isEqualTo(1);
        assertThat(events.get(0).getEventName()).isEqualTo(EventName.PACKAGE_CANCELLED);

        // No cancellation SMS to buyer
        verify(this.httpClientWrapper, never()).post(eq("null/sms"), anyMap());
    }

    @Test
    public void testCancelPackage_cancelsNonPlacedPackageWithTransitionalStatus() throws Exception {
        List<PackageStatus> transitionalStatuses = List.of(
                PackageStatus.REQUEST, PackageStatus.STARTED, PackageStatus.PENDING);

        for (PackageStatus packageStatus : transitionalStatuses) {
            Package pkg = this.orderFactory.createPackage(customer, packageStatus);
            DeliveryOrder order = pkg.getDeliveryOrder();
            Map<String, Object> requestBody = Map.of(
                    "note", "cancel package"
            );

            RequestBuilder requestBuilder = post("/packages/" + pkg.getPackageId() + "/cancellation")
                    .contentType("application/json")
                    .content(stringifyRequestBody(requestBody));
            MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
            assertThat(result.getStatus()).isEqualTo(204);

            // Validate order
            order = this.orderRepository.getById(order.getDeliveryOrderId());
            assertThat(order.getStatus()).isEqualTo(OrderStatus.INCOMPLETE);

            // Validate package
            pkg = this.packageRepository.getById(pkg.getPackageId());
            assertThat(pkg.getStatus()).isEqualTo(PackageStatus.INCOMPLETE);
            assertThat(pkg.getCancellationNote()).isEqualTo("cancel package");
        }
    }

    @Test
    public void testCancelPackage_returnsErrorIfPackageHasNonTransitionalStatus() throws Exception {
        List<PackageStatus> finalStatuses = List.of(
                PackageStatus.COMPLETE, PackageStatus.CANCELED, PackageStatus.INCOMPLETE);

        for (PackageStatus packageStatus : finalStatuses) {
            Package pkg = this.orderFactory.createPackage(customer, packageStatus);
            Map<String, Object> requestBody = Map.of(
                    "note", "cancel package"
            );

            RequestBuilder requestBuilder = post("/packages/" + pkg.getPackageId() + "/cancellation")
                    .contentType("application/json")
                    .content(stringifyRequestBody(requestBody));
            MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

            assertThat(result.getStatus()).isEqualTo(400);
            Map<String, Object> actualBody = parseResponseBody(result);
            Map<String, Object> expectedBody = Map.of(
                    "errorCode", "INVALID_REQUEST",
                    "errorMessage", "You can not cancel this package"
            );
            assertThat(actualBody).isEqualTo(expectedBody);
        }
    }

    @Test
    public void testCancelPackage_returnsErrorIfNoteIsMissing() throws Exception {
        DeliveryOrder order = this.orderFactory.createOrder(customer, OrderStatus.REQUEST);
        Package pkg = this.orderFactory.createPackage(order, PackageSize.SMALL);

        RequestBuilder requestBuilder = post("/packages/" + pkg.getPackageId() + "/cancellation")
                .contentType("application/json")
                .content(stringifyRequestBody(Map.of()));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(400);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "Cancellation reason is required"
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testPackageTracking() throws Exception {
        Package existingPackage = this.orderFactory.createPackage(customer);

        RequestBuilder requestBuilder = get("/package-tracking/" + existingPackage.getTrackingNumber());
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createPackageMap(existingPackage);
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testPackageTracking_returnsErrorIfPackageNotFound() throws Exception {
        RequestBuilder requestBuilder = get("/package-tracking/12345678901");
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(400);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "Package not found"
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }
}
