package com.vanoma.api.order.invoices;

import com.vanoma.api.order.charges.Charge;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_invoice_charge",
        indexes = {
                @Index(name = "delivery_invoice_charge_invoice_id_idx", columnList = "delivery_invoice_id", unique = false),
        })
public class DeliveryInvoiceCharge {

    @Id
    @Column(name = "delivery_invoice_charge_id", nullable = false)
    private String deliveryInvoiceChargeId;

    @Column(name = "delivery_invoice_id", nullable = false)
    private String deliveryInvoiceId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "charge_id", referencedColumnName = "charge_id")
    private Charge charge;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    public DeliveryInvoiceCharge() {
    }

    public DeliveryInvoiceCharge(String invoiceId, Charge charge) {
        this.deliveryInvoiceChargeId = UUID.randomUUID().toString();
        this.deliveryInvoiceId = invoiceId;
        this.charge = charge;
    }

    public String getDeliveryInvoiceChargeId() {
        return deliveryInvoiceChargeId;
    }

    public String getDeliveryInvoiceId() {
        return deliveryInvoiceId;
    }

    public DeliveryInvoiceCharge setDeliveryInvoiceId(String deliveryInvoiceId) {
        this.deliveryInvoiceId = deliveryInvoiceId;
        return this;
    }

    public Charge getCharge() {
        return charge;
    }

    public DeliveryInvoiceCharge setCharge(Charge charge) {
        this.charge = charge;
        return this;
    }
}
