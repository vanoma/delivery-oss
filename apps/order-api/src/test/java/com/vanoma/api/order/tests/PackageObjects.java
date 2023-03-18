package com.vanoma.api.order.tests;

public class PackageObjects {

    private String fromAddressId;
    private String toAddressId;
    private String fromContactId;
    private String toContactId;

    public PackageObjects() {
    }


    public String getFromAddressId() {
        return fromAddressId;
    }

    public PackageObjects setFromAddressId(String fromAddressId) {
        this.fromAddressId = fromAddressId;
        return this;
    }

    public String getToAddressId() {
        return toAddressId;
    }

    public PackageObjects setToAddressId(String toAddressId) {
        this.toAddressId = toAddressId;
        return this;
    }

    public String getFromContactId() {
        return fromContactId;
    }

    public PackageObjects setFromContactId(String fromContactId) {
        this.fromContactId = fromContactId;
        return this;
    }

    public String getToContactId() {
        return toContactId;
    }

    public PackageObjects setToContactId(String toContactId) {
        this.toContactId = toContactId;
        return this;
    }
}