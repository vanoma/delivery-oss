package com.vanoma.api.order.contacts;

import java.io.Serializable;

public class ContactJson implements Serializable {

    private String name;
    private String contactId;
    private String phoneNumberOne;
    private String phoneNumberTwo;
    private Boolean isDefault;

    public ContactJson() {
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumberOne() {
        return phoneNumberOne;
    }

    public String getPhoneNumberTwo() {
        return phoneNumberTwo;
    }

    public String getContactId() {
        return contactId;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    // For testing
    public ContactJson setName(String name) {
        this.name = name;
        return this;
    }

    // For testing
    public ContactJson setPhoneNumberOne(String phoneNumberOne) {
        this.phoneNumberOne = phoneNumberOne;
        return this;
    }

    // For testing
    public ContactJson setPhoneNumberTwo(String phoneNumberTwo) {
        this.phoneNumberTwo = phoneNumberTwo;
        return this;
    }

    // For testing
    public ContactJson setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
        return this;
    }


    public boolean isEmpty() {
        return this.contactId == null && this.phoneNumberOne == null;
    }
}
