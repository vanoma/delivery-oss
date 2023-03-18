package com.vanoma.api.order.packages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vanoma.api.order.charges.Charge;
import com.vanoma.api.order.charges.ChargeStatus;
import com.vanoma.api.order.charges.ChargeUtils;
import com.vanoma.api.order.contacts.Address;
import com.vanoma.api.order.contacts.Contact;
import com.vanoma.api.order.events.EventName;
import com.vanoma.api.order.events.PackageEvent;
import com.vanoma.api.order.orders.DeliveryOrder;
import com.vanoma.api.order.payment.PaymentStatus;
import com.vanoma.api.utils.exceptions.ExpectedServerError;
import com.vanoma.api.utils.exceptions.InvalidParameterException;
import com.vanoma.api.utils.input.NumberUtils;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.vanoma.api.order.packages.Constants.MINUTES_TO_PICK_UP_EARLY;
import static com.vanoma.api.order.packages.Constants.MINUTES_TO_PICK_UP_LATE;

@Entity
@Table(name = "package",
        indexes = {
                @Index(name = "package_delivery_order_id_idx", columnList = "delivery_order_id", unique = false),
                @Index(name = "package_tracking_number_idx", columnList = "tracking_number", unique = true),
                @Index(name = "package_status_idx", columnList = "status", unique = false),
        })
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class Package {

    @Id
    @Column(name = "package_id", nullable = false)
    private String packageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_order_id", referencedColumnName = "delivery_order_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private DeliveryOrder deliveryOrder;

    // TODO: migrating routing-api data to create assignments for old packages in delivery-api.
    //  Once created, populate the "assignmentId" field then nuke this field as it's no longer
    //  required. Need to update all apps as well to get driver info from delivery-api using
    //  assignmentId.
    @Column(name = "driver_id", nullable = true)
    private String driverId;

    @Column(name = "assignment_id", nullable = true)
    private String assignmentId;

    @Column(name = "priority", nullable = false)
    @Enumerated(EnumType.STRING)
    private PackagePriority priority;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PackageStatus status = PackageStatus.STARTED;

    @Column(name = "tracking_number", nullable = false)
    private String trackingNumber;

    @Column(name = "size")
    @Enumerated(EnumType.STRING)
    private PackageSize size;

    @ManyToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "from_contact", referencedColumnName = "contact_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Contact fromContact;

    @ManyToOne(optional = true, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "to_contact", referencedColumnName = "contact_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Contact toContact;

    @ManyToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "from_address", referencedColumnName = "address_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Address fromAddress;

    @ManyToOne(optional = true, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "to_address", referencedColumnName = "address_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Address toAddress;

    @Column(name = "from_note", length = 500, nullable = true)
    private String fromNote;

    @Column(name = "to_note", length = 500, nullable = true)
    private String toNote;

    @Column(name = "pick_up_start", nullable = true)
    private OffsetDateTime pickUpStart;

    @Column(name = "fragile_content", nullable = true)
    private String fragileContent;

    @Column(name = "event_callback", nullable = true)
    private String eventCallback;

    @Column(name = "staff_note", nullable = true)
    private String staffNote;

    @Column(name = "is_assignable", nullable = false)
    private Boolean isAssignable;

    @Column(name = "pick_up_change_note", nullable = true)
    private String pickUpChangeNote;

    @Column(name = "cancellation_note", nullable = true)
    private String cancellationNote;

    @Column(name = "enable_notifications", nullable = false)
    private Boolean enableNotifications;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "package_id", referencedColumnName = "package_id")
    private Set<Charge> charges;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "package_id", referencedColumnName = "package_id")
    private Set<PackageEvent> events;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    void preInsert() {
        if (this.priority == null) this.priority = PackagePriority.NORMAL;
        if (this.status == null) this.status = PackageStatus.STARTED;
    }

    public Package() {
    }

    public Package(DeliveryOrder deliveryOrder) {
        this.packageId = UUID.randomUUID().toString();
        this.deliveryOrder = deliveryOrder;
        this.trackingNumber = String.valueOf(NumberUtils.getRandomLongInRange((long) 1e12, (long) 1e13));
        this.isAssignable = true;
        this.enableNotifications = true;
    }

    public String getPackageId() {
        return packageId;
    }

    public Package setDriverId(String driverId) {
        this.driverId = driverId;
        return this;
    }

    public String getDriverId() {
        return driverId;
    }

    public Package setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
        return this;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public PackageSize getSize() {
        return size;
    }

    public Package setSize(PackageSize size) {
        this.size = size;
        return this;
    }

    public PackagePriority getPriority() {
        return priority;
    }

    public Package setPriority(PackagePriority priority) {
        this.priority = priority;
        return this;
    }

    public PackageStatus getStatus() {
        return status;
    }

    public Package setStatus(PackageStatus status) {
        this.status = status;
        return this;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public Package setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
        return this;
    }

    public String getFragileContent() {
        return fragileContent;
    }

    public Package setFragileContent(String fragileContent) {
        this.fragileContent = fragileContent;
        return this;
    }

    public String getEventCallback() {
        return eventCallback;
    }

    public Package setEventCallback(String eventCallback) {
        this.eventCallback = eventCallback;
        return this;
    }

    public String getStaffNote() {
        return staffNote;
    }

    public Package setStaffNote(String staffNote) {
        this.staffNote = staffNote;
        return this;
    }

    public Boolean getIsAssignable() {
        return this.isAssignable;
    }

    public Package setIsAssignable(Boolean isAssignable) {
        this.isAssignable = isAssignable;
        return this;
    }

    public Address getFromAddress() {
        return fromAddress;
    }

    public Package setFromAddress(Address fromAddress) {
        this.fromAddress = fromAddress;
        return this;
    }

    public Address getToAddress() {
        return toAddress;
    }

    public Package setToAddress(Address toAddress) {
        this.toAddress = toAddress;
        return this;
    }

    public Contact getToContact() {
        return toContact;
    }

    public Package setToContact(Contact toContact) {
        this.toContact = toContact;
        return this;
    }

    public Contact getFromContact() {
        return fromContact;
    }

    public Package setFromContact(Contact fromContact) {
        this.fromContact = fromContact;
        return this;
    }

    public String getFromNote() {
        return fromNote;
    }

    public Package setFromNote(String fromNote) {
        this.fromNote = fromNote;
        return this;
    }

    public String getToNote() {
        return toNote;
    }

    public Package setToNote(String toNote) {
        this.toNote = toNote;
        return this;
    }

    public OffsetDateTime getPickUpStart() {
        return pickUpStart;
    }

    public Package setPickUpStart(OffsetDateTime pickUpStart) {
        this.pickUpStart = pickUpStart;
        return this;
    }

    public String getPickUpChangeNote() {
        return pickUpChangeNote;
    }

    public Package setPickUpChangeNote(String pickUpChangeNote) {
        this.pickUpChangeNote = pickUpChangeNote;
        return this;
    }

    public String getCancellationNote() {
        return cancellationNote;
    }

    public Package setCancellationNote(String cancellationNote) {
        this.cancellationNote = cancellationNote;
        return this;
    }

    public Boolean getEnableNotifications() {
        return enableNotifications;
    }

    public Package setEnableNotifications(Boolean enableNotifications) {
        this.enableNotifications = enableNotifications;
        return this;
    }

    public OffsetDateTime getPickUpEnd() {
        if (pickUpStart == null) {
            return null;
        }

        return Package.getPickUpEnd(pickUpStart);
    }

    public Boolean getIsExpress() {
        return getSize() == PackageSize.LARGE;
    }

    // TODO: Remove this method; charges here are read-only for api response purpose.
    //  Internally we should save charges through its repository.
    public Package setCharges(Set<Charge> charges) {
        this.charges = charges;
        return this;
    }

    @JsonIgnore
    public Set<Charge> getCharges() {
        return charges;
    }

    public Set<PackageEvent> getEvents() {
        if (events == null) {
            return Set.of();
        }

        // We can have multiple events for different, especially if this package has one or multiple
        // cancelled assignments. We need to return only events applicable to the current assignment
        // in addition to the ones which don't require assignment (ORDER_PLACED and PACKAGE_CANCELLED).
        return events.stream()
                .filter(e -> e.getEventName() == EventName.ORDER_PLACED
                        || e.getEventName() == EventName.PACKAGE_CANCELLED
                        || Objects.equals(e.getAssignmentId(), getAssignmentId()))
                .collect(Collectors.toSet());
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getTrackingLink() {
        String webAppUrl = System.getenv("VANOMA_WEB_APP_URL");
        // The trailing slash is required since we host web app on S3 to avoid redirects
        return String.format("%s/tracking/?tn=%s", webAppUrl, getTrackingNumber());
    }

    public DeliveryOrder getDeliveryOrder() {
        return deliveryOrder;
    }

    public PaymentStatus getPaymentStatus() {
        if (charges == null || charges.size() == 0) return PaymentStatus.NO_CHARGE;
        Set<Charge> paidCharges = getChargesByStatus(ChargeStatus.PAID);
        if (paidCharges.size() == 0) return PaymentStatus.UNPAID;
        if (paidCharges.size() == charges.size()) return PaymentStatus.PAID;
        return PaymentStatus.PARTIAL;
    }

    public BigDecimal getTransactionAmount() {
        if (charges == null || charges.size() == 0) {
            return null;
        }

        return charges
                .stream()
                .map(Charge::getTransactionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTransactionFee() {
        if (charges == null || charges.size() == 0) {
            return null;
        }

        return ChargeUtils.computeTransactionFeeGivenTransactionAmount(getTransactionAmount());
    }

    public BigDecimal getTotalAmount() {
        if (charges == null || charges.size() == 0) {
            return null;
        }

        BigDecimal transactionAmount = getTransactionAmount();
        BigDecimal transactionFee = ChargeUtils.computeTransactionFeeGivenTransactionAmount(transactionAmount);
        return transactionAmount.add(transactionFee);
    }

    @JsonIgnore
    public Set<Charge> getUnpaidCharges() {
        return getChargesByStatus(ChargeStatus.UNPAID);
    }

    public static OffsetDateTime getPickUpEnd(OffsetDateTime pickUpStart) {
        return pickUpStart.plusMinutes(MINUTES_TO_PICK_UP_LATE - MINUTES_TO_PICK_UP_EARLY);
    }

    private Set<Charge> getChargesByStatus(ChargeStatus status) {
        return charges
                .stream()
                .filter(c -> c.getStatus().equals(status))
                .collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        String packageStr = "PACKAGE\n(" +
                "\tpackageId = " + this.packageId + "\n";
        if (size != null) packageStr += "\tsize = " + size.name() + "\n";
        if (pickUpStart != null) packageStr += "\tpickUpStart = " + pickUpStart + "\n";
        if (fromNote != null) packageStr += "\tfromNote = " + fromNote + "\n";
        if (fragileContent != null) packageStr += "\tfragileContent = " + fragileContent + "\n";
        if (toNote != null) packageStr += "\ttoNote = " + toNote + "\n";
        if (fromContact != null) packageStr += "\tfromContact = " + fromContact + "\n";
        if (fromAddress != null) packageStr += "\tfromAddress = " + fromAddress + "\n";
        if (toContact != null) packageStr += "\ttoContact = " + toContact + "\n";
        if (toAddress != null) packageStr += "\ttoAddress = " + toAddress + "\n";
        return packageStr + ")";
    }
}
