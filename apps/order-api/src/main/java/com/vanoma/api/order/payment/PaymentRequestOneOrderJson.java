package com.vanoma.api.order.payment;

import com.vanoma.api.utils.exceptions.InvalidParameterException;

import java.io.Serializable;

public class PaymentRequestOneOrderJson implements Serializable {

    private String paymentMethodId;

    public PaymentRequestOneOrderJson() {
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    // For testing
    public PaymentRequestOneOrderJson setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
        return this;
    }

    public void validate() {
        if (paymentMethodId == null || paymentMethodId.trim().isEmpty()) {
            throw new InvalidParameterException("crud.paymentAttempt.paymentMethodId.required");
        }
    }
}
