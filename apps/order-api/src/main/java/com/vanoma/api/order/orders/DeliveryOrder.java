package com.vanoma.api.order.orders;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vanoma.api.order.charges.Charge;
import com.vanoma.api.order.customers.Agent;
import com.vanoma.api.order.customers.Branch;
import com.vanoma.api.order.customers.Customer;
import com.vanoma.api.order.packages.Package;
import com.vanoma.api.order.payment.PaymentStatus;
import com.vanoma.api.order.payment.RefundStatus;
import com.vanoma.api.utils.input.TimeUtils;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "delivery_order",
        indexes = {
                @Index(name = "delivery_order_customer_id_idx", columnList = "customer_id", unique = false),
                @Index(name = "delivery_order_status_idx", columnList = "status", unique = false),
                @Index(name = "delivery_order_created_at_idx", columnList = "created_at", unique = false),
                @Index(name = "delivery_order_placed_at_idx", columnList = "placed_at", unique = false),
        })
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class DeliveryOrder {

    @Id
    @Column(name = "delivery_order_id", nullable = false)
    private String deliveryOrderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "branch_id", referencedColumnName = "branch_id")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "agent_id", referencedColumnName = "agent_id")
    private Agent agent;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.STARTED;

    @Column(name = "placed_at")
    private OffsetDateTime placedAt;

    @Column(name = "is_customer_paying",
            columnDefinition = "boolean default true")
    private Boolean isCustomerPaying = true;

    @Column(name = "client_type", nullable = true)
    @Enumerated(EnumType.STRING)
    private ClientType clientType;

    // TODO: Move this field to package
    @Column(name = "refund_status")
    @Enumerated(EnumType.STRING)
    private RefundStatus refundStatus = RefundStatus.NO_REFUND;

    // TODO: Move this field to package
    @Column(name = "refund_amount",
            precision = 10, scale = 2,
            nullable = true)
    private BigDecimal refundAmount;

    @Column(name = "link_opened_at")
    private OffsetDateTime linkOpenedAt;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_order_id", referencedColumnName = "delivery_order_id")
    private Set<Package> packages;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_order_id", referencedColumnName = "delivery_order_id")
    private Set<Discount> discounts;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    void preInsert() {
        if (this.status == null) this.status = OrderStatus.STARTED;
        if (this.refundStatus == null) this.refundStatus = RefundStatus.NO_REFUND;
    }

    public DeliveryOrder() {
    }

    public DeliveryOrder(Customer customer) {
        this.customer = customer;
        this.deliveryOrderId = UUID.randomUUID().toString();
        this.packages = new HashSet<>();
        this.discounts = new HashSet<>();
    }

    public DeliveryOrder(Agent agent) {
        this(agent.getCustomer());
        this.branch = agent.getBranch();
        this.agent = agent;
    }

    public String getDeliveryOrderId() {
        return deliveryOrderId;
    }

    // TODO: Update all references to use getCustomer().getCustomerId() instead
    public String getCustomerId() {
        return customer.getCustomerId();
    }

    public Customer getCustomer() {
        return customer;
    }

    public Branch getBranch() {
        return branch;
    }

    public Agent getAgent() {
        return agent;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public DeliveryOrder setStatus(OrderStatus status) {
        this.status = status;
        return this;
    }

    public DeliveryOrder setClientType(ClientType clientType) {
        this.clientType = clientType;
        return this;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public String getDeliveryLink() {
        String webAppUrl = System.getenv("VANOMA_WEB_APP_URL");
        return webAppUrl + "/delivery-request/?id=" + deliveryOrderId;
    }

    // TODO: Should we delete this read-only property? If we ever want to display orders to users, we might need it.
    @JsonIgnore
    public Set<Package> getPackages() {
        return packages;
    }

    @JsonIgnore
    public Set<Discount> getDiscounts() {
        return discounts;
    }

    public DeliveryOrder setIsCustomerPaying(Boolean isCustomerPaying) {
        this.isCustomerPaying = isCustomerPaying;
        return this;
    }

    public Boolean getIsCustomerPaying() {
        return isCustomerPaying;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getPlacedAt() {
        return placedAt;
    }

    public DeliveryOrder setPlacedAt(OffsetDateTime placedAt) {
        this.placedAt = placedAt;
        return this;
    }

    public OffsetDateTime getLinkOpenedAt() {
        return linkOpenedAt;
    }

    public DeliveryOrder setLinkOpenedAt() {
        this.linkOpenedAt = TimeUtils.getUtcNow();
        return this;
    }

    @JsonIgnore
    public PaymentStatus getPaymentStatus() {
        if (packages == null || packages.size() == 0) {
            return PaymentStatus.NO_CHARGE;
        }

        for (Package pkg : packages) {
            if (pkg.getPaymentStatus() != PaymentStatus.NO_CHARGE && pkg.getPaymentStatus() != PaymentStatus.PAID) {
                return pkg.getPaymentStatus();
            }
        }

        return PaymentStatus.PAID;
    }

    @JsonIgnore
    public Set<Charge> getUnpaidCharges() {
        if (packages == null || packages.size() == 0) {
            return Set.of();
        }

        return packages.stream()
                .map(Package::getUnpaidCharges)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    public Set<Discount> getPendingDiscounts() {
        if (discounts == null || discounts.size() == 0) {
            return Set.of();
        }

        return discounts.stream()
                .filter(d -> d.getStatus() == DiscountStatus.PENDING)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DeliveryOrder &&
                this.deliveryOrderId.equals(((DeliveryOrder) other).getDeliveryOrderId());
    }

    @Override
    public String toString() {
        StringBuilder orderStr = new StringBuilder("ORDER\n(");
        orderStr.append("\tdeliveryOrderId = ").append(this.deliveryOrderId).append("\n");
        orderStr.append("\tcustomerId = ").append(this.customer.getCustomerId()).append("\n");
        orderStr.append("\tbranchId = ").append(this.branch.getBranchId()).append("\n");
        if (packages != null) {
            for (Package pkg : packages) {
                orderStr.append(pkg.toString());
            }
        }
        return orderStr + ")";
    }
}
