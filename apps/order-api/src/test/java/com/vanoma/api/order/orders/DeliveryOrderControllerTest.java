package com.vanoma.api.order.orders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vanoma.api.order.businesshours.BusinessHourRepository;
import com.vanoma.api.order.charges.Charge;
import com.vanoma.api.order.charges.ChargeRepository;
import com.vanoma.api.order.charges.ChargeStatus;
import com.vanoma.api.order.charges.ChargeType;
import com.vanoma.api.order.contacts.*;
import com.vanoma.api.order.customers.Agent;
import com.vanoma.api.order.customers.Branch;
import com.vanoma.api.order.customers.Customer;
import com.vanoma.api.order.events.PackageEventRepository;
import com.vanoma.api.order.maps.Coordinates;
import com.vanoma.api.order.maps.IGeocodingService;
import com.vanoma.api.order.maps.INavigationDistanceApi;
import com.vanoma.api.order.maps.KigaliDistrict;
import com.vanoma.api.order.packages.*;
import com.vanoma.api.order.packages.Package;
import com.vanoma.api.order.pricing.CustomPricing;
import com.vanoma.api.order.pricing.CustomPricingRepository;
import com.vanoma.api.order.tests.ObjectFactory;
import com.vanoma.api.order.tests.OrderFactory;
import com.vanoma.api.order.tests.ResourceMapper;
import com.vanoma.api.order.tests.TimeTestUtils;
import com.vanoma.api.utils.NullableListBuilder;
import com.vanoma.api.utils.NullableValueMapBuilder;
import com.vanoma.api.utils.httpwrapper.HttpResult;
import com.vanoma.api.utils.httpwrapper.IHttpClientWrapper;
import com.vanoma.api.utils.input.TimeUtils;
import org.json.JSONObject;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.*;

