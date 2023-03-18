package com.vanoma.api.order.payment;

import com.vanoma.api.utils.exceptions.InvalidParameterException;

import java.math.BigDecimal;

public class PaymentRequestManyOrdersJson extends PaymentRequestOneOrderJson {

    private String endAt;
    private String branchId;
    private BigDecimal totalAmount;

    public PaymentRequestManyOrdersJson() {
        super();
    }

    public String getEndAt() {
        return endAt;
    }

    public String getBranchId() {
        return branchId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    // Setters mostly for testing
    public PaymentRequestManyOrdersJson setEndAt(String endAt) {
        this.endAt = endAt;
        return this;
    }

    public PaymentRequestManyOrdersJson setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
        return this;
    }

    public void validate() {
        super.validate();
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            throw new InvalidParameterException("crud.paymentAttempt.totalAmount.required");
        }
    }
}
