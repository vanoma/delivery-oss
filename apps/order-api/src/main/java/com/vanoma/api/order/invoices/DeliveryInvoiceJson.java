package com.vanoma.api.order.invoices;

import java.io.Serializable;

public class DeliveryInvoiceJson implements Serializable {

    private String startAt;
    private String endAt;

    public DeliveryInvoiceJson() {
    }

    public String getStartAt() {
        return startAt;
    }

    public String getEndAt() {
        return endAt;
    }

    // Setters mostly for testing
    public DeliveryInvoiceJson setStartAt(String startAt) {
        this.startAt = startAt;
        return this;
    }

    public DeliveryInvoiceJson setEndAt(String endAt) {
        this.endAt = endAt;
        return this;
    }
}
