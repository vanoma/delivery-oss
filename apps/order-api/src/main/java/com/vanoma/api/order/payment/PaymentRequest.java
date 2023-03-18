package com.vanoma.api.order.payment;

import com.vanoma.api.order.charges.Charge;
import com.vanoma.api.order.orders.Discount;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "payment_request",
        indexes = {
                @Index(name = "payment_request_is_success_idx", columnList = "is_success", unique = false)
        })
public class PaymentRequest {
    @Id
    @Column(name = "payment_request_id", nullable = false)
    private String paymentRequestId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "charge_payment_request", joinColumns = @JoinColumn(name = "payment_request_id"), inverseJoinColumns = @JoinColumn(name = "charge_id"))
    private Set<Charge> charges;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "discount_payment_request", joinColumns = @JoinColumn(name = "payment_request_id"), inverseJoinColumns = @JoinColumn(name = "discount_id"))
    private Set<Discount> discounts;

    @Column(name = "is_success", nullable = true)
    private Boolean isSuccess;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public PaymentRequest() {
    }

    public PaymentRequest(Set<Charge> charges, Set<Discount> discounts) {
        this.paymentRequestId = UUID.randomUUID().toString();
        this.charges = charges;
        this.discounts = discounts;
    }


    public String getPaymentRequestId() {
        return paymentRequestId;
    }

    public Set<Charge> getCharges() {
        return charges;
    }

    public PaymentRequest setCharge(Set<Charge> charges) {
        this.charges = charges;
        return this;
    }

    public Set<Discount> getDiscounts() {
        return discounts;
    }

    public PaymentRequest setDiscounts(Set<Discount> discounts) {
        this.discounts = discounts;
        return this;
    }

    public Boolean getIsSuccess() {
        return isSuccess;
    }

    public PaymentRequest setIsSuccess(Boolean success) {
        isSuccess = success;
        return this;
    }
}
