package com.vanoma.api.order.payment;

import com.vanoma.api.utils.exceptions.InvalidParameterException;

import java.io.Serializable;

public class PaymentCallbackJson implements Serializable {

    private static String SUCCESS = "SUCCESS";

    private String status;
    private String paymentRequestId;
    private String errorCode;
    private String errorMessage;

    public PaymentCallbackJson() {
    }


    public PaymentCallbackStatus getStatus() {
        return PaymentCallbackStatus.create(status);
    }

    public String getPaymentRequestId() {
        return paymentRequestId;
    }


    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    // Setters are mostly for testing
    public PaymentCallbackJson setStatus(PaymentCallbackStatus status) {
        this.status = status.name();
        return this;
    }

    public PaymentCallbackJson setPaymentRequestId(String paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
        return this;
    }

    public PaymentCallbackJson setErrorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public PaymentCallbackJson setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public boolean isSuccess() {
        return SUCCESS.equals(status);
    }

    public void validate() {
        if (status == null) {
            throw new InvalidParameterException("crud.paymentAttempt.callback.status.required");
        }

        if (paymentRequestId == null) {
            throw new InvalidParameterException("crud.paymentAttempt.callback.paymentRequestId.required");
        }
    }
}
