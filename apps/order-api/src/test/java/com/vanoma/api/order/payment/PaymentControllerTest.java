package com.vanoma.api.order.payment;

import com.vanoma.api.order.businesshours.BusinessHour;
import com.vanoma.api.order.businesshours.BusinessHourRepository;
import com.vanoma.api.order.charges.*;
import com.vanoma.api.order.customers.Agent;
import com.vanoma.api.order.customers.Branch;
import com.vanoma.api.order.customers.Customer;
import com.vanoma.api.order.events.PackageEventRepository;
import com.vanoma.api.order.orders.*;
import com.vanoma.api.order.packages.Package;
import com.vanoma.api.order.packages.PackageRepository;
import com.vanoma.api.order.packages.PackageSize;
import com.vanoma.api.order.packages.PackageStatus;
import com.vanoma.api.order.pricing.CustomPricingRepository;
import com.vanoma.api.order.tests.OrderFactory;
import com.vanoma.api.utils.httpwrapper.HttpResult;
import com.vanoma.api.utils.httpwrapper.IHttpClientWrapper;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.vanoma.api.order.tests.ControllerTestUtils.parseResponseBody;
import static com.vanoma.api.order.tests.ControllerTestUtils.stringifyRequestBody;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class PaymentControllerTest {
    private Customer customer;

    @Autowired
    private MockMvc mvc;
    @Autowired
    private OrderFactory orderFactory;
    @Autowired
    private ChargeRepository chargeRepository;
    @Autowired
    private DiscountRepository discountRepository;
    @MockBean
    private IHttpClientWrapper httpClientMock;
    @Autowired
    private CustomPricingRepository customPricingRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PackageRepository packageRepository;
    @Autowired
    private PaymentRequestRepository paymentRequestRepository;
    @Autowired
    private BusinessHourRepository businessHourRepository;
    @Autowired
    private PackageEventRepository packageEventRepository;

    @BeforeEach
    public void setUp() {
        this.customer = this.orderFactory.createCustomer();

        reset(this.httpClientMock);
        when(this.httpClientMock.post(any(String.class), any(Map.class)))
                .thenReturn(new HttpResult(new HashMap<>(), HttpStatus.OK.value()));
    }

    @Test
    public void testRequestPaymentForOneOrder() throws Exception {
        // Business hour record for today
        BusinessHour businessHour = new BusinessHour()
                .setWeekDay(TimeUtils.getUtcNow().getDayOfWeek().getValue())
                .setOpenAt(OffsetTime.MIN)
                .setCloseAt(OffsetTime.MAX)
                .setIsDayOff(false);
        this.businessHourRepository.save(businessHour);

        // Charges & discounts
        DeliveryOrder order = this.orderFactory.createOrder();
        Package pkg = this.orderFactory.createPackage(order, PackageSize.SMALL);
        Charge charge1 = this.orderFactory.createCharge(pkg, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Charge charge2 = this.orderFactory.createCharge(pkg, ChargeType.PICK_UP_DELAY, ChargeStatus.UNPAID);
        Discount discount1 = this.orderFactory.createDiscount(order, DiscountType.BATCHING, DiscountStatus.PENDING);

        // Call the endpoint
        String paymentMethodId = UUID.randomUUID().toString();
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/payment-requests", pkg.getDeliveryOrder().getDeliveryOrderId()))
                .contentType("application/json")
                .content(stringifyRequestBody(Map.of("paymentMethodId", paymentMethodId)));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate returned response
        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        assertThat(actualBody.get("paymentRequestId")).isNotNull();

        // Validate payment attempts
        String paymentRequestId = (String) actualBody.get("paymentRequestId");
        PaymentRequest paymentRequest = this.paymentRequestRepository.getById(paymentRequestId);
        assertThat(paymentRequest.getIsSuccess()).isNull();
        assertThat(paymentRequest.getCharges().size()).isEqualTo(2);
        assertThat(paymentRequest.getCharges().stream().filter(c -> c.equals(charge1)).count()).isEqualTo(1);
        assertThat(paymentRequest.getCharges().stream().filter(c -> c.equals(charge2)).count()).isEqualTo(1);
        assertThat(paymentRequest.getDiscounts().size()).isEqualTo(1);
        assertThat(paymentRequest.getDiscounts().stream().filter(d -> d.equals(discount1)).count()).isEqualTo(1);

        // Validate payment-api payload
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        TransactionBreakdown breakdown = ChargeUtils.getTransactionBreakdown(Set.of(charge1, charge2), Set.of(discount1));
        verify(this.httpClientMock).post(any(), captor.capture());
        Map<String, Object> actualPayload = (Map<String, Object>) captor.getValue();
        Map<String, Object> expectedPayload = Map.of(
                "callbackUrl", String.format("null/delivery-payment-requests/%s/callbacks", paymentRequestId),
                "description", "Delivery transaction",
                "paymentRequestId", paymentRequestId,
                "totalAmount", breakdown.getTotalAmount().doubleValue(),
                "transactionAmount", breakdown.getTransactionAmount().doubleValue(),
                "transactionFee", breakdown.getTransactionFee().doubleValue(),
                "paymentMethod", Map.of(
                        "paymentMethodId", paymentMethodId
                )
        );
        assertThat(actualPayload).isEqualTo(expectedPayload);
    }

    @Test
    public void testRequestPaymentForOneOrder_returns200IfOrderIsAlreadyPaid() throws Exception {
        // Business hour record for today
        BusinessHour businessHour = new BusinessHour()
                .setWeekDay(TimeUtils.getUtcNow().getDayOfWeek().getValue())
                .setOpenAt(OffsetTime.MIN)
                .setCloseAt(OffsetTime.MAX)
                .setIsDayOff(false);
        this.businessHourRepository.save(businessHour);

        // Charges to pay
        Package pkg = this.orderFactory.createPackage();
        Charge charge = this.orderFactory.createCharge(pkg, ChargeType.DELIVERY_FEE, ChargeStatus.PAID);

        // Call the endpoint
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/payment-requests", pkg.getDeliveryOrder().getDeliveryOrderId()))
                .contentType("application/json")
                .content(stringifyRequestBody(Map.of("paymentMethodId", UUID.randomUUID().toString())));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate returned response
        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of("message", "The fee has been already paid");
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testRequestPaymentForOneOrder_propagatesPaymentApiError() throws Exception {
        // Business hour record for today
        BusinessHour businessHour = new BusinessHour()
                .setWeekDay(TimeUtils.getUtcNow().getDayOfWeek().getValue())
                .setOpenAt(OffsetTime.MIN)
                .setCloseAt(OffsetTime.MAX)
                .setIsDayOff(false);
        this.businessHourRepository.save(businessHour);

        // Charges to pay
        Package pkg = this.orderFactory.createPackage();
        Charge charge = this.orderFactory.createCharge(pkg, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);

        // Mock payment api response
        Map<String, Object> paymentApiResponse = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "Invalid momo account"
        );
        when(this.httpClientMock.post(eq("null/payment-requests"), any(Map.class)))
                .thenReturn(new HttpResult(paymentApiResponse, HttpStatus.BAD_REQUEST.value()));

        // Call the endpoint
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/payment-requests", pkg.getDeliveryOrder().getDeliveryOrderId()))
                .contentType("application/json")
                .content(stringifyRequestBody(Map.of("paymentMethodId", UUID.randomUUID().toString())));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate returned response
        assertThat(result.getStatus()).isEqualTo(400);
        Map<String, Object> actualBody = parseResponseBody(result);
        assertThat(actualBody).isEqualTo(paymentApiResponse);
    }

    @Test
    public void testRequestPaymentForOneOrder_returnsErrorIfPickupStartIsOutsideBusinessHours() throws Exception {
        // Business hour record for today
        BusinessHour businessHour = new BusinessHour()
                .setWeekDay(TimeUtils.getUtcNow().getDayOfWeek().getValue())
                .setOpenAt(TimeUtils.getUtcNow().toOffsetTime().plusHours(2))
                .setCloseAt(TimeUtils.getUtcNow().toOffsetTime().plusHours(8))
                .setIsDayOff(false);
        this.businessHourRepository.save(businessHour);

        // Charges to pay
        Package pkg = this.orderFactory.createPackage();
        Charge charge = this.orderFactory.createCharge(pkg, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);

        // Call the endpoint
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/payment-requests", pkg.getDeliveryOrder().getDeliveryOrderId()))
                .contentType("application/json")
                .content(stringifyRequestBody(Map.of("paymentMethodId", UUID.randomUUID().toString())));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate returned response
        assertThat(result.getStatus()).isEqualTo(400);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "Pick-up time is earlier than business hours"
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testRequestPaymentForOneOrder_returnsErrorIfOrderHasNoCharges() throws Exception {
        // Business hour record for today
        BusinessHour businessHour = new BusinessHour()
                .setWeekDay(TimeUtils.getUtcNow().getDayOfWeek().getValue())
                .setOpenAt(OffsetTime.MIN)
                .setCloseAt(OffsetTime.MAX)
                .setIsDayOff(false);
        this.businessHourRepository.save(businessHour);

        // Charges to pay
        Package pkg = this.orderFactory.createPackage();

        // Call the endpoint
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/payment-requests", pkg.getDeliveryOrder().getDeliveryOrderId()))
                .contentType("application/json")
                .content(stringifyRequestBody(Map.of("paymentMethodId", UUID.randomUUID().toString())));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate returned response
        assertThat(result.getStatus()).isEqualTo(404);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "RESOURCE_NOT_FOUND",
                "errorMessage", "Delivery fee not found"
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testRequestPaymentForOneOrder_returnsErrorIfMissingPaymentMethod() throws Exception {
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/payment-requests", UUID.randomUUID()))
                .contentType("application/json")
                .content(stringifyRequestBody(Map.of()));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(400);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "paymentMethodId is a required parameter"
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testRequestPaymentForManyOrders_withoutEndAtParameter() throws Exception {
        // Charges & discounts
        DeliveryOrder order1 = this.orderFactory.createOrder(customer, OrderStatus.COMPLETE);
        DeliveryOrder order2 = this.orderFactory.createOrder(customer, OrderStatus.COMPLETE);
        Package pkg1 = this.orderFactory.createPackage(order1, PackageSize.SMALL);
        Package pkg2 = this.orderFactory.createPackage(order2, PackageSize.SMALL);
        Charge charge1 = this.orderFactory.createCharge(pkg1, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Charge charge2 = this.orderFactory.createCharge(pkg2, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Discount discount1 = this.orderFactory.createDiscount(order1, DiscountType.BATCHING, DiscountStatus.PENDING);
        Discount discount2 = this.orderFactory.createDiscount(order2, DiscountType.BATCHING, DiscountStatus.PENDING);

        // Call the endpoint
        BigDecimal totalTransactionAmount = new BigDecimal("1800");
        BigDecimal transactionFee = ChargeUtils.computeTransactionFeeGivenTransactionAmount(totalTransactionAmount);
        String paymentMethodId = UUID.randomUUID().toString();
        Map<String, Object> requestBody = Map.of(
                "paymentMethodId", paymentMethodId,
                "totalAmount", totalTransactionAmount.add(transactionFee)
        );
        RequestBuilder requestBuilder = post(String.format("/customers/%s/delivery-payment-requests", customer.getCustomerId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate returned response
        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        assertThat(actualBody.get("paymentRequestId")).isNotNull();

        // Validate payment attempts
        String paymentRequestId = (String) actualBody.get("paymentRequestId");
        PaymentRequest paymentRequest = this.paymentRequestRepository.getById(paymentRequestId);
        assertThat(paymentRequest.getIsSuccess()).isNull();
        assertThat(paymentRequest.getCharges().size()).isEqualTo(2);
        assertThat(paymentRequest.getCharges().stream().filter(c -> c.equals(charge1)).count()).isEqualTo(1);
        assertThat(paymentRequest.getCharges().stream().filter(c -> c.equals(charge2)).count()).isEqualTo(1);
        assertThat(paymentRequest.getDiscounts().size()).isEqualTo(2);
        assertThat(paymentRequest.getDiscounts().stream().filter(d -> d.equals(discount1)).count()).isEqualTo(1);
        assertThat(paymentRequest.getDiscounts().stream().filter(d -> d.equals(discount2)).count()).isEqualTo(1);

        // Validate payment-api payload
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        TransactionBreakdown breakdown = ChargeUtils.getTransactionBreakdown(Set.of(charge1, charge2), Set.of(discount1, discount2));
        verify(this.httpClientMock).post(any(), captor.capture());
        Map<String, Object> actualPayload = (Map<String, Object>) captor.getValue();
        Map<String, Object> expectedPayload = Map.of(
                "callbackUrl", String.format("null/delivery-payment-requests/%s/callbacks", paymentRequestId),
                "description", "Delivery transaction",
                "paymentRequestId", paymentRequestId,
                "totalAmount", breakdown.getTotalAmount().doubleValue(),
                "transactionAmount", breakdown.getTransactionAmount().doubleValue(),
                "transactionFee", breakdown.getTransactionFee().doubleValue(),
                "paymentMethod", Map.of(
                        "paymentMethodId", paymentMethodId
                )
        );
        assertThat(actualPayload).isEqualTo(expectedPayload);
    }

    @Test
    public void testRequestPaymentForManyOrders_withEndAtParameter() throws Exception {
        // Charges & discounts
        DeliveryOrder order1 = this.orderFactory.createOrder(customer, OrderStatus.COMPLETE, TimeUtils.getUtcNow().minusDays(5));
        DeliveryOrder order2 = this.orderFactory.createOrder(customer, OrderStatus.COMPLETE, TimeUtils.getUtcNow().minusDays(4));
        DeliveryOrder order3 = this.orderFactory.createOrder(customer, OrderStatus.COMPLETE, TimeUtils.getUtcNow().minusDays(2));
        Package pkg1 = this.orderFactory.createPackage(order1, PackageSize.SMALL);
        Package pkg2 = this.orderFactory.createPackage(order2, PackageSize.SMALL);
        Package pkg3 = this.orderFactory.createPackage(order3, PackageSize.SMALL);
        Charge charge1 = this.orderFactory.createCharge(pkg1, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Charge charge2 = this.orderFactory.createCharge(pkg2, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Charge charge3 = this.orderFactory.createCharge(pkg3, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Discount discount1 = this.orderFactory.createDiscount(order1, DiscountType.BATCHING, DiscountStatus.PENDING);
        Discount discount2 = this.orderFactory.createDiscount(order2, DiscountType.BATCHING, DiscountStatus.PENDING);
        Discount discount3 = this.orderFactory.createDiscount(order3, DiscountType.BATCHING, DiscountStatus.PENDING);

        // Call the endpoint
        BigDecimal totalTransactionAmount = new BigDecimal("1800");
        BigDecimal transactionFee = ChargeUtils.computeTransactionFeeGivenTransactionAmount(totalTransactionAmount);

        String paymentMethodId = UUID.randomUUID().toString();
        Map<String, Object> requestBody = Map.of(
                "paymentMethodId", paymentMethodId,
                "totalAmount", totalTransactionAmount.add(transactionFee),
                "endAt", TimeUtils.getUtcNow().minusDays(3).toString()
        );
        RequestBuilder requestBuilder = post(String.format("/customers/%s/delivery-payment-requests", customer.getCustomerId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate returned response
        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        assertThat(actualBody.get("paymentRequestId")).isNotNull();

        // Validate payment attempts
        String paymentRequestId = (String) actualBody.get("paymentRequestId");
        PaymentRequest paymentRequest = this.paymentRequestRepository.getById(paymentRequestId);
        assertThat(paymentRequest.getIsSuccess()).isNull();
        assertThat(paymentRequest.getCharges().size()).isEqualTo(2);
        assertThat(paymentRequest.getCharges().stream().filter(c -> c.equals(charge1)).count()).isEqualTo(1);
        assertThat(paymentRequest.getCharges().stream().filter(c -> c.equals(charge2)).count()).isEqualTo(1);
        assertThat(paymentRequest.getDiscounts().size()).isEqualTo(2);
        assertThat(paymentRequest.getDiscounts().stream().filter(d -> d.equals(discount1)).count()).isEqualTo(1);
        assertThat(paymentRequest.getDiscounts().stream().filter(d -> d.equals(discount2)).count()).isEqualTo(1);

        // Validate payment-api payload
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        TransactionBreakdown breakdown = ChargeUtils.getTransactionBreakdown(Set.of(charge1, charge2), Set.of(discount1, discount2));
        verify(this.httpClientMock).post(any(), captor.capture());
        Map<String, Object> actualPayload = (Map<String, Object>) captor.getValue();
        Map<String, Object> expectedPayload = Map.of(
                "callbackUrl", String.format("null/delivery-payment-requests/%s/callbacks", paymentRequestId),
                "description", "Delivery transaction",
                "paymentRequestId", paymentRequestId,
                "totalAmount", breakdown.getTotalAmount().doubleValue(),
                "transactionAmount", breakdown.getTransactionAmount().doubleValue(),
                "transactionFee", breakdown.getTransactionFee().doubleValue(),
                "paymentMethod", Map.of(
                        "paymentMethodId", paymentMethodId
                )
        );
        assertThat(actualPayload).isEqualTo(expectedPayload);
    }

    @Test
    public void testRequestPaymentForManyOrders_withBranchParameter() throws Exception {
        // Charges & discounts
        Branch branch1 = this.orderFactory.createBranch(customer);
        Branch branch2 = this.orderFactory.createBranch(customer);
        Agent agent1 = this.orderFactory.createAgent(branch1);
        Agent agent2 = this.orderFactory.createAgent(branch2);
        DeliveryOrder order1 = this.orderFactory.createOrder(agent1, OrderStatus.COMPLETE);
        DeliveryOrder order2 = this.orderFactory.createOrder(agent1, OrderStatus.COMPLETE);
        DeliveryOrder order3 = this.orderFactory.createOrder(agent2, OrderStatus.COMPLETE);
        Package pkg1 = this.orderFactory.createPackage(order1, PackageSize.SMALL);
        Package pkg2 = this.orderFactory.createPackage(order2, PackageSize.SMALL);
        Package pkg3 = this.orderFactory.createPackage(order3, PackageSize.SMALL);
        Charge charge1 = this.orderFactory.createCharge(pkg1, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Charge charge2 = this.orderFactory.createCharge(pkg2, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Charge charge3 = this.orderFactory.createCharge(pkg3, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Discount discount1 = this.orderFactory.createDiscount(order1, DiscountType.BATCHING, DiscountStatus.PENDING);
        Discount discount2 = this.orderFactory.createDiscount(order2, DiscountType.BATCHING, DiscountStatus.PENDING);
        Discount discount3 = this.orderFactory.createDiscount(order3, DiscountType.BATCHING, DiscountStatus.PENDING);

        // Call the endpoint
        BigDecimal totalTransactionAmount = new BigDecimal("1800");
        BigDecimal transactionFee = ChargeUtils.computeTransactionFeeGivenTransactionAmount(totalTransactionAmount);

        String paymentMethodId = UUID.randomUUID().toString();
        Map<String, Object> requestBody = Map.of(
                "paymentMethodId", paymentMethodId,
                "totalAmount", totalTransactionAmount.add(transactionFee),
                "branchId", branch1.getBranchId()
        );
        RequestBuilder requestBuilder = post(String.format("/customers/%s/delivery-payment-requests", customer.getCustomerId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate returned response
        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        assertThat(actualBody.get("paymentRequestId")).isNotNull();

        // Validate payment attempts
        String paymentRequestId = (String) actualBody.get("paymentRequestId");
        PaymentRequest paymentRequest = this.paymentRequestRepository.getById(paymentRequestId);
        assertThat(paymentRequest.getIsSuccess()).isNull();
        assertThat(paymentRequest.getCharges().size()).isEqualTo(2);
        assertThat(paymentRequest.getCharges().stream().filter(c -> c.equals(charge1)).count()).isEqualTo(1);
        assertThat(paymentRequest.getCharges().stream().filter(c -> c.equals(charge2)).count()).isEqualTo(1);
        assertThat(paymentRequest.getDiscounts().size()).isEqualTo(2);
        assertThat(paymentRequest.getDiscounts().stream().filter(d -> d.equals(discount1)).count()).isEqualTo(1);
        assertThat(paymentRequest.getDiscounts().stream().filter(d -> d.equals(discount2)).count()).isEqualTo(1);

        // Validate payment-api payload
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        TransactionBreakdown breakdown = ChargeUtils.getTransactionBreakdown(Set.of(charge1, charge2), Set.of(discount1, discount2));
        verify(this.httpClientMock).post(any(), captor.capture());
        Map<String, Object> actualPayload = (Map<String, Object>) captor.getValue();
        Map<String, Object> expectedPayload = Map.of(
                "callbackUrl", String.format("null/delivery-payment-requests/%s/callbacks", paymentRequestId),
                "description", "Delivery transaction",
                "paymentRequestId", paymentRequestId,
                "totalAmount", breakdown.getTotalAmount().doubleValue(),
                "transactionAmount", breakdown.getTransactionAmount().doubleValue(),
                "transactionFee", breakdown.getTransactionFee().doubleValue(),
                "paymentMethod", Map.of(
                        "paymentMethodId", paymentMethodId
                )
        );
        assertThat(actualPayload).isEqualTo(expectedPayload);
    }

    @Test
    public void testRequestPaymentForManyOrders_returnsErrorIfMissingPaymentMethod() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "totalAmount", 2051.28
        );
        RequestBuilder requestBuilder = post(String.format("/customers/%s/delivery-payment-requests", UUID.randomUUID()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(400);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "paymentMethodId is a required parameter"
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testRequestPaymentForManyOrders_returnsErrorIfMissingTotalAmount() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "paymentMethodId", UUID.randomUUID().toString()
        );
        RequestBuilder requestBuilder = post(String.format("/customers/%s/delivery-payment-requests", UUID.randomUUID()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(400);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "totalAmount is a required parameter"
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testRequestPaymentForManyOrders_returnsErrorForInvalidTotalAmount() throws Exception {
        // Charges to pay
        Package pkg1 = this.orderFactory.createPackage(customer, PackageStatus.COMPLETE);
        Package pkg2 = this.orderFactory.createPackage(customer, PackageStatus.COMPLETE);
        Charge charge1 = this.orderFactory.createCharge(pkg1, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Charge charge2 = this.orderFactory.createCharge(pkg2, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);

        // Call the endpoint
        Map<String, Object> requestBody = Map.of(
                "paymentMethodId", UUID.randomUUID().toString(),
                "totalAmount", 2000
        );
        RequestBuilder requestBuilder = post(String.format("/customers/%s/delivery-payment-requests", customer.getCustomerId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(400);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "Hmm. Payment amount is incorrect. Refresh the page."
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testRequestPaymentForManyOrders_returnsErrorIfNoChargesFound() throws Exception {
        String paymentMethodId = UUID.randomUUID().toString();
        Map<String, Object> requestBody = Map.of(
                "paymentMethodId", paymentMethodId,
                "totalAmount", 2051.28
        );
        RequestBuilder requestBuilder = post(String.format("/customers/%s/delivery-payment-requests", UUID.randomUUID()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(404);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of("message", "No unpaid charges in the specified time range");
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testRequestPaymentForManyOrders_propagatesPaymentApiError() throws Exception {
        // Charges to pay
        Package pkg1 = this.orderFactory.createPackage(customer, PackageStatus.COMPLETE);
        Package pkg2 = this.orderFactory.createPackage(customer, PackageStatus.COMPLETE);
        Charge charge1 = this.orderFactory.createCharge(pkg1, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Charge charge2 = this.orderFactory.createCharge(pkg2, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);

        // Mock payment api response
        Map<String, Object> paymentApiResponse = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "Invalid momo account"
        );
        when(this.httpClientMock.post(eq("null/payment-requests"), any(Map.class)))
                .thenReturn(new HttpResult(paymentApiResponse, HttpStatus.BAD_REQUEST.value()));

        // Call the endpoint
        BigDecimal totalTransactionAmount = charge1.getTransactionAmount().add(charge2.getTransactionAmount());
        BigDecimal transactionFee = ChargeUtils.computeTransactionFeeGivenTransactionAmount(totalTransactionAmount);
        Map<String, Object> requestBody = Map.of(
                "paymentMethodId", UUID.randomUUID().toString(),
                "totalAmount", transactionFee.add(totalTransactionAmount)
        );
        RequestBuilder requestBuilder = post(String.format("/customers/%s/delivery-payment-requests", customer.getCustomerId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate returned response
        assertThat(result.getStatus()).isEqualTo(400);
        Map<String, Object> actualBody = parseResponseBody(result);
        assertThat(actualBody).isEqualTo(paymentApiResponse);
    }

    @Test
    public void testProcessPaymentCallback_withSuccessfulCallback() throws Exception {
        // Business hour record for today
        BusinessHour businessHour = new BusinessHour()
                .setWeekDay(TimeUtils.getUtcNow().getDayOfWeek().getValue())
                .setOpenAt(OffsetTime.MIN)
                .setCloseAt(OffsetTime.MAX)
                .setIsDayOff(false);
        this.businessHourRepository.save(businessHour);

        // Charges and attempts to update
        DeliveryOrder order = this.orderFactory.createOrder();
        Package pkg = this.orderFactory.createPackage(order, PackageSize.SMALL);
        Charge charge1 = this.orderFactory.createCharge(pkg, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Charge charge2 = this.orderFactory.createCharge(pkg, ChargeType.PICK_UP_DELAY, ChargeStatus.UNPAID);
        Discount discount1 = this.orderFactory.createDiscount(order, DiscountType.BATCHING, DiscountStatus.PENDING);
        PaymentRequest paymentRequest = this.orderFactory.createPaymentRequest(Set.of(charge1, charge2), Set.of(discount1));

        // Call the endpoint
        Map<String, Object> requestBody = Map.of(
                "status", "SUCCESS",
                "paymentRequestId", paymentRequest.getPaymentRequestId()
        );
        RequestBuilder requestBuilder = post(String.format("/delivery-payment-requests/%s/callbacks", paymentRequest.getPaymentRequestId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate returned response
        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of("message", "Callback processed successfully");
        assertThat(actualBody).isEqualTo(expectedBody);

        // Validate payment attempts
        paymentRequest = this.paymentRequestRepository.getById(paymentRequest.getPaymentRequestId());
        assertThat(paymentRequest.getIsSuccess()).isTrue();
        assertThat(paymentRequest.getCharges().stream().map(Charge::getChargeId).collect(Collectors.toSet()).contains(charge1.getChargeId())).isTrue();
        assertThat(paymentRequest.getCharges().stream().map(Charge::getChargeId).collect(Collectors.toSet()).contains(charge2.getChargeId())).isTrue();

        // Validate charges
        charge1 = this.chargeRepository.getById(charge1.getChargeId());
        charge2 = this.chargeRepository.getById(charge2.getChargeId());
        assertThat(charge1.getStatus()).isEqualTo(ChargeStatus.PAID);
        assertThat(charge2.getStatus()).isEqualTo(ChargeStatus.PAID);

        // Validate discounts
        discount1 = this.discountRepository.getById(discount1.getDiscountId());
        assertThat(discount1.getStatus()).isEqualTo(DiscountStatus.APPLIED);

        // Validate order placement.
        order = this.orderRepository.getById(order.getDeliveryOrderId());
        pkg = this.packageRepository.getById(pkg.getPackageId());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(pkg.getStatus()).isEqualTo(PackageStatus.PLACED);
    }

    @Test
    public void testProcessPaymentCallback_withUnsuccessfulCallback() throws Exception {
        // Charges and attempts to update
        DeliveryOrder order = this.orderFactory.createOrder();
        Package pkg = this.orderFactory.createPackage(order, PackageSize.SMALL);
        Charge charge1 = this.orderFactory.createCharge(pkg, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Charge charge2 = this.orderFactory.createCharge(pkg, ChargeType.PICK_UP_DELAY, ChargeStatus.UNPAID);
        Discount discount1 = this.orderFactory.createDiscount(order, DiscountType.BATCHING, DiscountStatus.PENDING);
        PaymentRequest paymentRequest = this.orderFactory.createPaymentRequest(Set.of(charge1, charge2), Set.of(discount1));

        // Call the endpoint
        Map<String, Object> requestBody = Map.of(
                "status", "FAILURE",
                "paymentRequestId", paymentRequest.getPaymentRequestId()
        );
        RequestBuilder requestBuilder = post(String.format("/delivery-payment-requests/%s/callbacks", paymentRequest.getPaymentRequestId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate returned response
        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of("message", "Callback processed successfully");
        assertThat(actualBody).isEqualTo(expectedBody);

        // Validate payment attempts
        paymentRequest = this.paymentRequestRepository.getById(paymentRequest.getPaymentRequestId());
        assertThat(paymentRequest.getIsSuccess()).isFalse();
        assertThat(paymentRequest.getCharges().stream().map(Charge::getChargeId).collect(Collectors.toSet()).contains(charge1.getChargeId())).isTrue();
        assertThat(paymentRequest.getCharges().stream().map(Charge::getChargeId).collect(Collectors.toSet()).contains(charge2.getChargeId())).isTrue();

        // Validate charges
        charge1 = this.chargeRepository.getById(charge1.getChargeId());
        charge2 = this.chargeRepository.getById(charge2.getChargeId());
        assertThat(charge1.getStatus()).isEqualTo(ChargeStatus.UNPAID);
        assertThat(charge2.getStatus()).isEqualTo(ChargeStatus.UNPAID);

        // Validate discounts
        discount1 = this.discountRepository.getById(discount1.getDiscountId());
        assertThat(discount1.getStatus()).isEqualTo(DiscountStatus.PENDING);
    }

    @Test
    public void testConfirmOfflinePaymentOneOrder() throws Exception {
        // Charges to pay
        DeliveryOrder order = this.orderFactory.createOrder(OrderStatus.COMPLETE);
        Package pkg = this.orderFactory.createPackage(order, PackageSize.SMALL);
        Charge charge1 = this.orderFactory.createCharge(pkg, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Charge charge2 = this.orderFactory.createCharge(pkg, ChargeType.PICK_UP_DELAY, ChargeStatus.UNPAID);
        Discount discount1 = this.orderFactory.createDiscount(order, DiscountType.BATCHING, DiscountStatus.PENDING);

        // Call the endpoint
        String paymentMethodId = UUID.randomUUID().toString();
        String operatorTransactionId = UUID.randomUUID().toString();
        String paymentTime = TimeUtils.getUtcNow().toString();

        BigDecimal totalTransactionAmount = new BigDecimal("1875.0");
        BigDecimal transactionFee = ChargeUtils.computeTransactionFeeGivenTransactionAmount(totalTransactionAmount);
        Map<String, Object> requestBody = Map.of(
                "paymentMethodId", paymentMethodId,
                "operatorTransactionId", operatorTransactionId,
                "totalAmount", totalTransactionAmount.add(transactionFee),
                "paymentTime", paymentTime,
                "description", "Some description"
        );
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/payment-confirmations", pkg.getDeliveryOrder().getDeliveryOrderId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate returned response
        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of("deliveryOrderId", pkg.getDeliveryOrder().getDeliveryOrderId());
        assertThat(actualBody).isEqualTo(expectedBody);

        // Validate charges
        charge1 = this.chargeRepository.getById(charge1.getChargeId());
        charge2 = this.chargeRepository.getById(charge2.getChargeId());
        assertThat(charge1.getStatus()).isEqualTo(ChargeStatus.PAID);
        assertThat(charge2.getStatus()).isEqualTo(ChargeStatus.PAID);

        // Validate discounts
        discount1 = this.discountRepository.getById(discount1.getDiscountId());
        assertThat(discount1.getStatus()).isEqualTo(DiscountStatus.APPLIED);

        // Validate payment attempts
        PaymentRequest paymentRequest = this.paymentRequestRepository.findByChargesChargeId(charge1.getChargeId()).get(0);
        assertThat(paymentRequest.getIsSuccess()).isTrue();
        assertThat(paymentRequest.getCharges().stream().map(Charge::getChargeId).collect(Collectors.toSet()).contains(charge2.getChargeId())).isTrue();
        assertThat(paymentRequest.getDiscounts().stream().map(Discount::getDiscountId).collect(Collectors.toSet()).contains(discount1.getDiscountId())).isTrue();

        // Validate payment-api payload
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        TransactionBreakdown breakdown = ChargeUtils.getTransactionBreakdown(Set.of(charge1, charge2), Set.of(discount1));
        verify(this.httpClientMock).post(any(), captor.capture());
        Map<String, Object> actualPayload = (Map<String, Object>) captor.getValue();
        Map<String, Object> expectedPayload = Map.of(
                "description", "Some description",
                "totalAmount", breakdown.getTotalAmount().doubleValue(),
                "transactionAmount", breakdown.getTransactionAmount().doubleValue(),
                "transactionFee", breakdown.getTransactionFee().doubleValue(),
                "paymentRequestId", paymentRequest.getPaymentRequestId(),
                "paymentTime", paymentTime,
                "operatorTransactionId", operatorTransactionId,
                "paymentMethod", Map.of(
                        "paymentMethodId", paymentMethodId
                )
        );
        assertThat(actualPayload).isEqualTo(expectedPayload);
    }

    @Test
    public void testConfirmOfflinePaymentOneOrder_returnsErrorIfOrderHasInvalidStatus() throws Exception {
        DeliveryOrder order = this.orderFactory.createOrder(OrderStatus.STARTED);

        Map<String, Object> requestBody = Map.of(
                "paymentMethodId", UUID.randomUUID().toString(),
                "operatorTransactionId", UUID.randomUUID().toString(),
                "totalAmount", 2025.64,
                "paymentTime", TimeUtils.getUtcNow().toString(),
                "description", "Some description"
        );
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/payment-confirmations", order.getDeliveryOrderId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(400);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "Order has an invalid status (must be PLACED or COMPLETE)"
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testConfirmOfflinePaymentOneOrder_returnsErrorIfNoUnpaidChargesFound() throws Exception {
        DeliveryOrder order = this.orderFactory.createOrder(OrderStatus.COMPLETE);

        Map<String, Object> requestBody = Map.of(
                "paymentMethodId", UUID.randomUUID().toString(),
                "operatorTransactionId", UUID.randomUUID().toString(),
                "totalAmount", 2025.64,
                "paymentTime", TimeUtils.getUtcNow().toString(),
                "description", "Some description"
        );
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/payment-confirmations", order.getDeliveryOrderId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(404);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of("message", "No unpaid charges in the specified time range");
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testConfirmOfflinePaymentOneOrder_propagatesErrorFromPaymentApi() throws Exception {
        // Charges to pay
        Package pkg = this.orderFactory.createPackage(PackageStatus.COMPLETE);
        Charge charge1 = this.orderFactory.createCharge(pkg, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Charge charge2 = this.orderFactory.createCharge(pkg, ChargeType.PICK_UP_DELAY, ChargeStatus.UNPAID);

        // Mock payment api response
        Map<String, Object> paymentApiResponse = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "Something went wrong"
        );
        when(this.httpClientMock.post(eq("null/payment-records"), any(Map.class)))
                .thenReturn(new HttpResult(paymentApiResponse, HttpStatus.BAD_REQUEST.value()));

        // Call endpoint
        BigDecimal totalTransactionAmount = charge1.getTransactionAmount().add(charge2.getTransactionAmount());
        BigDecimal transactionFee = ChargeUtils.computeTransactionFeeGivenTransactionAmount(totalTransactionAmount);
        Map<String, Object> requestBody = Map.of(
                "paymentMethodId", UUID.randomUUID().toString(),
                "operatorTransactionId", UUID.randomUUID().toString(),
                "totalAmount", transactionFee.add(totalTransactionAmount),
                "paymentTime", TimeUtils.getUtcNow().toString(),
                "description", "Some description"
        );
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/payment-confirmations", pkg.getDeliveryOrder().getDeliveryOrderId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validation
        assertThat(result.getStatus()).isEqualTo(400);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of("errorMessage", "Something went wrong");
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testConfirmOfflinePaymentOneOrder_returnsErrorForInvalidTotalAmount() throws Exception {
        // Charges to pay
        Package pkg = this.orderFactory.createPackage(PackageStatus.COMPLETE);
        Charge charge1 = this.orderFactory.createCharge(pkg, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Charge charge2 = this.orderFactory.createCharge(pkg, ChargeType.PICK_UP_DELAY, ChargeStatus.UNPAID);

        // Call endpoint
        Map<String, Object> requestBody = Map.of(
                "paymentMethodId", UUID.randomUUID().toString(),
                "operatorTransactionId", UUID.randomUUID().toString(),
                "totalAmount", 2000,
                "paymentTime", TimeUtils.getUtcNow().toString(),
                "description", "Some description"
        );
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/payment-confirmations", pkg.getDeliveryOrder().getDeliveryOrderId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validation
        assertThat(result.getStatus()).isEqualTo(400);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "Hmm. Payment amount is incorrect. Refresh the page."
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testConfirmOfflinePaymentManyOrders_withoutEndAtParameter() throws Exception {
        // Charges to pay
        DeliveryOrder order1 = this.orderFactory.createOrder(customer, OrderStatus.COMPLETE);
        DeliveryOrder order2 = this.orderFactory.createOrder(customer, OrderStatus.COMPLETE);
        Package pkg1 = this.orderFactory.createPackage(order1, PackageSize.SMALL);
        Package pkg2 = this.orderFactory.createPackage(order2, PackageSize.SMALL);
        Charge charge1 = this.orderFactory.createCharge(pkg1, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Charge charge2 = this.orderFactory.createCharge(pkg2, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Discount discount1 = this.orderFactory.createDiscount(order1, DiscountType.BATCHING, DiscountStatus.PENDING);
        Discount discount2 = this.orderFactory.createDiscount(order2, DiscountType.BATCHING, DiscountStatus.PENDING);

        // Call the endpoint
        String paymentMethodId = UUID.randomUUID().toString();
        String operatorTransactionId = UUID.randomUUID().toString();
        String paymentTime = TimeUtils.getUtcNow().toString();

        BigDecimal totalTransactionAmount = new BigDecimal("1800");
        BigDecimal transactionFee = ChargeUtils.computeTransactionFeeGivenTransactionAmount(totalTransactionAmount);
        Map<String, Object> requestBody = Map.of(
                "paymentMethodId", paymentMethodId,
                "operatorTransactionId", operatorTransactionId,
                "totalAmount", totalTransactionAmount.add(transactionFee),
                "paymentTime", paymentTime,
                "description", "Some description"
        );
        RequestBuilder requestBuilder = post(String.format("/customers/%s/delivery-payment-confirmations", customer.getCustomerId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate returned response
        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of("customerId", customer.getCustomerId());
        assertThat(actualBody).isEqualTo(expectedBody);

        // Validate charges
        charge1 = this.chargeRepository.getById(charge1.getChargeId());
        charge2 = this.chargeRepository.getById(charge2.getChargeId());
        assertThat(charge1.getStatus()).isEqualTo(ChargeStatus.PAID);
        assertThat(charge2.getStatus()).isEqualTo(ChargeStatus.PAID);

        // Validate discounts
        discount1 = this.discountRepository.getById(discount1.getDiscountId());
        discount2 = this.discountRepository.getById(discount2.getDiscountId());
        assertThat(discount1.getStatus()).isEqualTo(DiscountStatus.APPLIED);
        assertThat(discount2.getStatus()).isEqualTo(DiscountStatus.APPLIED);

        // Validate payment attempts
        PaymentRequest paymentRequest = this.paymentRequestRepository.findByChargesChargeId(charge1.getChargeId()).get(0);
        assertThat(paymentRequest.getIsSuccess()).isTrue();
        assertThat(paymentRequest.getCharges().size()).isEqualTo(2);
        assertThat(paymentRequest.getCharges().stream().map(Charge::getChargeId).collect(Collectors.toSet()).contains(charge2.getChargeId())).isTrue();
        assertThat(paymentRequest.getDiscounts().size()).isEqualTo(2);
        assertThat(paymentRequest.getDiscounts().stream().map(Discount::getDiscountId).collect(Collectors.toSet()).contains(discount1.getDiscountId())).isTrue();
        assertThat(paymentRequest.getDiscounts().stream().map(Discount::getDiscountId).collect(Collectors.toSet()).contains(discount2.getDiscountId())).isTrue();

        // Validate payment-api payload
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        TransactionBreakdown breakdown = ChargeUtils.getTransactionBreakdown(Set.of(charge1, charge2), Set.of(discount1, discount2));
        verify(this.httpClientMock).post(any(), captor.capture());
        Map<String, Object> actualPayload = (Map<String, Object>) captor.getValue();
        Map<String, Object> expectedPayload = Map.of(
                "description", "Some description",
                "totalAmount", breakdown.getTotalAmount().doubleValue(),
                "transactionAmount", breakdown.getTransactionAmount().doubleValue(),
                "transactionFee", breakdown.getTransactionFee().doubleValue(),
                "paymentRequestId", paymentRequest.getPaymentRequestId(),
                "paymentTime", paymentTime,
                "operatorTransactionId", operatorTransactionId,
                "paymentMethod", Map.of(
                        "paymentMethodId", paymentMethodId
                )
        );
        assertThat(actualPayload).isEqualTo(expectedPayload);
    }

    @Test
    public void testConfirmOfflinePaymentManyOrders_withEndAtParameter() throws Exception {
        // Charges to pay
        DeliveryOrder order1 = this.orderFactory.createOrder(customer, OrderStatus.COMPLETE, TimeUtils.getUtcNow().minusDays(5));
        DeliveryOrder order2 = this.orderFactory.createOrder(customer, OrderStatus.COMPLETE, TimeUtils.getUtcNow().minusDays(4));
        DeliveryOrder order3 = this.orderFactory.createOrder(customer, OrderStatus.COMPLETE, TimeUtils.getUtcNow().minusDays(2));
        Package pkg1 = this.orderFactory.createPackage(order1, PackageSize.SMALL);
        Package pkg2 = this.orderFactory.createPackage(order2, PackageSize.SMALL);
        Package pkg3 = this.orderFactory.createPackage(order3, PackageSize.SMALL);
        Charge charge1 = this.orderFactory.createCharge(pkg1, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Charge charge2 = this.orderFactory.createCharge(pkg2, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Charge charge3 = this.orderFactory.createCharge(pkg3, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Discount discount1 = this.orderFactory.createDiscount(order1, DiscountType.BATCHING, DiscountStatus.PENDING);
        Discount discount2 = this.orderFactory.createDiscount(order2, DiscountType.BATCHING, DiscountStatus.PENDING);
        Discount discount3 = this.orderFactory.createDiscount(order3, DiscountType.BATCHING, DiscountStatus.PENDING);

        // Call the endpoint
        String paymentMethodId = UUID.randomUUID().toString();
        String operatorTransactionId = UUID.randomUUID().toString();
        String paymentTime = TimeUtils.getUtcNow().toString();

        BigDecimal totalTransactionAmount = new BigDecimal("1800");
        BigDecimal transactionFee = ChargeUtils.computeTransactionFeeGivenTransactionAmount(totalTransactionAmount);
        Map<String, Object> requestBody = Map.of(
                "paymentMethodId", paymentMethodId,
                "operatorTransactionId", operatorTransactionId,
                "totalAmount", transactionFee.add(totalTransactionAmount),
                "paymentTime", paymentTime,
                "description", "Some description",
                "endAt", TimeUtils.getUtcNow().minusDays(3).toString()
        );
        RequestBuilder requestBuilder = post(String.format("/customers/%s/delivery-payment-confirmations", customer.getCustomerId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate returned response
        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of("customerId", customer.getCustomerId());
        assertThat(actualBody).isEqualTo(expectedBody);

        // Validate charges
        charge1 = this.chargeRepository.getById(charge1.getChargeId());
        charge2 = this.chargeRepository.getById(charge2.getChargeId());
        assertThat(charge1.getStatus()).isEqualTo(ChargeStatus.PAID);
        assertThat(charge2.getStatus()).isEqualTo(ChargeStatus.PAID);

        // Validate discounts
        discount1 = this.discountRepository.getById(discount1.getDiscountId());
        discount2 = this.discountRepository.getById(discount2.getDiscountId());
        assertThat(discount1.getStatus()).isEqualTo(DiscountStatus.APPLIED);
        assertThat(discount2.getStatus()).isEqualTo(DiscountStatus.APPLIED);

        // Validate payment attempts
        PaymentRequest paymentRequest = this.paymentRequestRepository.findByChargesChargeId(charge1.getChargeId()).get(0);
        assertThat(paymentRequest.getIsSuccess()).isTrue();
        assertThat(paymentRequest.getCharges().size()).isEqualTo(2);
        assertThat(paymentRequest.getCharges().stream().map(Charge::getChargeId).collect(Collectors.toSet()).contains(charge2.getChargeId())).isTrue();
        assertThat(paymentRequest.getDiscounts().size()).isEqualTo(2);
        assertThat(paymentRequest.getDiscounts().stream().map(Discount::getDiscountId).collect(Collectors.toSet()).contains(discount1.getDiscountId())).isTrue();
        assertThat(paymentRequest.getDiscounts().stream().map(Discount::getDiscountId).collect(Collectors.toSet()).contains(discount2.getDiscountId())).isTrue();

        // Validate payment-api payload
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        TransactionBreakdown breakdown = ChargeUtils.getTransactionBreakdown(Set.of(charge1, charge2), Set.of(discount1, discount2));
        verify(this.httpClientMock).post(any(), captor.capture());
        Map<String, Object> actualPayload = (Map<String, Object>) captor.getValue();
        Map<String, Object> expectedPayload = Map.of(
                "description", "Some description",
                "totalAmount", breakdown.getTotalAmount().doubleValue(),
                "transactionAmount", breakdown.getTransactionAmount().doubleValue(),
                "transactionFee", breakdown.getTransactionFee().doubleValue(),
                "paymentRequestId", paymentRequest.getPaymentRequestId(),
                "paymentTime", paymentTime,
                "operatorTransactionId", operatorTransactionId,
                "paymentMethod", Map.of(
                        "paymentMethodId", paymentMethodId
                )
        );
        assertThat(actualPayload).isEqualTo(expectedPayload);
    }

    @Test
    public void testGetPaymentStatus() throws Exception {
        // Create charges
        Package pkg = this.orderFactory.createPackage(PackageStatus.STARTED);
        Charge charge1 = this.orderFactory.createCharge(pkg, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        PaymentRequest paymentRequest = this.orderFactory.createPaymentRequest(Set.of(charge1), Set.of());

        // Call endpoint
        RequestBuilder requestBuilder = get(String.format("/delivery-payment-requests/%s/payment-status", paymentRequest.getPaymentRequestId()));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validation
        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of("paymentStatus", PaymentStatus.UNPAID.name());
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetCustomerSpending() throws Exception {
        // Charges & discounts
        DeliveryOrder order1 = this.orderFactory.createOrder(customer, OrderStatus.COMPLETE);
        DeliveryOrder order2 = this.orderFactory.createOrder(customer, OrderStatus.COMPLETE);
        Package pkg1 = this.orderFactory.createPackage(order1, PackageSize.SMALL);
        Package pkg2 = this.orderFactory.createPackage(order2, PackageSize.SMALL);
        Charge charge1 = this.orderFactory.createCharge(pkg1, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Charge charge2 = this.orderFactory.createCharge(pkg2, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Discount discount1 = this.orderFactory.createDiscount(order1, DiscountType.BATCHING, DiscountStatus.PENDING);
        Discount discount2 = this.orderFactory.createDiscount(order2, DiscountType.BATCHING, DiscountStatus.PENDING);

        // Call endpoint
        RequestBuilder requestBuilder = get("/customers/" + customer.getCustomerId() + "/delivery-spending");
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate repsonse
        assertThat(result.getStatus()).isEqualTo(200);

        BigDecimal totalTransactionAmount = new BigDecimal("1800");
        BigDecimal transactionFee = ChargeUtils.computeTransactionFeeGivenTransactionAmount(totalTransactionAmount);
        Map<String, Object> responseBody = parseResponseBody(result);

        assertThat(responseBody.get("totalAmount")).isEqualTo(totalTransactionAmount.add(transactionFee).doubleValue());
        assertThat(responseBody.get("transactionAmount")).isEqualTo(totalTransactionAmount.doubleValue());
        assertThat(responseBody.get("transactionFee")).isEqualTo(transactionFee.doubleValue());
    }

    @Test
    public void testGetCustomerSpending_filtersByEndAt() throws Exception {
        // Charges & discounts
        DeliveryOrder order1 = this.orderFactory.createOrder(customer, OrderStatus.COMPLETE, TimeUtils.getUtcNow().minusDays(5));
        DeliveryOrder order2 = this.orderFactory.createOrder(customer, OrderStatus.COMPLETE, TimeUtils.getUtcNow().minusDays(4));
        DeliveryOrder order3 = this.orderFactory.createOrder(customer, OrderStatus.COMPLETE, TimeUtils.getUtcNow().minusDays(2));
        Package pkg1 = this.orderFactory.createPackage(order1, PackageSize.SMALL);
        Package pkg2 = this.orderFactory.createPackage(order2, PackageSize.SMALL);
        Package pkg3 = this.orderFactory.createPackage(order3, PackageSize.SMALL);
        Charge charge1 = this.orderFactory.createCharge(pkg1, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Charge charge2 = this.orderFactory.createCharge(pkg2, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Charge charge3 = this.orderFactory.createCharge(pkg3, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Discount discount1 = this.orderFactory.createDiscount(order1, DiscountType.BATCHING, DiscountStatus.PENDING);
        Discount discount2 = this.orderFactory.createDiscount(order2, DiscountType.BATCHING, DiscountStatus.PENDING);
        Discount discount3 = this.orderFactory.createDiscount(order3, DiscountType.BATCHING, DiscountStatus.PENDING);

        // Call endpoint
        OffsetDateTime endAt = TimeUtils.getUtcNow().minusDays(3);
        RequestBuilder requestBuilder = get("/customers/" + customer.getCustomerId() + "/delivery-spending?endAt=" + endAt);
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate response
        assertThat(result.getStatus()).isEqualTo(200);

        BigDecimal totalTransactionAmount = new BigDecimal("1800");
        BigDecimal transactionFee = ChargeUtils.computeTransactionFeeGivenTransactionAmount(totalTransactionAmount);
        Map<String, Object> responseBody = parseResponseBody(result);

        assertThat(responseBody.get("totalAmount")).isEqualTo(totalTransactionAmount.add(transactionFee).doubleValue());
        assertThat(responseBody.get("transactionAmount")).isEqualTo(totalTransactionAmount.doubleValue());
        assertThat(responseBody.get("transactionFee")).isEqualTo(transactionFee.doubleValue());
    }

    @Test
    public void testGetCustomerSpending_filtersBranchId() throws Exception {
        // Charges & discounts
        Branch branch1 = this.orderFactory.createBranch(customer);
        Branch branch2 = this.orderFactory.createBranch(customer);
        Agent agent1 = this.orderFactory.createAgent(branch1);
        Agent agent2 = this.orderFactory.createAgent(branch2);
        DeliveryOrder order1 = this.orderFactory.createOrder(agent1, OrderStatus.COMPLETE);
        DeliveryOrder order2 = this.orderFactory.createOrder(agent1, OrderStatus.COMPLETE);
        DeliveryOrder order3 = this.orderFactory.createOrder(agent2, OrderStatus.COMPLETE);
        Package pkg1 = this.orderFactory.createPackage(order1, PackageSize.SMALL);
        Package pkg2 = this.orderFactory.createPackage(order2, PackageSize.SMALL);
        Package pkg3 = this.orderFactory.createPackage(order3, PackageSize.SMALL);
        Charge charge1 = this.orderFactory.createCharge(pkg1, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Charge charge2 = this.orderFactory.createCharge(pkg2, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Charge charge3 = this.orderFactory.createCharge(pkg3, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Discount discount1 = this.orderFactory.createDiscount(order1, DiscountType.BATCHING, DiscountStatus.PENDING);
        Discount discount2 = this.orderFactory.createDiscount(order2, DiscountType.BATCHING, DiscountStatus.PENDING);
        Discount discount3 = this.orderFactory.createDiscount(order3, DiscountType.BATCHING, DiscountStatus.PENDING);

        // Call the endpoint
        RequestBuilder requestBuilder = get("/customers/" + customer.getCustomerId() + "/delivery-spending?branchId=" + branch1.getBranchId());
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate response
        assertThat(result.getStatus()).isEqualTo(200);

        BigDecimal totalTransactionAmount = new BigDecimal("1800");
        BigDecimal transactionFee = ChargeUtils.computeTransactionFeeGivenTransactionAmount(totalTransactionAmount);
        Map<String, Object> responseBody = parseResponseBody(result);

        assertThat(responseBody.get("totalAmount")).isEqualTo(totalTransactionAmount.add(transactionFee).doubleValue());
        assertThat(responseBody.get("transactionAmount")).isEqualTo(totalTransactionAmount.doubleValue());
        assertThat(responseBody.get("transactionFee")).isEqualTo(transactionFee.doubleValue());
    }

    @Test
    public void testGetBillingStatus_billIsNotDueBeforeBillingInterval() throws Exception {
        DeliveryOrder order1 = this.orderFactory.createOrder(customer, OrderStatus.COMPLETE);
        Package pkg1 = this.orderFactory.createPackage(order1, PackageSize.SMALL);
        Charge charge1 = this.orderFactory.createCharge(pkg1, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);

        RequestBuilder requestBuilder = get("/customers/" + customer.getCustomerId() + "/billing-status");
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "isBillDue", false,
                "gracePeriod", 9
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetBillingStatus_billIsDuePastBillingInterval() throws Exception {
        DeliveryOrder earliestOrder = this.orderFactory.createOrder(customer, OrderStatus.COMPLETE, TimeUtils.getUtcNow().minusDays(8));
        DeliveryOrder latestOrder = this.orderFactory.createOrder(customer, OrderStatus.COMPLETE);
        Package pkg1 = this.orderFactory.createPackage(earliestOrder, PackageSize.SMALL);
        Package pkg2 = this.orderFactory.createPackage(latestOrder, PackageSize.SMALL);
        Charge charge1 = this.orderFactory.createCharge(pkg1, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Charge charge2 = this.orderFactory.createCharge(pkg2, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);

        RequestBuilder requestBuilder = get("/customers/" + customer.getCustomerId() + "/billing-status");
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "isBillDue", true,
                "gracePeriod", 1
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetBillingStatus_billIsDuePastBillingGracePeriod() throws Exception {
        DeliveryOrder earliestOrder = this.orderFactory.createOrder(customer, OrderStatus.COMPLETE, TimeUtils.getUtcNow().minusDays(30));
        DeliveryOrder latestOrder = this.orderFactory.createOrder(customer, OrderStatus.COMPLETE);
        Package pkg1 = this.orderFactory.createPackage(earliestOrder, PackageSize.SMALL);
        Package pkg2 = this.orderFactory.createPackage(latestOrder, PackageSize.SMALL);
        Charge charge1 = this.orderFactory.createCharge(pkg1, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);
        Charge charge2 = this.orderFactory.createCharge(pkg2, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID);

        RequestBuilder requestBuilder = get("/customers/" + customer.getCustomerId() + "/billing-status");
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "isBillDue", true,
                "gracePeriod", 0
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }
}