import static com.vanoma.api.order.tests.ControllerTestUtils.parseResponseBody;
import static com.vanoma.api.order.tests.ControllerTestUtils.stringifyRequestBody;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class DeliveryOrderControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ChargeRepository chargeRepository;
    @Autowired
    private PackageRepository packageRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private ContactAddressRepository contactAddressRepository;
    @MockBean
    private IHttpClientWrapper httpClientMock;
    @MockBean
    private IGeocodingService geocodingService;
    @Autowired
    private OrderFactory orderFactory;
    @Autowired
    private CustomPricingRepository customPricingRepository;
    @Autowired
    private PackageEventRepository packageEventRepository;
    @Autowired
    private BusinessHourRepository businessHourRepository;
    @MockBean
    private INavigationDistanceApi navigationDistanceApi;

    @Test
    public void testCreateDeliveryOrder_asWebUser_createsOrderOnly() throws Exception {
        Customer customer = this.orderFactory.createCustomer();
        Map<String, Object> requestBody = Map.of(
                "packages", List.of(Map.of( // Including packages in a no-op.
                        "size", PackageSize.LARGE.name(),
                        "priority", PackagePriority.EXPRESS.name()
                ))
        );

        RequestBuilder requestBuilder = post("/customers/" + customer.getCustomerId() + "/delivery-orders")
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        DeliveryOrder order = this.orderRepository.findByCustomer(customer).get(0);
        assertThat(this.packageRepository.findByDeliveryOrder(order).size()).isEqualTo(0);

        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createDeliveryOrderMap(order);
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testCreateDeliveryOrder_asWebUser_doesNotRequireRequestBody() throws Exception {
        Customer customer = this.orderFactory.createCustomer();
        RequestBuilder requestBuilder = post("/customers/" + customer.getCustomerId() + "/delivery-orders")
                .contentType("application/json");

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        DeliveryOrder order = this.orderRepository.findByCustomer(customer).get(0);
        assertThat(this.packageRepository.findByDeliveryOrder(order).size()).isEqualTo(0);

        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createDeliveryOrderMap(order);
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testCreateDeliveryOrder_asApiUser_returnsErorWhenRequestBodyIsMissing() throws Exception {
        Customer customer = this.orderFactory.createCustomer();
        RequestBuilder requestBuilder = post("/customers/" + customer.getCustomerId() + "/delivery-orders")
                .contentType("application/json")
                .header("X-Access-Key", "5b2a34267dba613f7f9aef3a16e1e194f6fe4d3e8bfed24dcc2fc8c2ee2fdc9a");

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "Delivery must have packages"
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testCreateDeliveryOrder_asApiUser_returnsErrorWhenPickUpTimeIsBeyond48Hours() throws Exception {
        // Create business hour for "today"
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Request body
        Customer customer = this.orderFactory.createCustomer();
        Map<String, Object> requestBody = Map.of(
                "packages", List.of(Map.of(
                        "size", PackageSize.LARGE.name(),
                        "priority", PackagePriority.EXPRESS.name(),
                        "fromContact", Map.of(
                                "phoneNumberOne", "250788224455"
                        ),
                        "toContact", Map.of(
                                "phoneNumberOne", "250788221133"
                        ),
                        "fromAddress", Map.of(
                                "addressId", UUID.randomUUID().toString()
                        ),
                        "pickUpStart", TimeUtils.getUtcNow().plusHours(50).toString()
                ))
        );

        RequestBuilder requestBuilder = post("/customers/" + customer.getCustomerId() + "/delivery-orders")
                .contentType("application/json")
                .header("X-Access-Key", "5b2a34267dba613f7f9aef3a16e1e194f6fe4d3e8bfed24dcc2fc8c2ee2fdc9a")
                .content(new JSONObject(requestBody).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Map<String, Object> responseBody = parseResponseBody(result);
        assertThat(responseBody.get("errorMessage")).isEqualTo("Pick-up time must be within 48 hours");
    }

    @Test
    public void testCreateDeliveryOrder_asApiUser_returnsErrorWhenPickUpTimeIsEarlierThanCurrentTime() throws Exception {
        // Create business hours for today
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Request body
        Customer customer = this.orderFactory.createCustomer();
        Map<String, Object> requestBody = Map.of(
                "packages", List.of(Map.of(
                        "size", PackageSize.LARGE.name(),
                        "priority", PackagePriority.EXPRESS.name(),
                        "fromContact", Map.of(
                                "phoneNumberOne", "250788224455"
                        ),
                        "toContact", Map.of(
                                "phoneNumberOne", "250788221133"
                        ),
                        "fromAddress", Map.of(
                                "addressId", UUID.randomUUID().toString()
                        ),
                        "pickUpStart", TimeUtils.getUtcNow().minusHours(1).toString() // Earlier than current time
                ))
        );


        RequestBuilder requestBuilder = post("/customers/" + customer.getCustomerId() + "/delivery-orders")
                .contentType("application/json")
                .header("X-Access-Key", "5b2a34267dba613f7f9aef3a16e1e194f6fe4d3e8bfed24dcc2fc8c2ee2fdc9a")
                .content(new JSONObject(requestBody).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Map<String, Object> responseBody = parseResponseBody(result);
        assertThat(responseBody.get("errorMessage")).isEqualTo("Pick-up time must not be in the past");
    }

    @Test
    public void testCreateDeliveryOrder_asApiUser_returnsErrorWhenPickUpTimeIsBeforeOpenHours() throws Exception {
        // Create business hours for today
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.now().plusHours(2), OffsetTime.MAX);

        // Request body
        Customer customer = this.orderFactory.createCustomer();
        Map<String, Object> requestBody = Map.of(
                "packages", List.of(Map.of(
                        "size", PackageSize.LARGE.name(),
                        "priority", PackagePriority.EXPRESS.name(),
                        "fromContact", Map.of(
                                "phoneNumberOne", "250788224455"
                        ),
                        "toContact", Map.of(
                                "phoneNumberOne", "250788221133"
                        ),
                        "fromAddress", Map.of(
                                "addressId", UUID.randomUUID().toString()
                        ),
                        "pickUpStart", TimeUtils.getUtcNow().plusHours(1).toString() // Before work hours
                ))
        );

        RequestBuilder requestBuilder = post("/customers/" + customer.getCustomerId() + "/delivery-orders")
                .contentType("application/json")
                .header("X-Access-Key", "5b2a34267dba613f7f9aef3a16e1e194f6fe4d3e8bfed24dcc2fc8c2ee2fdc9a")
                .content(new JSONObject(requestBody).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Map<String, Object> responseBody = parseResponseBody(result);
        assertThat(responseBody.get("errorMessage")).isEqualTo("Pick-up time is earlier than business hours");
    }

    @Test
    public void testCreateDeliveryOrder_asApiUser_returnsErrorWhenPickUpTimeIsAfterClosingHours() throws Exception {
        // Create business hours for today
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.now().plusHours(1));

        // Request body
        Customer customer = this.orderFactory.createCustomer();
        Map<String, Object> requestBody = Map.of(
                "packages", List.of(Map.of(
                        "size", PackageSize.LARGE.name(),
                        "priority", PackagePriority.EXPRESS.name(),
                        "fromContact", Map.of(
                                "phoneNumberOne", "250788224455"
                        ),
                        "toContact", Map.of(
                                "phoneNumberOne", "250788221133"
                        ),
                        "fromAddress", Map.of(
                                "addressId", UUID.randomUUID().toString()
                        ),
                        "pickUpStart", TimeUtils.getUtcNow().plusHours(2).toString() // After work hours
                ))
        );

        RequestBuilder requestBuilder = post("/customers/" + customer.getCustomerId() + "/delivery-orders")
                .contentType("application/json")
                .header("X-Access-Key", "5b2a34267dba613f7f9aef3a16e1e194f6fe4d3e8bfed24dcc2fc8c2ee2fdc9a")
                .content(new JSONObject(requestBody).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Map<String, Object> responseBody = parseResponseBody(result);
        assertThat(responseBody.get("errorMessage")).isEqualTo("Pick-up time must be before our closing time");
    }

    @Test
    public void testCreateDeliveryOrder_asApiUser_returnsErrorForNonExistingContact() throws Exception {
        // Create business hours for today
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Creating test data
        Customer customer = this.orderFactory.createCustomer();
        Contact fromContact = this.orderFactory.createContact(customer.getCustomerId());
        Address fromAddress = this.orderFactory.createAddress(customer.getCustomerId());
        Address toAddress = this.orderFactory.createAddress(customer.getCustomerId());

        Map<String, Object> requestBody = Map.of(
                "packages", List.of(Map.of(
                        "size", PackageSize.LARGE.name(),
                        "priority", PackagePriority.EXPRESS.name(),
                        "fromContact", Map.of(
                                "contactId", fromContact.getContactId()
                        ),
                        "toContact", Map.of(
                                "contactId", UUID.randomUUID().toString()
                        ),
                        "fromAddress", Map.of(
                                "addressId", fromAddress.getAddressId()
                        ),
                        "toAddress", Map.of(
                                "addressId", toAddress.getAddressId()
                        )
                ))
        );

        RequestBuilder requestBuilder = post("/customers/" + customer.getCustomerId() + "/delivery-orders")
                .contentType("application/json")
                .header("X-Access-Key", "5b2a34267dba613f7f9aef3a16e1e194f6fe4d3e8bfed24dcc2fc8c2ee2fdc9a")
                .content(new JSONObject(requestBody).toString());
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "RESOURCE_NOT_FOUND",
                "errorMessage", "Contact not found"
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testCreateDeliveryOrder_asApiUser_returnsErrorForNonExistentAddress() throws Exception {
        // Create business hours for today
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Creating test data
        Customer customer = this.orderFactory.createCustomer();
        Contact fromContact = this.orderFactory.createContact(customer.getCustomerId());
        Contact toContact = this.orderFactory.createContact(customer.getCustomerId());
        Address fromAddress = this.orderFactory.createAddress(customer.getCustomerId());

        Map<String, Object> requestBody = Map.of(
                "packages", List.of(Map.of(
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
                                "addressId", UUID.randomUUID().toString()
                        )
                ))
        );

        RequestBuilder requestBuilder = post("/customers/" + customer.getCustomerId() + "/delivery-orders")
                .contentType("application/json")
                .header("X-Access-Key", "5b2a34267dba613f7f9aef3a16e1e194f6fe4d3e8bfed24dcc2fc8c2ee2fdc9a")
                .content(new JSONObject(requestBody).toString());
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "RESOURCE_NOT_FOUND",
                "errorMessage", "Address not found"
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testCreateDeliveryOrder_asApiUser_createsOrReusesExistingContactAndAddress() throws Exception {
        // Create business hours for today
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Create existing contact & address
        Customer customer = this.orderFactory.createCustomer();
        Contact fromContact = this.orderFactory.createContact(customer.getCustomerId());
        Address fromAddress = this.orderFactory.createAddress(customer.getCustomerId());

        Map<String, Object> requestBody = Map.of(
                "packages", List.of(Map.of(
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
                                "addressName", "Test address"
                        ),
                        "pickUpStart", TimeUtils.getUtcNow().plusHours(1).toString()
                ))
        );

        when(this.httpClientMock.post(any(String.class), any(Map.class)))
                .thenReturn(new HttpResult(HttpStatus.OK.value()));

        RequestBuilder requestBuilder = post("/customers/" + customer.getCustomerId() + "/delivery-orders")
                .contentType("application/json")
                .header("X-Access-Key", "5b2a34267dba613f7f9aef3a16e1e194f6fe4d3e8bfed24dcc2fc8c2ee2fdc9a")
                .content(new JSONObject(requestBody).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        // Validate reusing existing contact & address
        DeliveryOrder order = this.orderRepository.findByCustomer(customer).get(0);
        Package pkg = this.packageRepository.findByDeliveryOrder(order).get(0);
        assertThat(pkg.getFromContact().getPhoneNumberOne()).isEqualTo(fromContact.getPhoneNumberOne());
        assertThat(pkg.getFromAddress().getLatitude()).isEqualTo(fromAddress.getLatitude());
        assertThat(pkg.getFromAddress().getLongitude()).isEqualTo(fromAddress.getLongitude());
        assertThat(pkg.getToContact().getPhoneNumberOne()).isEqualTo("250788221133");
        assertThat(pkg.getToAddress().getLatitude()).isEqualTo(-1.94995);
        assertThat(pkg.getToAddress().getLongitude()).isEqualTo(30.05885);

        // Validate order placement
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(pkg.getStatus()).isEqualTo(PackageStatus.PLACED);

        // Validate response
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createDeliveryOrderMap(order, true);
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testCreateDeliveryOrder_asApiUser_acceptsAddressWithOnlyLatitudeAndLongitude() throws Exception {
        // Create business hours for today
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Create existing contact & address
        Customer customer = this.orderFactory.createCustomer();
        Contact fromContact = this.orderFactory.createContact(customer.getCustomerId());
        Address fromAddress = this.orderFactory.createAddress(customer.getCustomerId());

        Map<String, Object> requestBody = Map.of(
                "packages", List.of(Map.of(
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
                                "latitude", -1.94995,
                                "longitude", 30.05885,
                                "addressName", "Test address"
                        ),
                        "pickUpStart", TimeUtils.getUtcNow().plusHours(1).toString()
                ))
        );

        when(this.geocodingService.reverseGeocode(any(Coordinates.class)))
                .thenReturn(new HttpResult(Map.of(
                        "district", KigaliDistrict.GASABO.name(),
                        "latitude", -1.94995,
                        "longitude", 30.05885
                ), HttpStatus.OK.value()));
        when(this.httpClientMock.post(any(String.class), any(Map.class)))
                .thenReturn(new HttpResult(HttpStatus.OK.value()));

        RequestBuilder requestBuilder = post("/customers/" + customer.getCustomerId() + "/delivery-orders")
                .contentType("application/json")
                .header("X-Access-Key", "5b2a34267dba613f7f9aef3a16e1e194f6fe4d3e8bfed24dcc2fc8c2ee2fdc9a")
                .content(new JSONObject(requestBody).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        // Validate reverse mapping
        DeliveryOrder order = this.orderRepository.findByCustomer(customer).get(0);
        Package pkg = this.packageRepository.findByDeliveryOrder(order).get(0);
        assertThat(pkg.getToAddress().getDistrict()).isEqualTo(KigaliDistrict.GASABO);
        assertThat(pkg.getToAddress().getLatitude()).isEqualTo(-1.94995);
        assertThat(pkg.getToAddress().getLongitude()).isEqualTo(30.05885);

        // Validate order placement
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(pkg.getStatus()).isEqualTo(PackageStatus.PLACED);

        // Validate response
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createDeliveryOrderMap(order, true);
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testCreateDeliveryOrder_asApiUser_usesClientProvidedPickUpStart() throws Exception {
        // Create business hours for today
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Create contact & address
        Customer customer = this.orderFactory.createCustomer();
        Contact fromContact = this.orderFactory.createContact(customer.getCustomerId());
        Address fromAddress = this.orderFactory.createAddress(customer.getCustomerId());
        Contact toContact = this.orderFactory.createContact(customer.getCustomerId());
        Address toAddress = this.orderFactory.createAddress(customer.getCustomerId());

        OffsetDateTime pickUpStart = TimeUtils.getUtcNow().plusHours(1);
        Map<String, Object> requestBody = Map.of(
                "packages", List.of(Map.of(
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
                ))
        );

        when(this.httpClientMock.post(any(String.class), any(Map.class)))
                .thenReturn(new HttpResult(HttpStatus.OK.value()));

        RequestBuilder requestBuilder = post("/customers/" + customer.getCustomerId() + "/delivery-orders")
                .contentType("application/json")
                .header("X-Access-Key", "5b2a34267dba613f7f9aef3a16e1e194f6fe4d3e8bfed24dcc2fc8c2ee2fdc9a")
                .content(new JSONObject(requestBody).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        // Validate pickUpStart
        DeliveryOrder order = this.orderRepository.findByCustomer(customer).get(0);
        Package pkg = this.packageRepository.findByDeliveryOrder(order).get(0);
        assertThat(pkg.getPickUpStart()).isEqualTo(pickUpStart);
        assertThat(pkg.getPickUpEnd()).isEqualTo(pickUpStart.plusMinutes(15));

        // Validate order placement
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(pkg.getStatus()).isEqualTo(PackageStatus.PLACED);

        // Validate response
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createDeliveryOrderMap(order, true);
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testCreateDeliveryOrder_asApiUser_setsPickUpStartIfClientValueIsNull() throws Exception {
        // Create business hours for today
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Create contact & address
        Customer customer = this.orderFactory.createCustomer();
        Contact fromContact = this.orderFactory.createContact(customer.getCustomerId());
        Address fromAddress = this.orderFactory.createAddress(customer.getCustomerId());
        Contact toContact = this.orderFactory.createContact(customer.getCustomerId());
        Address toAddress = this.orderFactory.createAddress(customer.getCustomerId());

        Map<String, Object> requestBody = Map.of(
                "packages", List.of(Map.of(
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
                ))
        );

        when(this.httpClientMock.post(any(String.class), any(Map.class)))
                .thenReturn(new HttpResult(HttpStatus.OK.value()));

        RequestBuilder requestBuilder = post("/customers/" + customer.getCustomerId() + "/delivery-orders")
                .contentType("application/json")
                .header("X-Access-Key", "5b2a34267dba613f7f9aef3a16e1e194f6fe4d3e8bfed24dcc2fc8c2ee2fdc9a")
                .content(new JSONObject(requestBody).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        // Validate pickUpStart
        DeliveryOrder order = this.orderRepository.findByCustomer(customer).get(0);
        Package pkg = this.packageRepository.findByDeliveryOrder(order).get(0);
        assertThat(pkg.getPickUpStart()).isNotNull();
        assertThat(pkg.getPickUpEnd()).isEqualTo(pkg.getPickUpStart().plusMinutes(15));

        // Validate order placement
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(pkg.getStatus()).isEqualTo(PackageStatus.PLACED);

        // Validate response
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createDeliveryOrderMap(order, true);
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetDeliveryOrder_returnsFullOrderJsonObject() throws Exception {
        Customer customer = this.orderFactory.createCustomer();
        DeliveryOrder deliveryOrder = this.orderFactory.createOrderWithPackage(customer, PackageSize.SMALL);

        RequestBuilder requestBuilder = get("/delivery-orders" + "/" + deliveryOrder.getDeliveryOrderId());
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());
        Map<String, Object> actual = parseResponseBody(result);
        Map<String, Object> expected = ResourceMapper.createDeliveryOrderMap(deliveryOrder);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testPrePlaceDeliveryOrder_updateOrderAndPackageStatusToPending() throws Exception {
        // Create order
        Customer customer = this.orderFactory.createCustomer();
        DeliveryOrder deliveryOrder = this.orderFactory.createOrder(customer, OrderStatus.REQUEST);
        Package pkg = this.orderFactory.createPackage(deliveryOrder, PackageStatus.REQUEST, PackageSize.SMALL);

        // Create delivery fee charge (required)
        Charge deliveryFee = this.orderFactory.createCharge(pkg, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID, new BigDecimal("1000"));

        // Mock Communication API calls
        when(this.httpClientMock.post(any(String.class), any(Map.class)))
                .thenReturn(new HttpResult(Map.of(), HttpStatus.OK.value()));

        // Pre-place the order
        RequestBuilder requestBuilder = post("/delivery-orders/" + deliveryOrder.getDeliveryOrderId() + "/pre-placement")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJyb2xlcyI6WyJDVVNUT01FUl9DVVNUT01FUiJdfQ.b2HL1t2fErPvyl5JjLQMcmnZxPz9GAmLEzT9SYrtjeM")
                .content(new JSONObject(new HashMap<>()).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());

        // Validate status changes
        DeliveryOrder updatedOrder = this.orderRepository.getById(deliveryOrder.getDeliveryOrderId());
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        Package updatePackage = this.packageRepository.getById(pkg.getPackageId());
        assertThat(updatePackage.getStatus()).isEqualTo(PackageStatus.PENDING);

        // Validate SMS payload
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(this.httpClientMock, times(1)).post(eq("null/sms"), captor.capture());
        Map<String, Object> actualSmsPayload = captor.getValue();
        Map<String, Object> expectedSmsPayload = Map.of(
                "isUnicode", false,
                "phoneNumbers", List.of(customer.getPhoneNumber()),
                "serviceName", "DELIVERY_NOTIFICATION",
                "message", "A customer just provided their address. Please pay for the delivery here: null/deliveries/request. Call 8080 (Toll-Free) for any questions."
        );
        assertThat(actualSmsPayload).isEqualTo(expectedSmsPayload);
    }

    @Test
    public void testPlaceDeliveryOrder_returnsErrorWhenAccountIsPrepaidAndRequesterIsNotStaff() throws Exception {
        Customer customer = this.orderFactory.createCustomer();
        DeliveryOrder deliveryOrder = this.orderFactory.createOrder(customer, OrderStatus.STARTED);
        this.orderRepository.save(deliveryOrder);

        RequestBuilder requestBuilder = post("/delivery-orders/" + deliveryOrder.getDeliveryOrderId() + "/placement")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJyb2xlcyI6WyJDVVNUT01FUl9DVVNUT01FUiJdfQ.b2HL1t2fErPvyl5JjLQMcmnZxPz9GAmLEzT9SYrtjeM")
                .content(new JSONObject(new HashMap<>()).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        Map<String, Object> response = parseResponseBody(result);
        assertThat(response.get("errorMessage")).isEqualTo("Your account is not authorized to place this order");
    }

    @Test
    public void testPlaceDeliveryOrder_returnsErrorWhenPostpaidStatusExpiredAndRequesterIsNotStaff() throws Exception {
        Customer customer = this.orderFactory.createCustomer(TimeUtils.getUtcNow().minusDays(1));
        DeliveryOrder deliveryOrder = this.orderFactory.createOrderWithPackage(customer, OrderStatus.STARTED, PackageSize.SMALL);

        RequestBuilder requestBuilder = post("/delivery-orders/" + deliveryOrder.getDeliveryOrderId() + "/placement")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJyb2xlcyI6WyJDVVNUT01FUl9DVVNUT01FUiJdfQ.b2HL1t2fErPvyl5JjLQMcmnZxPz9GAmLEzT9SYrtjeM")
                .content(new JSONObject(new HashMap<>()).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        Map<String, Object> response = parseResponseBody(result);
        assertThat(response.get("errorMessage")).isEqualTo("Your account is not authorized to place this order");
    }

    @Test
    public void testPlaceDeliveryOrder_returnsErrorWhenCustomPricingIsNotPostpaidAndRequesterIsNotStaff() throws Exception {
        Customer customer = this.orderFactory.createCustomer();
        CustomPricing customPricing = new CustomPricing(customer.getCustomerId())
                .setCustomerName("ABC")
                .setExpireAt(TimeUtils.getUtcNow().minusDays(1));
        this.customPricingRepository.save(customPricing);

        DeliveryOrder deliveryOrder = this.orderFactory.createOrder(customer, OrderStatus.STARTED);
        this.orderRepository.save(deliveryOrder);

        RequestBuilder requestBuilder = post("/delivery-orders/" + deliveryOrder.getDeliveryOrderId() + "/placement")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJyb2xlcyI6WyJDVVNUT01FUl9DVVNUT01FUiJdfQ.b2HL1t2fErPvyl5JjLQMcmnZxPz9GAmLEzT9SYrtjeM")
                .content(new JSONObject(new HashMap<>()).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        Map<String, Object> response = parseResponseBody(result);
        assertThat(response.get("errorMessage")).isEqualTo("Your account is not authorized to place this order");
    }

    @Test
    public void testPlaceDeliveryOrder_placesOrderWhenRequesterIsStaffDespiteBeingPrepaidAccount() throws Exception {
        // Create business hour for "today"
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        Customer customer = this.orderFactory.createCustomer();
        CustomPricing customPricing = new CustomPricing(customer.getCustomerId())
                .setCustomerName("ABC")
                .setExpireAt(TimeUtils.getUtcNow().plusDays(1));
        this.customPricingRepository.save(customPricing);

        DeliveryOrder deliveryOrder = this.orderFactory.createOrderWithPackage(customer, PackageSize.SMALL);
        Package pkg = deliveryOrder.getPackages().stream().findFirst().get();
        Charge deliveryFee = this.orderFactory.createCharge(pkg, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID, new BigDecimal("1000"));

        // JWT token includes "STAFF_..." role.
        RequestBuilder requestBuilder = post("/delivery-orders/" + deliveryOrder.getDeliveryOrderId() + "/placement")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJyb2xlcyI6WyJTVEFGRl9TVVBQT1JUIl19.EPsIZ0N9lrCq_Y-zSUoe8frX39AiiR6zjHRhvSltWBI")
                .content(new JSONObject(new HashMap<>()).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void testPlaceDeliveryOrder_placesOrderWhenAccountIsPostpaid() throws Exception {
        // Create business hour for "today"
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        Customer customer = this.orderFactory.createCustomer(TimeUtils.getUtcNow().plusDays(1));
        CustomPricing customPricing = new CustomPricing(customer.getCustomerId())
                .setCustomerName("ABC")
                .setExpireAt(TimeUtils.getUtcNow().plusDays(1));
        this.customPricingRepository.save(customPricing);

        DeliveryOrder deliveryOrder = this.orderFactory.createOrderWithPackage(customer, PackageSize.SMALL);
        Package pkg = deliveryOrder.getPackages().stream().findFirst().get();
        Charge deliveryFee = this.orderFactory.createCharge(pkg, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID, new BigDecimal("1000"));

        RequestBuilder requestBuilder = post("/delivery-orders/" + deliveryOrder.getDeliveryOrderId() + "/placement")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJyb2xlcyI6WyJDVVNUT01FUl9DVVNUT01FUiJdfQ.b2HL1t2fErPvyl5JjLQMcmnZxPz9GAmLEzT9SYrtjeM")
                .content(new JSONObject(new HashMap<>()).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void testDuplicateDeliveryOrder_createsAndPlaceNewDeliveryOrderWithTheSamePickUpAndDropOffInformation() throws Exception {
        // Create business hour for "today"
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Create order with STARTED status
        DeliveryOrder oldOrder = this.orderFactory.createOrderWithPackage(OrderStatus.STARTED);
        Package oldPkg = this.packageRepository.findByDeliveryOrder(oldOrder).get(0);
        oldPkg.setFromNote("Pick-up instructions");
        oldPkg.setToNote("Drop-off instructions");
        this.packageRepository.save(oldPkg);

        // Charge DELIVERY_FEE charge
        Charge paidCharge = this.orderFactory.createCharge(oldPkg, ChargeType.DELIVERY_FEE, ChargeStatus.PAID, new BigDecimal("1000"));

        oldOrder = this.orderFactory.placeOrder(oldOrder);

        oldPkg = this.packageRepository.findById(oldPkg.getPackageId()).orElse(null); // Fetch updated.
        assertThat(oldPkg).isNotNull();

        // Duplicate the order above
        OffsetDateTime pickUpStart = TimeUtils.getUtcNow().plusMinutes(1);
        Map<String, Object> requestBody = Map.of("pickUpStart", pickUpStart);
        RequestBuilder requestBuilder = post("/delivery-orders/" + oldOrder.getDeliveryOrderId() + "/duplication")
                .contentType("application/json")
                .content(new JSONObject(requestBody).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        Map<String, Object> response = parseResponseBody(result);
        String newDeliveryOrderId = (String) response.get("deliveryOrderId");
        assertThat(newDeliveryOrderId).isNotNull();
        assertThat(oldOrder.getDeliveryOrderId()).isNotEqualTo(newDeliveryOrderId);

        DeliveryOrder newOrder = this.orderRepository.findById(newDeliveryOrderId).orElse(null);
        assertThat(newOrder).isNotNull();

        // Verify order properties
        assertThat(newOrder.getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(oldOrder.getDeliveryOrderId()).isNotEqualTo(newOrder.getDeliveryOrderId()); // Not equal

        // Verify package information
        Package newPkg = this.packageRepository.findByDeliveryOrder(
                this.orderRepository.getById(newDeliveryOrderId)
        ).get(0);
        assertThat(oldPkg.getPackageId()).isNotEqualTo(newPkg.getPackageId());

        assertThat(oldPkg.getFromContact().getContactId()).isNotEqualTo(newPkg.getFromContact().getContactId());
        assertThat(oldPkg.getFromContact().getParentContactId()).isEqualTo(newPkg.getFromContact().getParentContactId());
        assertThat(oldPkg.getToContact().getContactId()).isNotEqualTo(newPkg.getToContact().getContactId());
        assertThat(oldPkg.getToContact().getParentContactId()).isEqualTo(newPkg.getToContact().getParentContactId());

        assertThat(oldPkg.getFromAddress().getAddressId()).isNotEqualTo(newPkg.getFromAddress().getAddressId());
        assertThat(oldPkg.getFromAddress().getParentAddressId()).isEqualTo(newPkg.getFromAddress().getParentAddressId());
        assertThat(oldPkg.getFromAddress().getAddressId()).isNotEqualTo(newPkg.getFromAddress().getAddressId());
        assertThat(oldPkg.getFromAddress().getParentAddressId()).isEqualTo(newPkg.getFromAddress().getParentAddressId());

        assertThat(newPkg.getPickUpStart()).isEqualTo(pickUpStart);
        assertThat(oldPkg.getFromNote()).isEqualTo(newPkg.getFromNote());
        assertThat(oldPkg.getToNote()).isEqualTo(newPkg.getToNote());

        // Verify charge amount and status
        Charge newCharge = this.chargeRepository.findByDeliveryOrderAndType(newOrder, ChargeType.DELIVERY_FEE).get(0);
        assertThat(paidCharge.getDeliveryOrder().getDeliveryOrderId())
                .isNotEqualTo(newCharge.getDeliveryOrder().getDeliveryOrderId());
        assertThat(paidCharge.getTransactionAmount().compareTo(newCharge.getTransactionAmount()) == 0).isTrue();
        assertThat(newCharge.getStatus()).isEqualTo(ChargeStatus.UNPAID);
    }

    @Test
    public void testDuplicateDeliveryOrder_returns404ForOrderWithStartedStatus() throws Exception {
        DeliveryOrder order = this.orderFactory.createOrderWithPackage(OrderStatus.STARTED);

        RequestBuilder requestBuilder = post("/delivery-orders/" + order.getDeliveryOrderId() + "/duplication")
                .contentType("application/json")
                .content(new JSONObject(new HashMap<>()).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Map<String, Object> response = parseResponseBody(result);
        assertThat(response.get("errorMessage")).isEqualTo("DeliveryOrder status must be PLACED or COMPLETE.");
    }

    @Test
    public void testDuplicateDeliveryOrder_returns404ForOrderWithRequestStatus() throws Exception {
        DeliveryOrder order = this.orderFactory.createOrderWithPackage(OrderStatus.REQUEST);

        RequestBuilder requestBuilder = post("/delivery-orders/" + order.getDeliveryOrderId() + "/duplication")
                .contentType("application/json")
                .content(new JSONObject(new HashMap<>()).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Map<String, Object> response = parseResponseBody(result);
        assertThat(response.get("errorMessage")).isEqualTo("DeliveryOrder status must be PLACED or COMPLETE.");
    }

    @Test
    public void testDuplicateDeliveryOrder_returns404ForOrderWithPendingStatus() throws Exception {
        DeliveryOrder order = this.orderFactory.createOrderWithPackage(OrderStatus.PENDING);

        RequestBuilder requestBuilder = post("/delivery-orders/" + order.getDeliveryOrderId() + "/duplication")
                .contentType("application/json")
                .content(new JSONObject(new HashMap<>()).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Map<String, Object> response = parseResponseBody(result);
        assertThat(response.get("errorMessage")).isEqualTo("DeliveryOrder status must be PLACED or COMPLETE.");
    }

    @Test
    public void testCreateDeliveryRequest_createsDeliveryOrderWithRequestStatusAndProvidedPackageFields() throws Exception {
        // Mock communication-api call
        when(this.httpClientMock.post(any(String.class), any(Map.class)))
                .thenReturn(new HttpResult(new HashMap<>(), HttpStatus.OK.value()));

        // Create business hours
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Setup contact and address objects for delivery request
        Customer customer = this.orderFactory.createCustomer();
        Branch branch = this.orderFactory.createBranch(customer);
        Agent agent = this.orderFactory.createAgent(branch);
        Contact fromContact = ObjectFactory.createContact(customer.getCustomerId());
        Contact toContact = ObjectFactory.createContact(customer.getCustomerId());
        this.contactRepository.save(fromContact);
        this.contactRepository.save(toContact);
        Address fromAddress = ObjectFactory.createAddress(customer.getCustomerId());
        this.addressRepository.save(fromAddress);
        ContactAddress fromContactAddress = new ContactAddress(customer.getCustomerId(), fromContact, fromAddress);
        this.contactAddressRepository.save(fromContactAddress);

        Map<String, Object> requestBody = Map.of(
                "agentId", agent.getAgentId(),
                "isCustomerPaying", true,
                "packages", List.of(Map.of(
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
                        )
                ))
        );
        RequestBuilder requestBuilder = post("/customers/" + customer.getCustomerId() + "/delivery-requests")
                .contentType("application/json")
                .content(new JSONObject(requestBody).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        Map<String, Object> response = parseResponseBody(result);

        String deliveryOrderId = (String) response.get("deliveryOrderId");
        assertThat(deliveryOrderId).isNotNull();

        DeliveryOrder deliveryOrder = this.orderRepository.findById(deliveryOrderId).orElse(null);
        assertThat(deliveryOrder).isNotNull();
        assertThat(deliveryOrder.getStatus()).isEqualTo(OrderStatus.REQUEST);
        assertThat(deliveryOrder.getIsCustomerPaying()).isTrue();
        assertThat(deliveryOrder.getAgent().getAgentId()).isEqualTo(agent.getAgentId());
        assertThat(deliveryOrder.getBranch().getBranchId()).isEqualTo(branch.getBranchId());

        Set<Package> packages = deliveryOrder.getPackages();
        assertThat(packages.size()).isEqualTo(1);
        Package pkg = new ArrayList<>(packages).get(0);
        assertThat(pkg.getStatus()).isEqualTo(PackageStatus.REQUEST);
        assertThat(pkg.getFromContact().getContactId()).isEqualTo(fromContact.getContactId());
        assertThat(pkg.getFromAddress().getAddressId()).isEqualTo(fromAddress.getAddressId());
        assertThat(pkg.getToContact().getContactId()).isEqualTo(toContact.getContactId());

        // Validate sent web push
        ArgumentCaptor<Map<String, Object>> webPushPayloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(this.httpClientMock, times(1)).post(eq("null/push"), webPushPayloadCaptor.capture());
        Map<String, Object> actualWebPushPayload = webPushPayloadCaptor.getValue();
        Map<String, Object> expectedWebPushPayload = Map.of(
                "heading", "New Delivery Request!",
                "message", String.format("Delivery request from %s to %s", customer.getBusinessName(), pkg.getToContact().getPhoneNumberOne()),
                "receiverIds", new NullableListBuilder<String>()
                        .add(null)
                        .build(),
                "jsonData", Map.of(
                        "packageId", pkg.getPackageId(),
                        "deliveryOrderId", pkg.getDeliveryOrder().getDeliveryOrderId(),
                        "createdAt", ResourceMapper.stringifyDateTime(OffsetDateTime.now().withNano(0))
                ),
                "metadata", new NullableValueMapBuilder<String, String>()
                        .put("appId", null)
                        .put("apiKey", null)
                        .build()
        );
        assertThat(actualWebPushPayload).isEqualTo(expectedWebPushPayload);
    }

    @Test
    public void testCreateDeliveryRequest_createsNewContactWhenPhoneNumberIsProvidedInsteadOfContactId() throws Exception {
        // Mock communication-api call
        when(this.httpClientMock.post(any(String.class), any(Map.class)))
                .thenReturn(new HttpResult(new HashMap<>(), HttpStatus.OK.value()));

        // Create business hours
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Setup contact and address objects for delivery request
        Customer customer = this.orderFactory.createCustomer();
        Branch branch = this.orderFactory.createBranch(customer);
        Agent agent = this.orderFactory.createAgent(branch);
        Contact fromContact = ObjectFactory.createContact(customer.getCustomerId());
        String toContactPhoneNumberOne = "250788223344";
        this.contactRepository.save(fromContact);
        Address fromAddress = ObjectFactory.createAddress(customer.getCustomerId());
        this.addressRepository.save(fromAddress);
        ContactAddress fromContactAddress = new ContactAddress(customer.getCustomerId(), fromContact, fromAddress);
        this.contactAddressRepository.save(fromContactAddress);

        Map<String, Object> requestBody = Map.of(
                "agentId", agent.getAgentId(),
                "isCustomerPaying", true,
                "packages", List.of(Map.of(
                        "size", PackageSize.LARGE.name(),
                        "priority", PackagePriority.EXPRESS.name(),
                        "fromContact", Map.of(
                                "contactId", fromContact.getContactId()
                        ),
                        "toContact", Map.of(
                                "phoneNumberOne", toContactPhoneNumberOne
                        ),
                        "fromAddress", Map.of(
                                "addressId", fromAddress.getAddressId()
                        )
                ))
        );
        RequestBuilder requestBuilder = post("/customers/" + customer.getCustomerId() + "/delivery-requests")
                .contentType("application/json")
                .content(new JSONObject(requestBody).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        Map<String, Object> response = parseResponseBody(result);

        String deliveryOrderId = (String) response.get("deliveryOrderId");
        assertThat(deliveryOrderId).isNotNull();

        DeliveryOrder deliveryOrder = this.orderRepository.findById(deliveryOrderId).orElse(null);
        assertThat(deliveryOrder).isNotNull();
        assertThat(deliveryOrder.getAgent().getAgentId()).isEqualTo(agent.getAgentId());
        assertThat(deliveryOrder.getBranch().getBranchId()).isEqualTo(branch.getBranchId());

        Set<Package> packages = deliveryOrder.getPackages();
        assertThat(packages.size()).isEqualTo(1);
        Package pkg = new ArrayList<>(packages).get(0);
        assertThat(pkg.getFromContact().getContactId()).isEqualTo(fromContact.getContactId());
        assertThat(pkg.getFromAddress().getAddressId()).isEqualTo(fromAddress.getAddressId());
        assertThat(pkg.getToContact().getPhoneNumberOne()).isEqualTo(toContactPhoneNumberOne);

        // Validate sent web push
        ArgumentCaptor<Map<String, Object>> webPushPayloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(this.httpClientMock, times(1)).post(eq("null/push"), webPushPayloadCaptor.capture());
        Map<String, Object> actualWebPushPayload = webPushPayloadCaptor.getValue();
        Map<String, Object> expectedWebPushPayload = Map.of(
                "heading", "New Delivery Request!",
                "message", String.format("Delivery request from %s to %s", customer.getBusinessName(), pkg.getToContact().getPhoneNumberOne()),
                "receiverIds", new NullableListBuilder<String>()
                        .add(null)
                        .build(),
                "jsonData", Map.of(
                        "packageId", pkg.getPackageId(),
                        "deliveryOrderId", pkg.getDeliveryOrder().getDeliveryOrderId(),
                        "createdAt", ResourceMapper.stringifyDateTime(OffsetDateTime.now().withNano(0))
                ),
                "metadata", new NullableValueMapBuilder<String, String>()
                        .put("appId", null)
                        .put("apiKey", null)
                        .build()
        );
        assertThat(actualWebPushPayload).isEqualTo(expectedWebPushPayload);
    }

    @Test
    public void testCreateDeliveryRequest_returnsErrorWhenSendingSMSFailed() throws Exception {
        // Mock communication-api call
        when(this.httpClientMock.post(any(String.class), any(Map.class)))
                .thenReturn(new HttpResult(new HashMap<>(), HttpStatus.INTERNAL_SERVER_ERROR.value()));

        // Create business hours
        this.orderFactory.createBusinessHour(TimeTestUtils.getDayOfWeek(), OffsetTime.MIN, OffsetTime.MAX);

        // Setup contact and address objects for delivery request
        Customer customer = this.orderFactory.createCustomer();
        Contact fromContact = this.orderFactory.createContact(customer.getCustomerId());
        Address fromAddress = this.orderFactory.createAddress(customer.getCustomerId());

        Map<String, Object> requestBody = Map.of(
                "isCustomerPaying", false,
                "packages", List.of(Map.of(
                        "size", PackageSize.LARGE.name(),
                        "priority", PackagePriority.EXPRESS.name(),
                        "fromContact", Map.of(
                                "contactId", fromContact.getContactId()
                        ),
                        "toContact", Map.of(
                                "phoneNumberOne", "250788223344"
                        ),
                        "fromAddress", Map.of(
                                "addressId", fromAddress.getAddressId()
                        )
                ))
        );
        RequestBuilder requestBuilder = post("/customers/" + customer.getCustomerId() + "/delivery-requests")
                .contentType("application/json")
                .content(new JSONObject(requestBody).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Map<String, Object> actualPayload = parseResponseBody(result);
        Map<String, Object> expectedPayload = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "Unable to send SMS"
        );
        assertThat(actualPayload).isEqualTo(expectedPayload);
    }
}
