package com.vanoma.api.order.external;

import com.vanoma.api.order.payment.TransactionBreakdown;

public class PaymentRequestParams {

    private String paymentMethodId;
    private TransactionBreakdown transactionBreakdown;
    private String paymentRequestId;
    private String description;

    public PaymentRequestParams() {
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public PaymentRequestParams setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
        return this;
    }

    public String getPaymentRequestId() {
        return paymentRequestId;
    }

    public PaymentRequestParams setPaymentRequestId(String paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PaymentRequestParams setDescription(String description) {
        this.description = description;
        return this;
    }

    public TransactionBreakdown getTransactionBreakdown() {
        return transactionBreakdown;
    }

    public PaymentRequestParams setTransactionBreakdown(TransactionBreakdown transactionBreakdown) {
        this.transactionBreakdown = transactionBreakdown;
        return this;
    }
}
