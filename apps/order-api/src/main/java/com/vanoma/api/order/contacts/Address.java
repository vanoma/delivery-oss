package com.vanoma.api.order.contacts;

import com.bedatadriven.jackson.datatype.jts.serialization.GeometrySerializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vanoma.api.order.maps.KigaliDistrict;
import com.vanoma.api.utils.exceptions.InvalidParameterException;
import com.vanoma.api.utils.input.GeometryUtil;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "address",
        indexes = {
                @Index(name = "address_customer_id_is_saved_idx", columnList = "customer_id,is_saved", unique = false),
                @Index(name = "address_house_number_street_name_is_confirmed_idx", columnList = "house_number,street_name,is_confirmed", unique = false)

        })
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class Address {

    @Id
    @Column(name = "address_id", nullable = false)
    private String addressId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "address_name", nullable = true)
    private String addressName;

    @Column(name = "house_number", nullable = true)
    private String houseNumber;

    @Column(name = "street_name", nullable = true)
    private String streetName;

    @Column(name = "apartment_number", nullable = true)
    private String apartmentNumber;

    @Column(name = "floor", nullable = true)
    private String floor;

    @Column(name = "room", nullable = true)
    private String room;

    @Column(name = "district",
            length = 32,
            nullable = false)
    @Enumerated(EnumType.STRING)
    private KigaliDistrict district;

    // Needed for Jackson -- response JSON
    @Transient
    private Double latitude;
    @Transient
    private Double longitude;

    @Type(type = "org.locationtech.jts.geom.Point")
    @Column(name = "coordinates", nullable = false, columnDefinition = "geometry")
    @JsonSerialize(using = GeometrySerializer.class)
    private Point coordinates;

    @Column(name = "place_name", nullable = true)
    private String placeName;

    @Column(name = "landmark", nullable = true)
    private String landmark;

    @Column(name = "is_default",
            columnDefinition = "boolean default false not null")
    private Boolean isDefault = false;

    @Column(name = "is_saved",
            columnDefinition = "boolean default true not null")
    private Boolean isSaved = true;

    @Column(name = "is_confirmed",
            columnDefinition = "boolean default false not null")
    private Boolean isConfirmed = false;

    @Column(name = "parent_address_id", nullable = true)
    private String parentAddressId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    void preInsert() {
        if (this.isDefault == null) this.isDefault = false;
        if (this.isConfirmed == null) this.isConfirmed = false;
        if (this.isSaved == null) this.isSaved = true;
    }

    public Address() {
    }

    public Address(String customerId) {
        this.addressId = UUID.randomUUID().toString();
        this.customerId = customerId;
    }


    @Override
    public boolean equals(Object other) {
        return other instanceof Address && this.addressId.equals(((Address) other).getAddressId());
    }

    public static Address create(String customerId, AddressJson addressJson) {
        validateRequiredFields(addressJson);
        return new Address(customerId)
                .setHouseNumber(addressJson.getHouseNumber())
                .setStreetName(addressJson.getStreetName())
                .setApartmentNumber(addressJson.getApartmentNumber())
                .setDistrict(addressJson.getDistrict())
                .setAddressName(addressJson.getAddressName())
                .setPlaceName(addressJson.getPlaceName())
                .setCoordinates(addressJson.getLatitude(), addressJson.getLongitude())
                .setIsSaved(addressJson.getIsSaved())
                .setIsDefault(addressJson.getIsDefault());
    }

    private static void validateRequiredFields(AddressJson addressJson) {
        if (addressJson.getLatitude() == null || addressJson.getLongitude() == null) {
            throw new InvalidParameterException("crud.address.coordinates.required");
        }
        if (addressJson.getDistrict() == null) {
            throw new InvalidParameterException("crud.address.district.required");
        }
        // getIsSaved() can be null. Using `getIsSaved() && ...` will throw exception.
        if ((addressJson.getIsSaved() == null || addressJson.getIsSaved()) && addressJson.getAddressName() == null) {
            throw new InvalidParameterException("crud.address.addressName.required");
        }
    }

    public Address buildCopy(Boolean isSaved) {
        return new Address(this.customerId)
                .setIsSaved(isSaved)
                .setIsDefault(false)
                .setAddressName(this.addressName)
                .setPlaceName(this.placeName)
                .setCoordinates(this.getLatitude(), this.getLongitude())
                .setHouseNumber(this.houseNumber)
                .setStreetName(this.streetName)
                .setApartmentNumber(this.apartmentNumber)
                .setDistrict(this.district)
                .setIsConfirmed(this.isConfirmed)
                .setParentAddressId(this.addressId);

    }

    public String getAddressId() {
        return addressId;
    }

    public Address setAddressId(String addressId) {
        this.addressId = addressId;
        return this;
    }

    public String getCustomerId() {
        return customerId;
    }


    public String getHouseNumber() {
        return houseNumber;
    }

    public Address setHouseNumber(String houseNumber) {
        if (houseNumber == null) return this;
        this.houseNumber = houseNumber.toUpperCase();
        return this;
    }

    public String getStreetName() {
        return streetName;
    }

    public Address setStreetName(String streetName) {
        if (streetName == null) return this;
        this.streetName = streetName.toUpperCase();
        return this;
    }

    public String getApartmentNumber() {
        return apartmentNumber;
    }

    public Address setApartmentNumber(String apartmentNumber) {
        this.apartmentNumber = apartmentNumber;
        return this;
    }

    public String getFloor() {
        return floor;
    }

    public Address setFloor(String floor) {
        this.floor = floor;
        return this;
    }

    public String getRoom() {
        return room;
    }

    public Address setRoom(String room) {
        this.room = room;
        return this;
    }

    public String getAddressName() {
        return addressName;
    }

    public Address setAddressName(String addressName) {
        this.addressName = addressName;
        return this;
    }

    public Address setDistrict(KigaliDistrict district) {
        this.district = district;
        return this;
    }

    public Address setCoordinates(double latitude, double longitude) {
        if (!GeometryUtil.areValid(latitude, longitude)) {
            throw new InvalidParameterException("crud.address.invalidCoordinates");
        }
        this.coordinates = GeometryUtil.parseLocation(latitude, longitude);
        return this;
    }

    public Address setCoordinates(Map<String, Object> value) {
        if (value != null) {
            createCoordinatesFromMap(value);
        }
        return this;
    }

    public Point getCoordinates() {
        return coordinates;
    }

    private void createCoordinatesFromMap(Map<String, Object> coordinates) {
        List<Double> rawCoordinates = (List<Double>) coordinates.get("coordinates");
        double latitude = rawCoordinates.get(0);
        double longitude = rawCoordinates.get(1);
        this.coordinates = GeometryUtil.parseLocation(latitude, longitude);
    }

    public KigaliDistrict getDistrict() {
        return district;
    }

    public double getLatitude() {
        return coordinates.getX();
    }

    public double getLongitude() {
        return coordinates.getY();
    }

    public Boolean getIsSaved() {
        return isSaved;
    }

    public Address setIsSaved(Boolean saved) {
        isSaved = saved;
        return this;
    }

    public String getPlaceName() {
        return placeName;
    }

    public Address setPlaceName(String placeName) {
        this.placeName = placeName;
        return this;
    }

    public String getLandmark() {
        return landmark;
    }

    public Address setLandmark(String landmark) {
        this.landmark = landmark;
        return this;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public Address setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
        return this;
    }

    public Boolean getIsConfirmed() {
        return isConfirmed;
    }

    public Address setIsConfirmed(Boolean confirmed) {
        isConfirmed = confirmed;
        return this;
    }

    public String getParentAddressId() {
        return parentAddressId;
    }

    public Address setParentAddressId(String parentAddressId) {
        this.parentAddressId = parentAddressId;
        return this;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public static String getDistrictFromStreetName(String streetName) {
        if (streetName == null) return null;
        String district = null;
        if (streetName.toUpperCase().startsWith("KK")) {
            district = KigaliDistrict.KICUKIRO.name();
        } else if (streetName.toUpperCase().startsWith("KN")) {
            district = KigaliDistrict.NYARUGENGE.name();
        } else if (streetName.toUpperCase().startsWith("KG")) {
            district = KigaliDistrict.GASABO.name();
        }
        return district;
    }

    @Override
    public String toString() {
        String addressStr = "ADDRESS\n(" +
                "\taddressId = " + this.addressId + "\n";
        if (houseNumber != null) addressStr += "\thouseNumber = " + houseNumber + "\n";
        if (streetName != null) addressStr += "\tstreetName = " + streetName + "\n";
        if (district != null) addressStr += "\tdistrict = " + district + "\n";
        if (placeName != null) addressStr += "\tplaceName = " + houseNumber + "\n";
        if (addressName != null) addressStr += "\taddressName = " + houseNumber + "\n";
        if (latitude != null) addressStr += "\tlatitude = " + latitude + "\n";
        if (longitude != null) addressStr += "\tlongitude = " + houseNumber + "\n";
        if (parentAddressId != null) addressStr += "\tparentAddressId = " + parentAddressId + "\n";
        return addressStr + ")";
    }
}
