package com.vanoma.api.order.contacts;


import com.vanoma.api.order.maps.KigaliDistrict;
import com.vanoma.api.utils.exceptions.InvalidParameterException;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class AddressJson implements Serializable {
    private String addressId;
    private String houseNumber;
    private String streetName;
    private String apartmentNumber;
    private String district;
    private Double latitude;
    private Double longitude;
    private String addressName;
    private String placeName;
    private Boolean isSaved;
    private Boolean isDefault;
    private String isConfirmed;
    private CoordinatesJson coordinates;

    public String getAddressId() {
        return addressId;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public String getStreetName() {
        return streetName;
    }

    public String getApartmentNumber() {
        return apartmentNumber;
    }

    public boolean hasDistrict() {
        return district != null && !district.trim().isEmpty();
    }

    public KigaliDistrict getDistrict() throws InvalidParameterException {
        return KigaliDistrict.create(district);
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getAddressName() {
        return addressName;
    }

    public String getPlaceName() {
        return placeName;
    }

    public Boolean getIsSaved() {
        return isSaved;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public String getIsConfirmed() {
        return isConfirmed;
    }

    public CoordinatesJson getCoordinates() {
        return coordinates;
    }

    // Setters for testing

    public AddressJson setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
        return this;
    }

    public AddressJson setStreetName(String streetName) {
        this.streetName = streetName;
        return this;
    }

    public AddressJson setApartmentNumber(String apartmentNumber) {
        this.apartmentNumber = apartmentNumber;
        return this;
    }

    public AddressJson setDistrict(String district) {
        this.district = district;
        return this;
    }

    public AddressJson setLatitude(Double latitude) {
        this.latitude = latitude;
        return this;
    }

    public AddressJson setLongitude(Double longitude) {
        this.longitude = longitude;
        return this;
    }

    public AddressJson setAddressName(String addressName) {
        this.addressName = addressName;
        return this;
    }

    public AddressJson setPlaceName(String placeName) {
        this.placeName = placeName;
        return this;
    }

    public AddressJson setIsSaved(boolean isSaved) {
        this.isSaved = isSaved;
        return this;
    }

    public AddressJson setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
        return this;
    }

    public boolean isEmpty() {
        return this.addressId == null &&
                (this.latitude == null || this.longitude == null);
    }

    public static class CoordinatesJson {
        private String type;
        private List<Double> coordinates;

        public String getType() {
            return type;
        }

        public List<Double> getCoordinates() {
            return coordinates;
        }

        public Map<String, Object> toMap() {
            return Map.of(
                    "type", type,
                    "coordinates", coordinates
            );
        }
    }
}
