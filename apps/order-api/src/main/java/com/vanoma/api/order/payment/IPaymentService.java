package com.vanoma.api.order.payment;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface IPaymentService {
    ResponseEntity<Map<String, Object>> requestPaymentOneOrder(String deliveryOrderId, PaymentRequestOneOrderJson json);

    ResponseEntity<Map<String, Object>> requestPaymentManyOrders(String customerId, PaymentRequestManyOrdersJson json);

    ResponseEntity<Map<String, Object>> processPaymentCallback(String requestId, PaymentCallbackJson json);

    ResponseEntity<Map<String, Object>> confirmOfflinePaymentOneOrder(String deliveryOrderId, OfflinePaymentOneOrderJson json);

    ResponseEntity<Map<String, Object>> confirmOfflinePaymentManyOrders(String customerId, OfflinePaymentManyOrdersJson json);

    ResponseEntity<Map<String, Object>> getPaymentRequestStatus(String paymentRequestId);

    Map<String, Object> getCustomerSpending(String customerId, String endAt, String branchId);

    Map<String, Object> getBillingStatus(String customerId);
}
