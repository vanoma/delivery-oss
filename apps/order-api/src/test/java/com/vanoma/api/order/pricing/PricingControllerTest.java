package com.vanoma.api.order.pricing;

import com.vanoma.api.order.charges.*;
import com.vanoma.api.order.customers.Customer;
import com.vanoma.api.order.customers.CustomerRepository;
import com.vanoma.api.order.maps.Coordinates;
import com.vanoma.api.order.maps.IGeocodingService;
import com.vanoma.api.order.maps.INavigationDistanceApi;
import com.vanoma.api.order.orders.*;
import com.vanoma.api.order.packages.Package;
import com.vanoma.api.order.packages.PackageRepository;
import com.vanoma.api.order.packages.PackageSize;
import com.vanoma.api.order.tests.OrderFactory;
import com.vanoma.api.utils.exceptions.InvalidParameterException;
import com.vanoma.api.utils.input.TimeUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static com.vanoma.api.order.tests.ControllerTestUtils.parseResponseBody;
import static com.vanoma.api.order.tests.ControllerTestUtils.stringifyRequestBody;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class PricingControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private OrderFactory orderFactory;
    @Autowired
    private ChargeRepository chargeRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private DiscountRepository discountRepository;
    @Autowired
    private PackageRepository packageRepository;
    @MockBean
    private IGeocodingService geocodingService;
    @MockBean
    private INavigationDistanceApi navigationDistanceApi;

    @Test
    public void testGetDeliveryPricing_getPriceForSmallPackageSize() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "packages", List.of(Map.of(
                        "size", "SMALL",
                        "origin", Map.of(
                                "latitude", -1.939308,
                                "longitude", 30.1312124
                        ),
                        "destination", Map.of(
                                "latitude", -1.9518833,
                                "longitude", 30.1369548
                        )
                ))
        );

        RequestBuilder requestBuilder = post("/delivery-pricing")
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "totalAmount", 1200,
                "transactionAmount", 1170,
                "transactionFee", 30
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetDeliveryPricing_getPriceForMediumPackageSize() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "packages", List.of(Map.of(
                        "size", "MEDIUM",
                        "origin", Map.of(
                                "latitude", -1.939308,
                                "longitude", 30.1312124
                        ),
                        "destination", Map.of(
                                "latitude", -1.9518833,
                                "longitude", 30.1369548
                        )
                ))
        );

        RequestBuilder requestBuilder = post("/delivery-pricing")
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "totalAmount", 1800,
                "transactionAmount", 1755,
                "transactionFee", 45
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetDeliveryPricing_getPriceForLargePackageSize() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "packages", List.of(Map.of(
                        "size", "LARGE",
                        "origin", Map.of(
                                "latitude", -1.939308,
                                "longitude", 30.1312124
                        ),
                        "destination", Map.of(
                                "latitude", -1.9518833,
                                "longitude", 30.1369548
                        )
                ))
        );

        RequestBuilder requestBuilder = post("/delivery-pricing")
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "totalAmount", 4800,
                "transactionAmount", 4680,
                "transactionFee", 120
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetDeliveryPricing_returns400WhenLocationIsNotInKigali() throws Exception {
        when(this.geocodingService.reverseGeocode(any(Coordinates.class)))
                .thenThrow(new InvalidParameterException("crud.address.outOfKigali"));

        Map<String, Object> requestBody = Map.of(
                "packages", List.of(Map.of(
                        "volume", 0.025,
                        "origin", Map.of(
                                "latitude", -1.939308,
                                "longitude", 30.1312124
                        ),
                        "destination", Map.of(
                                "latitude", -1.9518833,
                                "longitude", 30.1369548
                        )
                ))
        );

        RequestBuilder requestBuilder = post("/delivery-pricing")
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(400);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "We currently deliver within Kigali only"
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetDeliveryPricing_returns400WhenLocationIsNotInRwanda() throws Exception {
        when(this.geocodingService.reverseGeocode(any(Coordinates.class)))
                .thenThrow(new InvalidParameterException("crud.address.outOfRwanda"));

        Map<String, Object> requestBody = Map.of(
                "packages", List.of(Map.of(
                        "volume", 0.025,
                        "origin", Map.of(
                                "latitude", -1.939308,
                                "longitude", 30.1312124
                        ),
                        "destination", Map.of(
                                "latitude", -1.9518833,
                                "longitude", 30.1369548
                        )
                ))
        );

        RequestBuilder requestBuilder = post("/delivery-pricing")
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(400);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "The selected location seems outside of Rwanda"
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetPricingForOrder_withPrepaidAccountAndRegularPrice() throws Exception {
        // Create order
        Customer customer = this.orderFactory.createCustomer();
        DeliveryOrder order = this.orderFactory.createOrderWithPackage(customer, PackageSize.SMALL);

        // Call endpoint
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/pricing", order.getDeliveryOrderId()));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate response
        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "isPrepaid", true,
                "totalAmount", 1200,
                "transactionAmount", 1170,
                "transactionFee", 30
        );
        assertThat(actualBody).isEqualTo(expectedBody);

        // Validate charge
        List<Charge> charges = this.chargeRepository.findByDeliveryOrder(order);
        assertThat(charges.size()).isEqualTo(1);
        assertThat(charges.get(0).getTotalAmount()).isEqualTo(new BigDecimal("1200.00"));
        assertThat(charges.get(0).getStatus()).isEqualTo(ChargeStatus.UNPAID);
        assertThat(charges.get(0).getType()).isEqualTo(ChargeType.DELIVERY_FEE);
        assertThat(charges.get(0).getTransactionAmount()).isEqualTo(new BigDecimal("1170.00"));
        assertThat(charges.get(0).getActualTransactionAmount()).isEqualTo(new BigDecimal("1170.00"));

        // Validate discount
        assertThat(this.discountRepository.findFirstByDeliveryOrderAndType(order, DiscountType.BATCHING)).isNull();
    }

    @Test
    public void testGetPricingForOrder_withPrepaidAccountAndFixedPrice() throws Exception {
        // Create required data
        Customer customer = this.orderFactory.createCustomer(BigDecimal.valueOf(1200), TimeUtils.getUtcNow().plusDays(1));
        DeliveryOrder order = this.orderFactory.createOrderWithPackage(customer, PackageSize.SMALL);

        // Call endpoint
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/pricing", order.getDeliveryOrderId()));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate response
        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "isPrepaid", true,
                "totalAmount", 1200,
                "transactionAmount", 1170,
                "transactionFee", 30
        );
        assertThat(actualBody).isEqualTo(expectedBody);

        // Validate charge
        List<Charge> charges = this.chargeRepository.findByDeliveryOrder(order);
        assertThat(charges.size()).isEqualTo(1);
        assertThat(charges.get(0).getTotalAmount()).isEqualTo(new BigDecimal("1200.00"));
        assertThat(charges.get(0).getStatus()).isEqualTo(ChargeStatus.UNPAID);
        assertThat(charges.get(0).getType()).isEqualTo(ChargeType.DELIVERY_FEE);
        assertThat(charges.get(0).getTransactionAmount()).isEqualTo(new BigDecimal("1170.00"));

        // Validate discount
        assertThat(this.discountRepository.findFirstByDeliveryOrderAndType(order, DiscountType.BATCHING)).isNull();
    }

    @Test
    public void testGetPricingForOrder_withPostpaidAccountAndRegularPrice() throws Exception {
        // Create order
        Customer customer = this.orderFactory.createCustomer(TimeUtils.getUtcNow().plusDays(1));
        DeliveryOrder order = this.orderFactory.createOrderWithPackage(customer, PackageSize.SMALL);

        // Call endpoint
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/pricing", order.getDeliveryOrderId()));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate response
        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "isPrepaid", false,
                "totalAmount", 1200,
                "transactionAmount", 1170,
                "transactionFee", 30
        );
        assertThat(actualBody).isEqualTo(expectedBody);

        // Validate charge
        List<Charge> charges = this.chargeRepository.findByDeliveryOrder(order);
        assertThat(charges.size()).isEqualTo(1);
        assertThat(charges.get(0).getTotalAmount()).isEqualTo(new BigDecimal("1200.00"));
        assertThat(charges.get(0).getStatus()).isEqualTo(ChargeStatus.UNPAID);
        assertThat(charges.get(0).getType()).isEqualTo(ChargeType.DELIVERY_FEE);
        assertThat(charges.get(0).getTransactionAmount()).isEqualTo(new BigDecimal("1170.00"));

        // Validate discount
        assertThat(this.discountRepository.findFirstByDeliveryOrderAndType(order, DiscountType.BATCHING)).isNull();
    }

    @Test
    public void testGetPricingForOrder_withPostpaidAccountAndFixedPrice() throws Exception {
        // Create required data
        OffsetDateTime expiry = TimeUtils.getUtcNow().plusDays(1);
        Customer customer = this.customerRepository.save(this.orderFactory.createCustomer(expiry).setFixedPriceAmount(BigDecimal.valueOf(1200)).setFixedPriceExpiry(expiry));
        DeliveryOrder order = this.orderFactory.createOrderWithPackage(customer, PackageSize.SMALL);

        // Call endpoint
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/pricing", order.getDeliveryOrderId()));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate response
        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "isPrepaid", false,
                "totalAmount", 1200,
                "transactionAmount", 1170,
                "transactionFee", 30
        );
        assertThat(actualBody).isEqualTo(expectedBody);

        // Validate charge
        List<Charge> charges = this.chargeRepository.findByDeliveryOrder(order);
        assertThat(charges.size()).isEqualTo(1);
        assertThat(charges.get(0).getTotalAmount()).isEqualTo(new BigDecimal("1200.00"));
        assertThat(charges.get(0).getStatus()).isEqualTo(ChargeStatus.UNPAID);
        assertThat(charges.get(0).getType()).isEqualTo(ChargeType.DELIVERY_FEE);
        assertThat(charges.get(0).getTransactionAmount()).isEqualTo(new BigDecimal("1170.00"));

        // Validate discount
        assertThat(this.discountRepository.findFirstByDeliveryOrderAndType(order, DiscountType.BATCHING)).isNull();
    }

    @Test
    public void testGetPricingForOrder_withWeightingFactorAndRegularPrice() throws Exception {
        // Create required data
        Customer customer = this.orderFactory.createCustomer(new BigDecimal("4.00"));
        DeliveryOrder order = this.orderFactory.createOrderWithPackage(customer, PackageSize.SMALL);

        // Call endpoint
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/pricing", order.getDeliveryOrderId()));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        BigDecimal expectedTransactionAmount = new BigDecimal("1170").multiply(new BigDecimal("4.00"));
        BigDecimal expectedTransactionAmountRounded = expectedTransactionAmount.setScale(0, RoundingMode.UP);
        BigDecimal expectedTransactionFee = ChargeUtils.computeTransactionFeeGivenTransactionAmount(expectedTransactionAmount);
        BigDecimal expectedTransactionFeeRounded = expectedTransactionFee.setScale(0, RoundingMode.UP);
        BigDecimal expectedTotalAmount = expectedTransactionAmount.add(expectedTransactionFee);
        BigDecimal expectedTotalAmountRounded = expectedTotalAmount.setScale(0, RoundingMode.UP);

        // Validate response
        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "isPrepaid", true,
                "totalAmount", expectedTotalAmountRounded.intValue(),
                "transactionAmount", expectedTransactionAmountRounded.intValue(),
                "transactionFee", expectedTransactionFeeRounded.intValue()
        );
        assertThat(actualBody).isEqualTo(expectedBody);

        // Validate charge
        List<Charge> charges = this.chargeRepository.findByDeliveryOrder(order);
        assertThat(charges.size()).isEqualTo(1);
        assertThat(charges.get(0).getTotalAmount()).isEqualTo(expectedTotalAmount);
        assertThat(charges.get(0).getStatus()).isEqualTo(ChargeStatus.UNPAID);
        assertThat(charges.get(0).getType()).isEqualTo(ChargeType.DELIVERY_FEE);
        assertThat(charges.get(0).getTransactionAmount()).isEqualTo(expectedTransactionAmount);
        assertThat(charges.get(0).getActualTransactionAmount()).isEqualTo(expectedTransactionAmount);

        // Validate discount
        assertThat(this.discountRepository.findFirstByDeliveryOrderAndType(order, DiscountType.BATCHING)).isNull();
    }

    @Test
    public void testGetPricingForOrder_withWeightingFactorAndFixedPrice() throws Exception {
        // Create required data
        OffsetDateTime expiry = TimeUtils.getUtcNow().plusDays(1);
        BigDecimal weightFactor = new BigDecimal("4");
        Customer customer = this.customerRepository.save(this.orderFactory.createCustomer(weightFactor).setFixedPriceAmount(BigDecimal.valueOf(1200)).setFixedPriceExpiry(expiry));
        DeliveryOrder order = this.orderFactory.createOrderWithPackage(customer, PackageSize.SMALL);

        // Call endpoint
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/pricing", order.getDeliveryOrderId()));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate response
        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "isPrepaid", true,
                "totalAmount", 1200,
                "transactionAmount", 1170,
                "transactionFee", 30
        );
        assertThat(actualBody).isEqualTo(expectedBody);

        // Validate charge
        List<Charge> charges = this.chargeRepository.findByDeliveryOrder(order);
        assertThat(charges.size()).isEqualTo(1);
        assertThat(charges.get(0).getTotalAmount()).isEqualTo(new BigDecimal("1200.00"));
        assertThat(charges.get(0).getStatus()).isEqualTo(ChargeStatus.UNPAID);
        assertThat(charges.get(0).getType()).isEqualTo(ChargeType.DELIVERY_FEE);
        assertThat(charges.get(0).getTransactionAmount()).isEqualTo(new BigDecimal("1170.00"));

        // Validate discount
        assertThat(this.discountRepository.findFirstByDeliveryOrderAndType(order, DiscountType.BATCHING)).isNull();
    }

    @Test
    public void testGetPricingForOrder_withMultiplePackages() throws Exception {
        // Create order and packages
        DeliveryOrder order = this.orderFactory.createOrder();
        List<Package> packages = List.of(
                this.orderFactory.createPackage(order, PackageSize.SMALL),
                this.orderFactory.createPackage(order, PackageSize.MEDIUM)
        );
        this.packageRepository.save(packages.get(1).setFromAddress(packages.get(0).getFromAddress()));

        // Call endpoint
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/pricing", order.getDeliveryOrderId()));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate response
        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "isPrepaid", true,
                "totalAmount", 3000,
                "transactionAmount", 2925,
                "transactionFee", 75
        );
        assertThat(actualBody).isEqualTo(expectedBody);

        // Validate charges
        Charge firstCharge = this.chargeRepository.findFirstByPkgAndType(packages.get(0), ChargeType.DELIVERY_FEE);
        assertThat(firstCharge).isNotNull();
        assertThat(firstCharge.getTotalAmount()).isEqualTo(new BigDecimal("1200.00"));
        assertThat(firstCharge.getStatus()).isEqualTo(ChargeStatus.UNPAID);
        assertThat(firstCharge.getTransactionAmount()).isEqualTo(new BigDecimal("1170.00"));

        Charge secondCharge = this.chargeRepository.findFirstByPkgAndType(packages.get(1), ChargeType.DELIVERY_FEE);
        assertThat(secondCharge).isNotNull();
        assertThat(secondCharge.getTotalAmount()).isEqualTo(new BigDecimal("1800.00"));
        assertThat(secondCharge.getStatus()).isEqualTo(ChargeStatus.UNPAID);
        assertThat(secondCharge.getTransactionAmount()).isEqualTo(new BigDecimal("1755.00"));
    }
    @Test
    public void testGetPricingForOrder_updatesExistingDeliveryFee() throws Exception {
        // Create required data
        DeliveryOrder order = this.orderFactory.createOrder();
        Package pkg1 = this.orderFactory.createPackage(order, PackageSize.MEDIUM);
        Package pkg2 = this.orderFactory.createPackage(order, PackageSize.MEDIUM);
        this.packageRepository.save(pkg2.setFromAddress(pkg1.getFromAddress()));

        Charge existingCharge1 = this.orderFactory.createCharge(pkg1, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID, BigDecimal.valueOf(1200));
        Charge existingCharge2 = this.orderFactory.createCharge(pkg2, ChargeType.DELIVERY_FEE, ChargeStatus.UNPAID, BigDecimal.valueOf(1200));

        // Call endpoint
        RequestBuilder requestBuilder = post(String.format("/delivery-orders/%s/pricing", order.getDeliveryOrderId()));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate response
        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "isPrepaid", true,
                "totalAmount", 3600,
                "transactionAmount", 3510,
                "transactionFee", 90
        );
        assertThat(actualBody).isEqualTo(expectedBody);

        // Validate charges
        existingCharge1 = this.chargeRepository.getById(existingCharge1.getChargeId());
        assertThat(existingCharge1.getTotalAmount()).isEqualTo(new BigDecimal("1800.00"));
        assertThat(existingCharge1.getStatus()).isEqualTo(ChargeStatus.UNPAID);
        assertThat(existingCharge1.getType()).isEqualTo(ChargeType.DELIVERY_FEE);

        existingCharge2 = this.chargeRepository.getById(existingCharge2.getChargeId());
        assertThat(existingCharge2.getTotalAmount()).isEqualTo(new BigDecimal("1800.00"));
        assertThat(existingCharge2.getStatus()).isEqualTo(ChargeStatus.UNPAID);
        assertThat(existingCharge2.getType()).isEqualTo(ChargeType.DELIVERY_FEE);
    }
}
