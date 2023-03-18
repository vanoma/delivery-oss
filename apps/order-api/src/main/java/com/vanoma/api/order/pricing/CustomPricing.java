package com.vanoma.api.order.pricing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vanoma.api.order.utils.BigDecimalSerializer;
import com.vanoma.api.utils.input.TimeUtils;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

// TODO: We can actually delete this model completely. We'd just have to move price and expireAt
//  fields to customer model. We can rename them to fixedPrice and fixedPriceExpireAt. Moving
//  both fields would simplify UI as staff-app already has a list of customers; we'd just have to
//  edit the customer. This would also makes custom pricing management easier as we wouldn't have
//  to create new CustomPricing entity for each customer; editing the customer in staff-app would
//  suffice.

@Entity
@Table(name = "custom_pricing",
        indexes = {
                @Index(name = "custom_pricing_customer_id", columnList = "customer_id", unique = false)
        })
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class CustomPricing {
    @Id
    @Column(name = "custom_pricing_id", nullable = false)
    private String customPricingId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "price",
            precision = 10, scale = 2, nullable = true)
    private BigDecimal price;

    // TODO: Remove this field to avoid data duplication.
    // NOTE: Needed until Staff app includes actions on CustomPricing.
    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "expire_at", nullable = false)
    private OffsetDateTime expireAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;


    public CustomPricing() {
    }

    public CustomPricing(String customerId) {
        this.customPricingId = UUID.randomUUID().toString();
        this.customerId = customerId;
    }

    public String getCustomPricingId() {
        return customPricingId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public CustomPricing setCustomerId(String customerId) {
        this.customerId = customerId;
        return this;
    }

    @JsonSerialize(using = BigDecimalSerializer.class)
    public BigDecimal getPrice() {
        return price;
    }

    public CustomPricing setPrice(BigDecimal price) {
        this.price = price;
        return this;
    }

    public String getCustomerName() {
        return customerName;
    }

    public CustomPricing setCustomerName(String customerName) {
        this.customerName = customerName;
        return this;
    }

    public OffsetDateTime getExpireAt() {
        return expireAt;
    }

    public CustomPricing setExpireAt(OffsetDateTime expireAt) {
        this.expireAt = expireAt;
        return this;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public CustomPricing setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public boolean isActive() {
        return expireAt != null && expireAt.isAfter(TimeUtils.getUtcNow());
    }
}
