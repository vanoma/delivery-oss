package com.vanoma.api.order.external;

public class PaymentRecordParams extends PaymentRequestParams {

    private String paymentTime;
    private String operatorTransactionId;

    public PaymentRecordParams() {
        super();
    }

    public String getPaymentTime() {
        return paymentTime;
    }

    public PaymentRecordParams setPaymentTime(String paymentTime) {
        this.paymentTime = paymentTime;
        return this;
    }

    public String getOperatorTransactionId() {
        return operatorTransactionId;
    }

    public PaymentRecordParams setOperatorTransactionId(String operatorTransactionId) {
        this.operatorTransactionId = operatorTransactionId;
        return this;
    }
}
