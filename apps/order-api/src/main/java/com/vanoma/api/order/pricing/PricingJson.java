package com.vanoma.api.order.pricing;

import java.io.Serializable;
import java.util.List;

public class PricingJson implements Serializable {
    private String deliveryOrderId;
    private List<PricingItemJson> packages;

    public String getDeliveryOrderId() {
        return deliveryOrderId;
    }

    public List<PricingItemJson> getPackages() {
        return packages;
    }

    // Setters for testing
    public PricingJson setDeliveryOrderId(String deliveryOrderId) {
        this.deliveryOrderId = deliveryOrderId;
        return this;
    }

    public PricingJson setPackages(List<PricingItemJson> packages) {
        this.packages = packages;
        return this;
    }
}
