package com.vanoma.api.order.maps;

public class AddressLine {
    private String houseNumber;
    private String streetName;

    public AddressLine() {
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public AddressLine setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
        return this;
    }

    public String getStreetName() {
        return streetName;
    }

    public AddressLine setStreetName(String streetName) {
        this.streetName = streetName;
        return this;
    }
}
