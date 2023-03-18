package com.vanoma.api.order.customers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vanoma.api.order.contacts.Address;
import com.vanoma.api.order.contacts.Contact;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "branch")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class Branch {
    @Id
    @Column(name = "branch_id", nullable = false)
    private String branchId;

    @Column(name = "branch_name", nullable = false)
    private String branchName;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contact_id", referencedColumnName = "contact_id")
    private Contact contact;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_id", referencedColumnName = "address_id")
    private Address address;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public Branch() { }

    public Branch(Customer customer) {
        this.branchId = UUID.randomUUID().toString();
        this.customer = customer;
        this.isDeleted = false;
    }

    public String getBranchId() {
        return branchId;
    }

    public Branch setBranchName(String branchName) {
        this.branchName = branchName;
        return this;
    }

    public String getBranchName() {
        return branchName;
    }

    public Branch setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
        return this;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    @JsonIgnore
    public Customer getCustomer() {
        return customer;
    }

    public Branch setContact(Contact contact) {
        this.contact = contact;
        return this;
    }

    public Contact getContact() {
        return contact;
    }

    public Branch setAddress(Address address) {
        this.address = address;
        return this;
    }

    public Address getAddress() {
        return address;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Branch &&
                this.branchId.equals(((Branch) other).getBranchId());
    }

    @Override
    public String toString() {
        String branchStr = "BRANCH\n(" +
                "\tbranchId = " + this.branchId + "\n";
        if (branchName != null) branchStr += "\tbranchName = " + branchName + "\n";
        if (isDeleted != null) branchStr += "\tisDeleted = " + isDeleted + "\n";
        return branchStr + ")";
    }
}
