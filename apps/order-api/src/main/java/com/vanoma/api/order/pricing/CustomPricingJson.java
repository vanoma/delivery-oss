package com.vanoma.api.order.pricing;

import com.vanoma.api.utils.exceptions.InvalidParameterException;
import com.vanoma.api.utils.input.TimeUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class CustomPricingJson implements Serializable {

    private String customPricingId;
    private String customerId;
    private Double price;
    private String customerName;
    private String expireAt;

    public String getCustomPricingId() {
        return customPricingId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getPrice() {
        if (price == null) return null;
        return new BigDecimal(price);
    }

    public String getCustomerName() {
        return customerName;
    }

    public OffsetDateTime getExpireAt() {
        return TimeUtils.parseISOString(expireAt);
    }

    public void validate() {
        if (this.customerName == null) {
            throw new InvalidParameterException("crud.customPricing.customerName.required");
        }
        if (this.expireAt == null) {
            throw new InvalidParameterException("crud.customPricing.expireAt.required");
        }
        if (this.price == null) {
            throw new InvalidParameterException("crud.customPricing.price.required");
        }
    }
}
