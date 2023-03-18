package com.vanoma.api.order.charges;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vanoma.api.order.orders.DeliveryOrder;
import com.vanoma.api.order.packages.Package;
import com.vanoma.api.order.utils.BigDecimalSerializer;
import com.vanoma.api.utils.exceptions.InvalidParameterException;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "charge",
        indexes = {
                @Index(name = "charge_delivery_order_id_idx", columnList = "delivery_order_id", unique = false),
                @Index(name = "charge_package_id_idx", columnList = "package_id", unique = false),
                @Index(name = "charge_type_idx", columnList = "type", unique = false),
        })
public class Charge {

    @Id
    @Column(name = "charge_id", nullable = false)
    private String chargeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_order_id", referencedColumnName = "delivery_order_id")
    private DeliveryOrder deliveryOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", referencedColumnName = "package_id")
    private Package pkg;

    @Column(name = "type", nullable = true)
    @Enumerated(EnumType.STRING)
    private ChargeType type;

    @Column(name = "status", nullable = true)
    @Enumerated(EnumType.STRING)
    private ChargeStatus status = ChargeStatus.UNPAID;

    @Column(name = "transaction_amount", nullable = false,
            precision = 10, scale = 2)
    private BigDecimal transactionAmount;

    @Column(name = "actual_transaction_amount", nullable = true,
            precision = 10, scale = 2)
    private BigDecimal actualTransactionAmount;

    @Column(name = "description", nullable = true)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    void preInsert() {
        if (this.status == null) this.status = ChargeStatus.UNPAID;
    }

    public Charge() {
    }

    public Charge(Package pkg) {
        this.chargeId = UUID.randomUUID().toString();
        this.pkg = pkg;
        this.deliveryOrder = pkg.getDeliveryOrder();
    }

    public String getChargeId() {
        return chargeId;
    }

    public String getDeliveryOrderId() {
        return deliveryOrder.getDeliveryOrderId();
    }

    @JsonIgnore
    public DeliveryOrder getDeliveryOrder() {
        return deliveryOrder;
    }

    public String getPackageId() {
        return pkg.getPackageId();
    }

    @JsonIgnore
    public Package getPackage() {
        return pkg;
    }

    public Charge setPackage(Package pkg) {
        this.pkg = pkg;
        return this;
    }

    public ChargeType getType() {
        return type;
    }

    public Charge setType(ChargeType type) {
        this.type = type;
        return this;
    }

    @JsonIgnore
    public boolean isDeliveryFee() {
        return ChargeType.DELIVERY_FEE.equals(this.type);
    }

    @JsonSerialize(using = BigDecimalSerializer.class)
    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public Charge setTransactionAmount(BigDecimal transactionAmount) {
        if (this.type == null) throw new InvalidParameterException("crud.charge.type.required");
        this.transactionAmount = transactionAmount;
        return this;
    }

    @JsonSerialize(using = BigDecimalSerializer.class)
    public BigDecimal getActualTransactionAmount() {
        return actualTransactionAmount;
    }

    public Charge setActualTransactionAmount(BigDecimal actualTransactionAmount) {
        if (this.type == null) throw new InvalidParameterException("crud.charge.type.required");
        if (this.transactionAmount == null) throw new InvalidParameterException("crud.charge.transactionAmount.required");
        this.actualTransactionAmount = actualTransactionAmount;
        return this;
    }

    @JsonSerialize(using = BigDecimalSerializer.class)
    public BigDecimal getTransactionFee() {
        return ChargeUtils.computeTransactionFeeGivenTransactionAmount(transactionAmount);
    }

    @JsonSerialize(using = BigDecimalSerializer.class)
    public BigDecimal getTotalAmount() {
        return getTransactionAmount().add(getTransactionFee());
    }

    public ChargeStatus getStatus() {
        return status;
    }

    public Charge setStatus(ChargeStatus status) {
        this.status = status;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Charge setDescription(String description) {
        this.description = description;
        return this;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Charge duplicate(Package pkg) {
        return new Charge(pkg)
                .setType(this.type)
                .setStatus(ChargeStatus.UNPAID)
                .setTransactionAmount(this.transactionAmount)
                .setActualTransactionAmount(this.actualTransactionAmount)
                .setDescription(this.description);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Charge &&
                this.chargeId.equals(((Charge) other).getChargeId());
    }

    @Override
    public String toString() {
        String chargeStr = "CHARGE\n(" +
                "\tchargeId = " + this.chargeId + "\n";
        if (deliveryOrder != null) chargeStr += "\tdeliveryOrderId = " + deliveryOrder.getDeliveryOrderId() + "\n";
        if (pkg != null) chargeStr += "\tpackageId = " + pkg.getPackageId() + "\n";
        if (transactionAmount != null) chargeStr += "\ttransactionAmount = " + transactionAmount + "\n";
        if (actualTransactionAmount != null) chargeStr += "\tactualTransactionAmount = " + actualTransactionAmount + "\n";
        if (type != null) chargeStr += "\ttype = " + type.name() + "\n";
        if (status != null) chargeStr += "\tstatus = " + status.name() + "\n";
        return chargeStr + ")";
    }
}
