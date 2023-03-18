package com.vanoma.api.order.pricing;

import com.vanoma.api.order.utils.annotations.RequestMappingJson;
import com.vanoma.api.order.utils.annotations.PostMappingJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMappingJson
public class PricingController {
    @Autowired
    private CustomPricingRepository customPricingRepository;
    @Autowired
    private IPricingService pricingService;

    @PostMappingJson(value = "/delivery-pricing")
    public ResponseEntity<Map<String, Object>> getDeliveryPricing(@RequestBody PricingJson pricingJson) {
        return ResponseEntity.ok(this.pricingService.getDeliveryPricing(pricingJson));
    }

    @PostMapping(value = "/delivery-orders/{deliveryOrderId}/pricing")
    public ResponseEntity<Map<String, Object>> getPricingForOrder(@PathVariable String deliveryOrderId) {
        return ResponseEntity.ok(this.pricingService.createDeliveryFees(deliveryOrderId));
    }
}
