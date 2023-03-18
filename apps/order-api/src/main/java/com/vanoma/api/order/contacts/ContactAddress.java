package com.vanoma.api.order.contacts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "contact_address",
        uniqueConstraints = @UniqueConstraint(columnNames = {"contact_id", "address_id"}),
        indexes = {
                @Index(name = "contact_address_customer_id_idx", columnList = "customer_id", unique = false),
                @Index(name = "contact_address_contact_id_idx", columnList = "contact_id", unique = false),
                @Index(name = "contact_address_contact_id_address_id_idx", columnList = "contact_id,address_id", unique = true)
        })
public class ContactAddress {

    @Id
    @Column(name = "contact_address_id", nullable = false)
    private String contactAddressId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "contact_id", referencedColumnName = "contact_id")
    // You might need to uncomment the line below if returning contact in JSON
    // @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JsonIgnore
    private Contact contact;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "address_id", referencedColumnName = "address_id")
    @JsonIgnore
    private Address address;

    @Column(name = "last_note", nullable = true)
    private String lastNote;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public ContactAddress() {
        this.contactAddressId = UUID.randomUUID().toString();
    }

    public ContactAddress(String customerId, Contact contact, Address address) {
        this.contactAddressId = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.address = address;
        this.contact = contact;
    }

    public String getContactAddressId() {
        return contactAddressId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public ContactAddress setCustomerId(String customerId) {
        this.customerId = customerId;
        return this;
    }

    public Contact getContact() {
        return contact;
    }

    public ContactAddress setContact(Contact contact) {
        this.contact = contact;
        return this;
    }

    public Address getAddress() {
        return address;
    }

    public ContactAddress setAddress(Address address) {
        this.address = address;
        return this;
    }

    public String getLastNote() {
        return lastNote;
    }

    public ContactAddress setLastNote(String lastNote) {
        this.lastNote = lastNote;
        return this;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ContactAddress &&
                this.contactAddressId.equals(((ContactAddress) other).getContactAddressId());
    }
}
