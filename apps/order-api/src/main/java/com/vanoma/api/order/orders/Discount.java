package com.vanoma.api.order.orders;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "discount",
        uniqueConstraints = @UniqueConstraint(columnNames = {"delivery_order_id", "type"}),
        indexes = {
                @Index(name = "discount_delivery_order_id_idx", columnList = "delivery_order_id", unique = false),
                @Index(name = "discount_type_idx", columnList = "type", unique = false)
        })
public class Discount {
    @Id
    @Column(name = "discount_id", nullable = false)
    private String discountId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_order_id", referencedColumnName = "delivery_order_id")
    private DeliveryOrder deliveryOrder;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DiscountType type;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private DiscountStatus status;

    @Column(name = "amount", nullable = false,
            precision = 10, scale = 2)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public Discount() { }

    public Discount(DeliveryOrder deliveryOrder) {
        this.discountId = UUID.randomUUID().toString();
        this.deliveryOrder = deliveryOrder;
        this.status = DiscountStatus.PENDING;
    }

    public String getDiscountId() {
        return discountId;
    }

    @JsonIgnore
    public DeliveryOrder getDeliveryOrder() {
        return deliveryOrder;
    }

    public Discount setType(DiscountType type) {
        this.type = type;
        return this;
    }

    public DiscountType getType() {
        return type;
    }

    public Discount setStatus(DiscountStatus status) {
        this.status = status;
        return this;
    }

    public DiscountStatus getStatus() {
        return status;
    }

    public Discount setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Discount &&
                this.discountId.equals(((Discount) other).getDiscountId());
    }

    @Override
    public String toString() {
        String discountStr = "DISCOUNT\n(" +
                "\tdiscountId = " + this.discountId + "\n";
        if (deliveryOrder != null) discountStr += "\tdeliveryOrderId = " + deliveryOrder.getDeliveryOrderId() + "\n";
        if (amount != null) discountStr += "\tamount = " + amount + "\n";
        if (type != null) discountStr += "\ttype = " + type.name() + "\n";
        if (status != null) discountStr += "\tstatus = " + status.name() + "\n";
        return discountStr + ")";
    }
}
