package com.vanoma.api.order.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vanoma.api.order.orders.DeliveryOrder;
import com.vanoma.api.order.packages.Package;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;


@Entity
@Table(name = "package_event",
        uniqueConstraints = @UniqueConstraint(columnNames = {"package_id", "event_name", "assignment_id"}),
        indexes = {
                @Index(name = "package_event_delivery_order_id_idx", columnList = "delivery_order_id", unique = false),
                @Index(name = "package_event_package_id_idx", columnList = "package_id", unique = false)
        })
public class PackageEvent {

    @Id
    @Column(name = "package_event_id", nullable = false)
    private String packageEventId;

    @Column(name = "assignment_id", nullable = true)
    private String assignmentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", referencedColumnName = "package_id")
    @JsonIgnore
    private Package pkg;

    @Column(name = "event_name", nullable = false)
    @Enumerated(EnumType.STRING)
    private EventName eventName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_order_id", referencedColumnName = "delivery_order_id")
    @JsonIgnore
    private DeliveryOrder deliveryOrder;

    @Column(name = "text_en", nullable = false)
    private String textEN;

    @Column(name = "text_fr", nullable = false)
    private String textFR;

    @Column(name = "text_rw", nullable = false)
    private String textRW;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public PackageEvent() {
    }

    public PackageEvent(Package pkg) {
        this.packageEventId = UUID.randomUUID().toString();
        this.pkg = pkg;
        this.deliveryOrder = pkg.getDeliveryOrder();
    }

    public String getPackageEventId() {
        return packageEventId;
    }

    public PackageEvent setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
        return this;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public String getPackageId() {
        return pkg.getPackageId();
    }

    public EventName getEventName() {
        return eventName;
    }

    public PackageEvent setEventName(EventName eventName) {
        this.eventName = eventName;
        return this;
    }

    public String getDeliveryOrderId() {
        return deliveryOrder.getDeliveryOrderId();
    }

    public String getTextEN() {
        return textEN;
    }

    public PackageEvent setTextEN(String textEN) {
        this.textEN = textEN;
        return this;
    }

    public String getTextFR() {
        return textFR;
    }

    public PackageEvent setTextFR(String textFR) {
        this.textFR = textFR;
        return this;
    }

    public String getTextRW() {
        return textRW;
    }

    public PackageEvent setTextRW(String textRW) {
        this.textRW = textRW;
        return this;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
