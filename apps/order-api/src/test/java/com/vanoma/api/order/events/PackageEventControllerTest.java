package com.vanoma.api.order.events;

import com.vanoma.api.order.charges.ChargeRepository;
import com.vanoma.api.order.customers.Customer;
import com.vanoma.api.order.packages.PackageSize;
import com.vanoma.api.order.packages.PackageStatus;
import com.vanoma.api.order.payment.PaymentAttemptRepository;
import com.vanoma.api.order.tests.OrderFactory;
import com.vanoma.api.order.tests.ResourceMapper;
import com.vanoma.api.order.orders.DeliveryOrder;
import com.vanoma.api.order.orders.OrderRepository;
import com.vanoma.api.order.orders.OrderStatus;
import com.vanoma.api.order.packages.Package;
import com.vanoma.api.order.packages.PackageRepository;
import com.vanoma.api.utils.NullableValueMapBuilder;
import com.vanoma.api.utils.httpwrapper.HttpResult;
import com.vanoma.api.utils.httpwrapper.IHttpClientWrapper;
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

import java.util.*;
import java.util.stream.Collectors;

import static com.vanoma.api.order.tests.ControllerTestUtils.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class PackageEventControllerTest {
    private final List<EventName> eventsCreatedThroughEndpoint = Arrays.stream(EventName.values())
            .filter(eventName -> eventName != EventName.ORDER_PLACED && eventName != EventName.PACKAGE_CANCELLED)
            .collect(Collectors.toList());

    private final List<EventName> eventsWithBuyerSMS = List.of( // Missing PACKAGE_CANCELLED event as it's not created through endpoint
            EventName.DRIVER_DEPARTING_DROP_OFF,
            EventName.PACKAGE_DELIVERED);

    private Customer customer;

    @Autowired
    private MockMvc mvc;
    @Autowired
    private OrderFactory orderFactory;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PackageRepository packageRepository;
    @Autowired
    private PackageEventRepository packageEventRepository;
    @MockBean
    private IHttpClientWrapper httpClientWrapper;
    @Autowired
    private ChargeRepository chargeRepository;
    @Autowired
    private PaymentAttemptRepository paymentAttemptRepository;

    @BeforeEach
    public void setUp() {
        // Delete existing package events to avoid polluting the next test.
        this.packageEventRepository.deleteAll();

        this.customer = this.orderFactory.createCustomer();

        reset(this.httpClientWrapper);
        when(this.httpClientWrapper.post(any(String.class), any(Map.class)))
                .thenReturn(new HttpResult(new HashMap<>(), HttpStatus.OK.value()));
    }

    @Test
    public void testCreatePackageEvent_sendsSMSForEventsWithBuyerSMS() throws Exception {
        Package pkg = this.orderFactory.createPackage(PackageStatus.PLACED);
        DeliveryOrder order = pkg.getDeliveryOrder();
        List<String> messages = List.of(
                String.format(
                        "You have a delivery from %s (%s). Track the progress at null/tracking/?tn=%s",
                        order.getCustomer().getBusinessName(), pkg.getFromContact().getPhoneNumberOne().substring(2), pkg.getTrackingNumber()),
                String.format(
                        "Your package from %s (%s) is now delivered. Thank you!",
                        order.getCustomer().getBusinessName(), pkg.getFromContact().getPhoneNumberOne().substring(2))
        );

        for (int index = 0; index < eventsWithBuyerSMS.size(); index += 1) {
            String assignmentId = UUID.randomUUID().toString();
            EventName eventName = eventsWithBuyerSMS.get(index);
            Map<String, Object> requestBody = Map.of(
                    "eventName", eventName.name(),
                    "assignmentId", assignmentId
        );

            // Create event
            RequestBuilder requestBuilder = put("/packages/" + pkg.getPackageId() + "/events")
                    .contentType("application/json")
                    .content(stringifyRequestBody(requestBody));
            MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

            // Validate event
            assertThat(result.getStatus()).isEqualTo(200);
            PackageEvent packageEvent = this.packageEventRepository.findFirstByPkgAndEventNameAndAssignmentId(pkg, eventName, assignmentId);
            Map<String, Object> actualBody = parseResponseBody(result);
            Map<String, Object> expectedBody = ResourceMapper.createPackageEventMap(packageEvent);
            assertThat(actualBody).isEqualTo(expectedBody);

            // Validate sent SMS
            ArgumentCaptor<Map<String, Object>> smsPayloadCaptor = ArgumentCaptor.forClass(Map.class);
            verify(this.httpClientWrapper, times(index + 1)).post(eq("null/sms"), smsPayloadCaptor.capture());
            Map<String, Object> actualSMSPayload = smsPayloadCaptor.getValue();
            Map<String, Object> expectedSMSPayload = Map.of(
                    "message", messages.get(index),
                    "phoneNumbers", List.of(pkg.getToContact().getPhoneNumberOne()),
                    "serviceName", "DELIVERY_NOTIFICATION",
                    "isUnicode", false
            );
            assertThat(actualSMSPayload).isEqualTo(expectedSMSPayload);
        }
    }

    @Test
    public void testCreatePackageEvent_sendsPushToWebUserForEventsCreatedThroughEndpoint() throws Exception {
        Package pkg = this.orderFactory.createPackage(PackageStatus.PLACED);

        for (int index = 0; index < eventsCreatedThroughEndpoint.size(); index += 1) {
            String assignmentId = UUID.randomUUID().toString();
            EventName eventName = eventsCreatedThroughEndpoint.get(index);
            Map<String, Object> requestBody = Map.of(
                    "eventName", eventName.name(),
                    "assignmentId", assignmentId
            );

            // Create event
            RequestBuilder requestBuilder = put("/packages/" + pkg.getPackageId() + "/events")
                    .contentType("application/json")
                    .content(stringifyRequestBody(requestBody));
            MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

            // Validate event
            assertThat(result.getStatus()).isEqualTo(200);
            PackageEvent packageEvent = this.packageEventRepository.findFirstByPkgAndEventNameAndAssignmentId(pkg, eventName, assignmentId);
            Map<String, Object> actualBody = parseResponseBody(result);
            Map<String, Object> expectedBody = ResourceMapper.createPackageEventMap(packageEvent);
            assertThat(actualBody).isEqualTo(expectedBody);

            // Validate sent web push
            ArgumentCaptor<Map<String, Object>> webPushPayloadCaptor = ArgumentCaptor.forClass(Map.class);
            verify(this.httpClientWrapper, times(index + 1)).post(eq("null/push"), webPushPayloadCaptor.capture());
            Map<String, Object> actualWebPushPayload = webPushPayloadCaptor.getValue();
            Map<String, Object> expectedWebPushPayload = Map.of(
                    "heading", "Check new delivery event!",
                    "message", packageEvent.getTextEN(),
                    "receiverIds", List.of(pkg.getFromContact().getCustomerId()),
                    "jsonData", Map.of(
                            "packageEventId", packageEvent.getPackageEventId(),
                            "packageId", pkg.getPackageId(),
                            "deliveryOrderId", pkg.getDeliveryOrder().getDeliveryOrderId(),
                            "eventName", packageEvent.getEventName().name(),
                            "text", Map.of(
                                    "en", packageEvent.getTextEN(),
                                    "fr", packageEvent.getTextFR(),
                                    "rw", packageEvent.getTextRW()
                            ),
                            "createdAt", ResourceMapper.stringifyDateTime(packageEvent.getCreatedAt())
                    ),
                    "metadata", new NullableValueMapBuilder<String, String>()
                            .put("appId", null)
                            .put("apiKey", null)
                            .build()
            );
            assertThat(actualWebPushPayload).isEqualTo(expectedWebPushPayload);
        }
    }

    @Test
    public void testCreatePackageEvent_sendsCallbackToApiUserForEventsCreatedThroughEndpoint() throws Exception {
        String eventCallbackUrl = "https://api.example.com/delivery-events";
        Package pkg = this.orderFactory.createPackage(customer, eventCallbackUrl);

        for (int index = 0; index < eventsCreatedThroughEndpoint.size(); index += 1) {
            String assignmentId = UUID.randomUUID().toString();
            EventName eventName = eventsCreatedThroughEndpoint.get(index);
            Map<String, Object> requestBody = Map.of(
                    "eventName", eventName.name(),
                    "assignmentId", assignmentId
            );

            // Create event
            RequestBuilder requestBuilder = put("/packages/" + pkg.getPackageId() + "/events")
                    .contentType("application/json")
                    .content(stringifyRequestBody(requestBody));
            MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

            // Validate event
            assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());
            PackageEvent packageEvent = this.packageEventRepository.findFirstByPkgAndEventNameAndAssignmentId(pkg, eventName, assignmentId);
            Map<String, Object> actualBody = parseResponseBody(result);
            Map<String, Object> expectedBody = ResourceMapper.createPackageEventMap(packageEvent);
            assertThat(actualBody).isEqualTo(expectedBody);

            // Validate sent callback
            ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(this.httpClientWrapper, times(index + 1)).post(eq(eventCallbackUrl), mapCaptor.capture());
            Map<String, Object> actualCallbackPayload = mapCaptor.getValue();
            Map<String, Object> expectedCallbackPayload = Map.of(
                    "eventName", eventName.name(),
                    "packageEventId", packageEvent.getPackageEventId(),
                    "text", Map.of(
                            "en", packageEvent.getTextEN(),
                            "fr", packageEvent.getTextFR(),
                            "rw", packageEvent.getTextRW()
                    ),
                    "package", ResourceMapper.createPackageContactsMap(pkg),
                    "createdAt", ResourceMapper.stringifyDateTime(packageEvent.getCreatedAt())
            );
            assertThat(actualCallbackPayload).isEqualTo(expectedCallbackPayload);

            // No push is sent out
            verify(this.httpClientWrapper, never()).post(eq("null/push"), any());
        }
    }

    @Test
    public void testCreatePackageEvent_updatesPackageAndOrderStatusesForDeliveredEvent() throws Exception {
        DeliveryOrder order = this.orderFactory.createOrder(OrderStatus.PLACED);
        Package pkg1 = this.orderFactory.createPackage(order, PackageSize.SMALL);
        Package pkg2 = this.orderFactory.createPackage(order, PackageStatus.CANCELED, PackageSize.SMALL);
        Package pkg3 = this.orderFactory.createPackage(order, PackageStatus.INCOMPLETE, PackageSize.SMALL);
        Map<String, Object> requestBody = Map.of(
                "eventName", EventName.PACKAGE_DELIVERED.name(),
                "assignmentId", UUID.randomUUID().toString()
        );

        // Create event
        RequestBuilder requestBuilder = put("/packages/" + pkg1.getPackageId() + "/events")
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate event
        assertThat(result.getStatus()).isEqualTo(200);
        PackageEvent packageEvent = this.packageEventRepository.findByPkg(pkg1).get(0);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createPackageEventMap(packageEvent);
        assertThat(actualBody).isEqualTo(expectedBody);

        // Validate order completion
        order = this.orderRepository.getById(pkg1.getDeliveryOrder().getDeliveryOrderId());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETE);

        // Validate package status
        pkg1 = this.packageRepository.getById(pkg1.getPackageId());
        assertThat(pkg1.getStatus()).isEqualTo(PackageStatus.COMPLETE);

        pkg2 = this.packageRepository.getById(pkg2.getPackageId());
        assertThat(pkg2.getStatus()).isEqualTo(PackageStatus.CANCELED);

        pkg3 = this.packageRepository.getById(pkg3.getPackageId());
        assertThat(pkg3.getStatus()).isEqualTo(PackageStatus.INCOMPLETE);
    }

    @Test
    public void testCreatePackageEvent_deletesExistingEvent() throws Exception {
        String assignmentId = UUID.randomUUID().toString();
        Package pkg = this.orderFactory.createPackage(PackageStatus.PLACED);
        PackageEvent existingEvent = this.orderFactory.createPackageEvent(pkg, EventName.DRIVER_ASSIGNED, assignmentId);
        Map<String, Object> requestBody = Map.of(
                "eventName", EventName.DRIVER_ASSIGNED.name(),
                "assignmentId", assignmentId
        );

        // Create event
        RequestBuilder requestBuilder = put("/packages/" + pkg.getPackageId() + "/events")
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        PackageEvent packageEvent = this.packageEventRepository.findFirstByPkgAndEventNameAndAssignmentId(pkg, EventName.DRIVER_ASSIGNED, assignmentId);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createPackageEventMap(packageEvent);
        assertThat(actualBody).isEqualTo(expectedBody);
        assertThat(existingEvent.getPackageEventId()).isNotEqualTo(packageEvent.getPackageEventId());
    }

    @Test
    public void testCreatePackageEvent_returnsErrorWhenNoMatchingPackageIsFound() throws Exception {
        String packageId = UUID.randomUUID().toString();
        Map<String, Object> requestBody = Map.of(
                "eventName", EventName.PACKAGE_DELIVERED.name(),
                "assignmentId", UUID.randomUUID().toString()
        );

        RequestBuilder requestBuilder = put("/packages/" + packageId + "/events")
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(404);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "RESOURCE_NOT_FOUND",
                "errorMessage", "Unable to find com.vanoma.api.order.packages.Package with id " + packageId
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testCreatePackageEvent_returnsErrorWhenAssignmentIdIsMissing() throws Exception {
        String packageId = UUID.randomUUID().toString();
        Map<String, Object> requestBody = Map.of(
                "eventName", EventName.PACKAGE_DELIVERED.name()
        );

        RequestBuilder requestBuilder = put("/packages/" + packageId + "/events")
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(400);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "assignmentId is a required parameter"
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testCreatePackageEvent_skipsSendingBuyerSMSIfNotificationsAreDisabled() throws Exception {
        Package pkg = this.packageRepository.save(this.orderFactory.createPackage(PackageStatus.PLACED).setEnableNotifications(false));
        DeliveryOrder order = pkg.getDeliveryOrder();
        List<String> messages = List.of(
                String.format(
                        "You have a delivery from %s (%s). Track the progress at null/tracking/?tn=%s",
                        order.getCustomer().getBusinessName(), pkg.getFromContact().getPhoneNumberOne().substring(2), pkg.getTrackingNumber()),
                String.format(
                        "Your package from %s (%s) is now delivered. Thank you!",
                        order.getCustomer().getBusinessName(), pkg.getFromContact().getPhoneNumberOne().substring(2))
        );

        for (EventName eventName : eventsWithBuyerSMS) {
            String assignmentId = UUID.randomUUID().toString();
            Map<String, Object> requestBody = Map.of(
                    "eventName", eventName.name(),
                    "assignmentId", assignmentId
            );

            // Create event
            RequestBuilder requestBuilder = put("/packages/" + pkg.getPackageId() + "/events")
                    .contentType("application/json")
                    .content(stringifyRequestBody(requestBody));
            MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

            // Validate event
            assertThat(result.getStatus()).isEqualTo(200);
            PackageEvent packageEvent = this.packageEventRepository.findFirstByPkgAndEventNameAndAssignmentId(pkg, eventName, assignmentId);
            Map<String, Object> actualBody = parseResponseBody(result);
            Map<String, Object> expectedBody = ResourceMapper.createPackageEventMap(packageEvent);
            assertThat(actualBody).isEqualTo(expectedBody);

            // No sent SMS
            verify(this.httpClientWrapper, never()).post(eq("null/sms"), anyMap());
        }
    }

    @Test
    public void testCreatePackageEvent_skipsSendingPushToWebUserIfNotificationsAreDisabled() throws Exception {
        Package pkg = this.packageRepository.save(this.orderFactory.createPackage(PackageStatus.PLACED).setEnableNotifications(false));

        for (EventName eventName : eventsCreatedThroughEndpoint) {
            String assignmentId = UUID.randomUUID().toString();
            Map<String, Object> requestBody = Map.of(
                    "eventName", eventName.name(),
                    "assignmentId", assignmentId
            );

            // Create event
            RequestBuilder requestBuilder = put("/packages/" + pkg.getPackageId() + "/events")
                    .contentType("application/json")
                    .content(stringifyRequestBody(requestBody));
            MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

            // Validate event
            assertThat(result.getStatus()).isEqualTo(200);
            PackageEvent packageEvent = this.packageEventRepository.findFirstByPkgAndEventNameAndAssignmentId(pkg, eventName, assignmentId);
            Map<String, Object> actualBody = parseResponseBody(result);
            Map<String, Object> expectedBody = ResourceMapper.createPackageEventMap(packageEvent);
            assertThat(actualBody).isEqualTo(expectedBody);

            // No web push is sent out
            verify(this.httpClientWrapper, never()).post(eq("null/push"), anyMap());
        }
    }

    @Test
    public void testCreatePackageEvent_skipsSendingCallbackToApiUserIfNotificationsAreDisabled() throws Exception {
        String eventCallbackUrl = "https://api.example.com/delivery-events";
        Package pkg = this.packageRepository.save(this.orderFactory.createPackage(customer, eventCallbackUrl).setEnableNotifications(false));

        for (EventName eventName : eventsCreatedThroughEndpoint) {
            String assignmentId = UUID.randomUUID().toString();
            Map<String, Object> requestBody = Map.of(
                    "eventName", eventName.name(),
                    "assignmentId", assignmentId
            );

            // Create event
            RequestBuilder requestBuilder = put("/packages/" + pkg.getPackageId() + "/events")
                    .contentType("application/json")
                    .content(stringifyRequestBody(requestBody));
            MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

            // Validate event
            assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());
            PackageEvent packageEvent = this.packageEventRepository.findFirstByPkgAndEventNameAndAssignmentId(pkg, eventName, assignmentId);
            Map<String, Object> actualBody = parseResponseBody(result);
            Map<String, Object> expectedBody = ResourceMapper.createPackageEventMap(packageEvent);
            assertThat(actualBody).isEqualTo(expectedBody);

            // No callback (or web push) is sent out
            verify(this.httpClientWrapper, never()).post(eq(eventCallbackUrl), anyMap());
            verify(this.httpClientWrapper, never()).post(eq("null/push"), anyMap());
        }
    }
}
