package com.vanoma.api.order.invoices;

import com.vanoma.api.order.charges.Charge;
import com.vanoma.api.order.charges.ChargeService;
import com.vanoma.api.order.charges.ChargeUtils;
import com.vanoma.api.order.payment.PaymentStatus;
import com.vanoma.api.utils.exceptions.ExpectedServerError;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "delivery_invoice",
        indexes = {
                @Index(name = "delivery_invoice_customer_id_idx", columnList = "customer_id", unique = false),
        })
public class DeliveryInvoice {

    @Id
    @Column(name = "delivery_invoice_id", nullable = false)
    private String deliveryInvoiceId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "due_by")
    private OffsetDateTime dueBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_invoice_id", referencedColumnName = "delivery_invoice_id")
    private Set<DeliveryInvoiceCharge> deliveryInvoiceCharges;

    public DeliveryInvoice() {
    }

    public DeliveryInvoice(String customerId) {
        this.deliveryInvoiceId = UUID.randomUUID().toString();
        this.customerId = customerId;
    }

    public String getDeliveryInvoiceId() {
        return deliveryInvoiceId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public Set<DeliveryInvoiceCharge> getInvoiceCharges() {
        return deliveryInvoiceCharges;
    }

    public DeliveryInvoice setInvoiceCharges(Set<DeliveryInvoiceCharge> deliveryInvoiceCharges) {
        this.deliveryInvoiceCharges = deliveryInvoiceCharges;
        return this;
    }

    public PaymentStatus getStatus() {
        if (deliveryInvoiceCharges == null || deliveryInvoiceCharges.size() == 0) {
            throw new ExpectedServerError("crud.deliveryInvoice.chargesNotFound");
        }
        Set<Charge> charges = deliveryInvoiceCharges
                .stream()
                .map(DeliveryInvoiceCharge::getCharge)
                .collect(Collectors.toSet());
        return ChargeUtils.getPaymentStatus(charges);
    }
}
