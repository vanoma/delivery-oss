package com.vanoma.api.order.payment;

import com.vanoma.api.order.utils.annotations.RequestMappingJson;
import com.vanoma.api.order.utils.annotations.PostMappingJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMappingJson
public class PaymentController {

    @Autowired
    private IPaymentService paymentService;

    @PostMappingJson(value = "/delivery-orders/{deliveryOrderId}/payment-requests")
    public ResponseEntity<Map<String, Object>> requestPaymentOneOrder(@PathVariable String deliveryOrderId,
                                                                      @RequestBody PaymentRequestOneOrderJson paymentRequestOneOrderJson) {
        return this.paymentService.requestPaymentOneOrder(deliveryOrderId, paymentRequestOneOrderJson);
    }

    @PostMappingJson(value = "/delivery-orders/{deliveryOrderId}/payment-confirmations")
    public ResponseEntity<Map<String, Object>> confirmPaymentOneOrder(@PathVariable String deliveryOrderId,
                                                                      @RequestBody OfflinePaymentOneOrderJson json) {
        return this.paymentService.confirmOfflinePaymentOneOrder(deliveryOrderId, json);
    }

    @PostMappingJson(value = "/customers/{customerId}/delivery-payment-requests")
    public ResponseEntity<Map<String, Object>> requestPaymentManyOrders(@PathVariable String customerId,
                                                                        @RequestBody PaymentRequestManyOrdersJson json) {
        return this.paymentService.requestPaymentManyOrders(customerId, json);
    }

    @PostMappingJson(value = "/customers/{customerId}/delivery-payment-confirmations")
    public ResponseEntity<Map<String, Object>> confirmPaymentManyOrders(@PathVariable String customerId,
                                                                        @RequestBody OfflinePaymentManyOrdersJson json) {
        return this.paymentService.confirmOfflinePaymentManyOrders(customerId, json);
    }

    @PostMappingJson(value = "/delivery-payment-requests/{deliveryPaymentRequestId}/callbacks")
    public ResponseEntity<Map<String, Object>> processPaymentCallback(@PathVariable String deliveryPaymentRequestId,
                                                                      @RequestBody PaymentCallbackJson paymentCallbackJson) {
        return this.paymentService.processPaymentCallback(deliveryPaymentRequestId, paymentCallbackJson);
    }

    @GetMapping(value = "/delivery-payment-requests/{deliveryPaymentRequestId}/payment-status")
    public ResponseEntity<Map<String, Object>> getPaymentRequestStatus(@PathVariable String deliveryPaymentRequestId) {
        return this.paymentService.getPaymentRequestStatus(deliveryPaymentRequestId);
    }

    @GetMapping(value = "/customers/{customerId}/delivery-spending")
    public ResponseEntity<Map<String, Object>> getCustomerBill(@PathVariable String customerId,
                                                               @RequestParam(required = false) String endAt,
                                                               @RequestParam(required = false) String branchId) {
        return ResponseEntity.ok(this.paymentService.getCustomerSpending(customerId, endAt, branchId));
    }

    @GetMapping(value = "/customers/{customerId}/billing-status")
    public ResponseEntity<Map<String, Object>> getBillingStatus(@PathVariable String customerId) {
        return ResponseEntity.ok(this.paymentService.getBillingStatus(customerId));
    }

}
