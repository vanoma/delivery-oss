package com.vanoma.api.order.orders;

import com.vanoma.api.order.utils.annotations.PostMappingJson;
import com.vanoma.api.order.utils.annotations.RequestMappingJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMappingJson
public class OrderController {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private IDeliveryOrderService orderService;

    @PostMappingJson(value = "/customers/{customerId}/delivery-orders")
    public ResponseEntity<?> createDeliveryOrder(@PathVariable String customerId,
                                                 @RequestBody(required = false) DeliveryOrderJson deliveryOrderJson,
                                                 @RequestHeader(value = "X-Access-Key", required = false) String apiAccessKey) {
        if (apiAccessKey != null && apiAccessKey.length() >= 64) {
            // TODO Check if access is valid
            return this.orderService.createApiUserDeliveryOrder(customerId, deliveryOrderJson);
        } else {
            return this.orderService.createWebUserDeliveryOrder(customerId, deliveryOrderJson);
        }
    }

    @GetMapping(value = "/delivery-orders/{deliveryOrderId}")
    public ResponseEntity<DeliveryOrder> getDeliveryOrder(@PathVariable String deliveryOrderId) {
        return ResponseEntity.ok(this.orderRepository.getById(deliveryOrderId));
    }

    @PostMapping(value = "/delivery-orders/{deliveryOrderId}/link-opening")
    public ResponseEntity<Map<String, Object>> createLinkOpeningActions(@PathVariable String deliveryOrderId) {
        return this.orderService.createLinkOpeningActions(deliveryOrderId);
    }

    @PostMapping(value = "/delivery-orders/{deliveryOrderId}/pre-placement")
    public ResponseEntity<Map<String, Object>> prePlaceDeliveryOrder(@PathVariable String deliveryOrderId) {
        return this.orderService.prePlaceDeliveryOrder(deliveryOrderId);
    }

    @PostMapping(value = "/delivery-orders/{deliveryOrderId}/placement")
    public ResponseEntity<Void> placeDeliveryOrder(@PathVariable String deliveryOrderId,
                                                   @RequestHeader(value = "Authorization", required = false) String authHeader) {
        this.orderService.placeDeliveryOrder(deliveryOrderId, authHeader);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/delivery-orders/{deliveryOrderId}/duplication")
    public ResponseEntity<DeliveryOrder> duplicateDeliveryOrder(@PathVariable String deliveryOrderId,
                                                                @RequestBody OrderDuplicationJson orderDuplicationJson) {
        return this.orderService.duplicateDeliveryOrder(deliveryOrderId, orderDuplicationJson);
    }

    @PostMappingJson(value = "/customers/{customerId}/delivery-requests")
    public ResponseEntity<Map<String, Object>> createDeliveryRequest(@PathVariable String customerId,
                                                                     @RequestBody DeliveryRequestJson deliveryRequestJson) {
        return this.orderService.createDeliveryRequest(customerId, deliveryRequestJson);
    }
}
