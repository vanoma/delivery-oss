package com.vanoma.api.order.pricing;

import com.vanoma.api.order.orders.DeliveryOrder;

import java.util.Map;

public interface IPricingService {

    Map<String, Object> createDeliveryFees(DeliveryOrder order);

    Map<String, Object> createDeliveryFees(String deliveryOrderId);

    Map<String, Object> getDeliveryPricing(PricingJson pricingJson);

    CustomPricing createCustomPricing(String customerId, CustomPricingJson customPricingJson);
}
