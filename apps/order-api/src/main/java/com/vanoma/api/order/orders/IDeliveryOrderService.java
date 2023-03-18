package com.vanoma.api.order.orders;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface IDeliveryOrderService {

    // TODO: Move ResponseEntity to controller as it's an http-level semantic.
    ResponseEntity<DeliveryOrder> createWebUserDeliveryOrder(String customerId, DeliveryOrderJson deliveryOrderJson);

    // TODO: Move ResponseEntity to controller as it's an http-level semantic.
    ResponseEntity<Map<String, Object>> createApiUserDeliveryOrder(String customerId, DeliveryOrderJson deliveryOrderJson);

    // TODO: Move ResponseEntity to controller as it's an http-level semantic.
    ResponseEntity<Map<String, Object>> createDeliveryRequest(String customerId, DeliveryRequestJson deliveryRequestJson);

    // TODO: Return void here
    ResponseEntity<Map<String, Object>> createLinkOpeningActions(String deliveryOrderId);

    void placeDeliveryOrder(String deliveryOrderId, String authHeader);

    // TODO: Return void here
    ResponseEntity<Map<String, Object>> prePlaceDeliveryOrder(String deliveryOrderId);

    // TODO: Move ResponseEntity to controller as it's an http-level semantic.
    ResponseEntity<DeliveryOrder> duplicateDeliveryOrder(String deliveryOrderId, OrderDuplicationJson orderDuplicationJson);

    // TODO: Can we move this to DeliveryOrderFilter class? Same pattern we use to "get" packages.
    List<DeliveryOrder> getUnpaidDeliveryOrders(String customerId, String startAt, String endAt);
}
