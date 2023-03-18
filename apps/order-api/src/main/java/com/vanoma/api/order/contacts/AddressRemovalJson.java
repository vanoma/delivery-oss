package com.vanoma.api.order.contacts;

import java.io.Serializable;

public class AddressRemovalJson implements Serializable {

    public AddressRemovalJson() {
    }

    private String contactId;

    public String getContactId() {
        return contactId;
    }

    // For testing
    public AddressRemovalJson setContactId(String contactId) {
        this.contactId = contactId;
        return this;
    }


}
