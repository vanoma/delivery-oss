package com.vanoma.api.order.payment;

public class OfflinePaymentManyOrdersJson extends OfflinePaymentOneOrderJson {

    private String endAt;

    public String getEndAt() {
        return endAt;
    }

    // Setters mostly for testing
    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }
}
