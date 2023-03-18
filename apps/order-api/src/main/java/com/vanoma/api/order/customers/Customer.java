package com.vanoma.api.order.customers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vanoma.api.utils.input.TimeUtils;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

// TODO: make business name unique after migrating data and making sure that existing business names are indeed unique
@Entity
@Table(name = "customer",
        indexes = {
                @Index(name = "customer_business_name_idx", columnList = "business_name", unique = false),
                @Index(name = "customer_phone_number_idx", columnList = "phone_number", unique = true),
        })
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class Customer {
        @Id
        @Column(name = "customer_id", nullable = false)
        private String customerId;

        @Column(name = "business_name", nullable = false)
        private String businessName;

        @Column(name = "phone_number", nullable = false)
        private String phoneNumber;

        @Column(name = "weighting_factor", precision = 3, scale = 2, nullable = false)
        private BigDecimal weightingFactor;

        // TODO: Deleted this field. It will be computed automatically based on postpaidExpiry
        @Column(name = "is_prepaid", nullable = false)
        private Boolean isPrepaid;

        @Column(name = "billing_grace_period", nullable = false)
        private Integer billingGracePeriod;

        @Column(name = "billing_interval", nullable = false)
        private Integer billingInterval;

        @Column(name = "postpaid_expiry", nullable = true)
        private OffsetDateTime postpaidExpiry;

        @Column(name = "fixed_price_amount", precision = 10, scale = 2, nullable = true)
        private BigDecimal fixedPriceAmount;

        @Column(name = "fixed_price_expiry", nullable = true)
        private OffsetDateTime fixedPriceExpiry;

        @CreationTimestamp
        @Column(name = "created_at", updatable = false)
        private OffsetDateTime createdAt;

        @UpdateTimestamp
        @Column(name = "updated_at")
        private OffsetDateTime updatedAt;

        public Customer() {
                this.customerId = UUID.randomUUID().toString();
                this.weightingFactor = new BigDecimal("1.00");
                this.isPrepaid = true;
                this.billingInterval = 7;
                this.billingGracePeriod = 3;
        }

        public String getCustomerId() {
                return customerId;
        }

        public Customer setBusinessName(String businessName) {
                this.businessName = businessName;
                return this;
        }

        public String getBusinessName() {
                return businessName;
        }

        public Customer setPhoneNumber(String phoneNumber) {
                this.phoneNumber = phoneNumber;
                return this;
        }

        public String getPhoneNumber() {
                return phoneNumber;
        }

        public BigDecimal getWeightingFactor() {
                return weightingFactor;
        }

        public Customer setWeightingFactor(BigDecimal weightingFactor) {
                assert weightingFactor != null && weightingFactor.doubleValue() > 0;
                this.weightingFactor = weightingFactor;
                return this;
        }

        public Integer getBillingGracePeriod() {
                return billingGracePeriod;
        }

        public Customer setBillingGracePeriod(Integer billingGracePeriod) {
                this.billingGracePeriod = billingGracePeriod;
                return this;
        }

        public Integer getBillingInterval() {
                return billingInterval;
        }

        public Customer setBillingInterval(Integer billingInterval) {
                this.billingInterval = billingInterval;
                return this;
        }

        public OffsetDateTime getPostpaidExpiry() {
                return postpaidExpiry;
        }

        public Customer setPostpaidExpiry(OffsetDateTime postpaidExpiry) {
                this.postpaidExpiry = postpaidExpiry;
                return this;
        }

        public BigDecimal getFixedPriceAmount() {
                return fixedPriceAmount;
        }

        public Customer setFixedPriceAmount(BigDecimal fixedPriceAmount) {
                this.fixedPriceAmount = fixedPriceAmount;
                return this;
        }

        public OffsetDateTime getFixedPriceExpiry() {
                return fixedPriceExpiry;
        }

        public Customer setFixedPriceExpiry(OffsetDateTime fixedPriceExpiry) {
                this.fixedPriceExpiry = fixedPriceExpiry;
                return this;
        }

        public Boolean getIsPrepaid() {
                if (Objects.isNull(this.postpaidExpiry)) {
                        return true;
                }

                return this.postpaidExpiry.isBefore(TimeUtils.getUtcNow());
        }

        public Boolean getHasFixedPrice() {
                if (Objects.isNull(this.fixedPriceAmount) || Objects.isNull(this.fixedPriceExpiry)) {
                        return false;
                }

                return this.fixedPriceExpiry.isAfter(TimeUtils.getUtcNow());
        }

        public OffsetDateTime getUpdatedAt() {
                return updatedAt;
        }

        public OffsetDateTime getCreatedAt() {
                return createdAt;
        }

        @Override
        public boolean equals(Object other) {
                return other instanceof Customer &&
                        this.customerId.equals(((Customer) other).getCustomerId());
        }

        @Override
        public String toString() {
                String customerStr = "CUSTOMER\n(" +
                        "\tcustomerId = " + this.customerId + "\n";
                if (businessName != null) customerStr += "\tbusinessName = " + businessName + "\n";
                if (phoneNumber != null) customerStr += "\tphoneNumber = " + phoneNumber + "\n";
                return customerStr + ")";
        }
}
