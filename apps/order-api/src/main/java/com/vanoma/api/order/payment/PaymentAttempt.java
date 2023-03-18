package com.vanoma.api.order.payment;

import com.vanoma.api.order.charges.Charge;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_attempt",
        indexes = {
                @Index(name = "payment_attempt_request_id_idx", columnList = "request_id", unique = false),
                @Index(name = "payment_attempt_is_success_idx", columnList = "is_success", unique = false)
        })
public class PaymentAttempt {

    @Id
    @Column(name = "payment_attempt_id", nullable = false)
    private String paymentAttemptId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "charge_id", referencedColumnName = "charge_id")
    private Charge charge;

    @Column(name = "request_id", nullable = true)
    private String requestId;

    @Column(name = "is_success", nullable = true)
    private Boolean isSuccess;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public PaymentAttempt() {
    }

    public PaymentAttempt(Charge charge) {
        this.paymentAttemptId = UUID.randomUUID().toString();
        this.charge = charge;
    }


    public String getPaymentAttemptId() {
        return paymentAttemptId;
    }

    public Charge getCharge() {
        return charge;
    }

    public PaymentAttempt setCharge(Charge charge) {
        this.charge = charge;
        return this;
    }

    public String getRequestId() {
        return requestId;
    }

    public PaymentAttempt setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public Boolean getIsSuccess() {
        return isSuccess;
    }

    public PaymentAttempt setIsSuccess(Boolean success) {
        isSuccess = success;
        return this;
    }
}
