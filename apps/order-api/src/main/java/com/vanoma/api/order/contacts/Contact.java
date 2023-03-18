package com.vanoma.api.order.contacts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vanoma.api.utils.input.PhoneNumberUtils;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "contact",
        indexes = {
                @Index(name = "contact_customer_id_is_saved_idx", columnList = "customer_id,is_saved", unique = false),
                @Index(name = "contact_customer_id_name_is_saved_idx", columnList = "customer_id,name,is_saved", unique = false),
                @Index(name = "contact_customer_id_phone_number_one_is_saved_idx", columnList = "customer_id,phone_number_one,is_saved", unique = false),
                @Index(name = "contact_customer_id_name_phone_number_one_is_saved_idx", columnList = "customer_id,name,phone_number_one,is_saved", unique = false),
        })
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class Contact implements Serializable {

    @Id
    @Column(name = "contact_id", nullable = false)
    private String contactId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "name", nullable = true)
    private String name;

    @Column(name = "phone_number_one", nullable = false)
    private String phoneNumberOne;

    @Column(name = "phone_number_two", nullable = true)
    private String phoneNumberTwo;

    @Column(name = "is_saved",
            columnDefinition = "boolean default true")
    private Boolean isSaved = true;

    @Column(name = "is_default",
            columnDefinition = "boolean default false not null")
    private Boolean isDefault = false;

    @Column(name = "parent_contact_id", nullable = true)
    private String parentContactId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    void preInsert() {
        if (this.isSaved == null) this.isSaved = true;
    }

    public Contact() {
    }

    public Contact(String customerId) {
        this.contactId = UUID.randomUUID().toString();
        this.customerId = customerId;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Contact && this.contactId.equals(((Contact) other).getContactId());
    }

    public static Contact create(String customerId, ContactJson contactJson) {
        return new Contact(customerId)
                .setName(contactJson.getName())
                .setPhoneNumberOne(contactJson.getPhoneNumberOne())
                .setPhoneNumberTwo(contactJson.getPhoneNumberTwo())
                .setIsDefault(contactJson.getIsDefault() != null && contactJson.getIsDefault());
    }

    public String getContactId() {
        return contactId;
    }

    public Contact setContactId(String contactId) {
        this.contactId = contactId;
        return this;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Contact setCustomerId(String customerId) {
        this.customerId = customerId;
        return this;
    }

    public String getName() {
        return name;
    }

    public Contact setName(String name) {
        this.name = name;
        return this;
    }

    public String getPhoneNumberOne() {
        return phoneNumberOne;
    }

    public Contact setPhoneNumberOne(String phoneNumberOne) {
        this.phoneNumberOne = PhoneNumberUtils.normalize(phoneNumberOne, true);
        return this;
    }

    public String getPhoneNumberTwo() {
        return phoneNumberTwo;
    }

    public Contact setPhoneNumberTwo(String phoneNumberTwo) {
        this.phoneNumberTwo = PhoneNumberUtils.normalize(phoneNumberTwo, false);
        return this;
    }

    public Boolean getIsSaved() {
        return isSaved;
    }

    public Contact setIsSaved(Boolean saved) {
        this.isSaved = saved;
        return this;
    }

    public String getParentContactId() {
        return parentContactId;
    }

    public Contact setParentContactId(String parentContactId) {
        this.parentContactId = parentContactId;
        return this;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public Contact setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
        return this;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Contact buildUnsavedCopy() {
        return new Contact(this.customerId)
                .setContactId(UUID.randomUUID().toString()) // Overwriting existing ID
                .setIsSaved(false)
                .setIsDefault(false)
                .setName(this.name)
                .setPhoneNumberOne(this.phoneNumberOne)
                .setPhoneNumberTwo(this.phoneNumberTwo)
                .setParentContactId(this.contactId);
    }

    @Override
    public String toString() {
        String contactStr = "CONTACT\n(" +
                "\tcontactId=" + this.contactId + "\n"
                + "\tname = " + this.name + "\n"
                + "\tphoneNumberOne = " + this.phoneNumberOne + "\n";
        if (phoneNumberTwo != null) contactStr += "\tphoneNumberTwo = " + phoneNumberTwo + "\n";
        if (parentContactId != null) contactStr += "\tparentContactId = " + parentContactId + "\n";
        return contactStr + ")";
    }
}
