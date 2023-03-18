package com.vanoma.api.order.payment;

import com.vanoma.api.utils.exceptions.InvalidParameterException;
import com.vanoma.api.utils.input.TimeUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class OfflinePaymentOneOrderJson implements Serializable {

    private String paymentMethodId;
    private String operatorTransactionId;
    private BigDecimal totalAmount;
    private String paymentTime;
    private String description;

    public OfflinePaymentOneOrderJson() {
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public String getOperatorTransactionId() {
        return operatorTransactionId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OffsetDateTime getPaymentTime() {
        return TimeUtils.parseISOString(paymentTime);
    }

    // Setters mostly for testing
    public OfflinePaymentOneOrderJson setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
        return this;
    }

    public OfflinePaymentOneOrderJson setOperatorTransactionId(String operatorTransactionId) {
        this.operatorTransactionId = operatorTransactionId;
        return this;
    }

    public OfflinePaymentOneOrderJson setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
        return this;
    }

    public OfflinePaymentOneOrderJson setPaymentTime(String paymentTime) {
        this.paymentTime = paymentTime;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public OfflinePaymentOneOrderJson setDescription(String description) {
        this.description = description;
        return this;
    }

    public void validate() {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            throw new InvalidParameterException("crud.paymentAttempt.totalAmount.required");
        }
        if (paymentMethodId == null || paymentMethodId.trim().isEmpty()) {
            throw new InvalidParameterException("crud.paymentAttempt.paymentMethodId.required");
        }
        if (paymentTime == null || paymentTime.trim().isEmpty()) {
            throw new InvalidParameterException("crud.paymentAttempt.paymentTime.required");
        }
        if (operatorTransactionId == null || operatorTransactionId.trim().isEmpty()) {
            throw new InvalidParameterException("crud.paymentAttempt.operatorTransactionId.required");
        }
    }
}
