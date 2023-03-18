package com.vanoma.api.order.payment;

import java.math.BigDecimal;

public class TransactionBreakdown {

    private BigDecimal totalAmount;
    private BigDecimal transactionAmount;
    private BigDecimal transactionFee;

    public TransactionBreakdown() {
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public TransactionBreakdown setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
        return this;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public TransactionBreakdown setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
        return this;
    }

    public BigDecimal getTransactionFee() {
        return transactionFee;
    }

    public TransactionBreakdown setTransactionFee(BigDecimal transactionFee) {
        this.transactionFee = transactionFee;
        return this;
    }
}
