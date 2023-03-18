package com.vanoma.api.order.charges;

import com.vanoma.api.order.payment.PaymentRequestRepository;
import com.vanoma.api.order.tests.OrderFactory;
import com.vanoma.api.order.tests.ResourceMapper;
import com.vanoma.api.order.packages.Package;
import com.vanoma.api.utils.NullableValueMapBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.vanoma.api.order.tests.ControllerTestUtils.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class ChargeControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private OrderFactory orderFactory;
    @Autowired
    private ChargeRepository chargeRepository;
    @Autowired
    private PaymentRequestRepository paymentRequestRepository;

    @BeforeEach
    public void setUp() {
        // Delete existing charges to avoid polluting the next test. We have to corresponding payment
        // attempts first because cascading deletes is not working :(
        this.paymentRequestRepository.deleteAll();
        this.chargeRepository.deleteAll();
    }

    @Test
    public void testCreateCharge() throws Exception {
        Package pkg = this.orderFactory.createPackage();
        Map<String, Object> requestBody = Map.of(
                "type", ChargeType.PICK_UP_DELAY,
                "transactionAmount", "1000",
                "description", "Pickup delay"
        );

        RequestBuilder requestBuilder = post("/packages/" + pkg.getPackageId() + "/charges")
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(201);

        List<Charge> charges = this.chargeRepository.findByPkg(pkg);
        assertThat(charges.size()).isEqualTo(1);
        assertThat(charges.get(0).getType()).isEqualTo(ChargeType.PICK_UP_DELAY);
        assertThat(charges.get(0).getStatus()).isEqualTo(ChargeStatus.UNPAID);
        assertThat(charges.get(0).getDescription()).isEqualTo("Pickup delay");
        assertThat(charges.get(0).getTransactionAmount()).isEqualTo(new BigDecimal("975.00"));
        assertThat(charges.get(0).getActualTransactionAmount()).isNull();

        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createChargeMap(charges.get(0));
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testCreateCharge_returnsErrorIfChargeTypeIsDeliveryFee() throws Exception {
        Package pkg = this.orderFactory.createPackage();
        Map<String, Object> requestBody = Map.of(
                "type", ChargeType.DELIVERY_FEE,
                "transactionAmount", "1000",
                "description", "Pickup delay"
        );

        RequestBuilder requestBuilder = post("/packages/" + pkg.getPackageId() + "/charges")
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(400);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage","You can not create a DELIVERY_FEE charge type via endpoint"
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetPackageCharges() throws Exception {
        Package pkg = this.orderFactory.createPackage();
        Charge charge1 = this.orderFactory.createCharge(pkg, ChargeStatus.UNPAID);
        Charge charge2 = this.orderFactory.createCharge(pkg, ChargeStatus.PAID);

        RequestBuilder requestBuilder = get("/packages/" + pkg.getPackageId() + "/charges");
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = new NullableValueMapBuilder<String, Object>()
                .put("count", 2)
                .put("next", null)
                .put("previous", null)
                .put("results", List.of(ResourceMapper.createChargeMap(charge1), ResourceMapper.createChargeMap(charge2)))
                .build();
        assertThat(actualBody).isEqualTo(expectedBody);
    }
}
