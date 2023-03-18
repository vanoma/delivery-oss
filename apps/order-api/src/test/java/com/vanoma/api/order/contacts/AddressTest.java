package com.vanoma.api.order.contacts;

import com.vanoma.api.order.maps.KigaliDistrict;
import com.vanoma.api.utils.exceptions.InvalidParameterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AddressTest {

    private String customerId;
    private AddressJson addressJson;

    @BeforeEach
    public void setUp() {
        this.customerId = UUID.randomUUID().toString();
        this.addressJson = new AddressJson()
                .setHouseNumber("23")
                .setStreetName("KK 177 ST")
                .setAddressName("Glads Apt")
                .setIsDefault(true)
                .setDistrict("kicukiro")
                .setLatitude(30.213)
                .setLongitude(-1.970)
                .setIsSaved(true);
    }

    @Test
    public void testAddress_create_createsNewAddressWithCustomerIdAndJsonData() {
        Address address = Address.create(customerId, addressJson);

        assertThat(address.getAddressId()).isNotNull();
        assertThat(address.getHouseNumber()).isEqualTo(addressJson.getHouseNumber());
        assertThat(address.getStreetName()).isEqualTo(addressJson.getStreetName());
        assertThat(address.getAddressName()).isEqualTo(addressJson.getAddressName());
        assertThat(address.getLatitude()).isEqualTo(addressJson.getLatitude());
        assertThat(address.getLongitude()).isEqualTo(addressJson.getLongitude());
        assertThat(address.getIsDefault()).isTrue();
        assertThat(address.getIsSaved()).isTrue();
        assertThat(address.getDistrict().toString()).isEqualTo(KigaliDistrict.KICUKIRO.name());
    }

    @Test
    public void testAddress_create_convertsHouseNumberToUpperCase() {
        addressJson.setHouseNumber("d12");
        Address address = Address.create(customerId, addressJson);
        assertThat(address.getHouseNumber()).isEqualTo("D12");
    }

    @Test
    public void testAddress_create_convertsStreetNameToUpperCase() {
        addressJson.setStreetName("kk 177 st");
        Address address = Address.create(customerId, addressJson);
        assertThat(address.getStreetName()).isEqualTo("KK 177 ST");
    }

    @Test
    public void testAddress_create_throwsInvalidParameterErrorWhenLatitudeIsBelowMinus90() {
        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            new Address(UUID.randomUUID().toString())
                    .setCoordinates(-2000, 50);
        });
        assertThat(exception.getMessage()).isEqualTo("crud.address.invalidCoordinates");
    }

    @Test
    public void testAddress_create_throwsInvalidParameterErrorWhenLatitudeIsAbovePositive90() {
        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            new Address(UUID.randomUUID().toString())
                    .setCoordinates(2000, 50);
        });
        assertThat(exception.getMessage()).isEqualTo("crud.address.invalidCoordinates");
    }

    @Test
    public void testAddress_create_throwsInvalidParameterErrorWhenLongitudeIsBelowMinus180() {
        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            new Address(UUID.randomUUID().toString())
                    .setCoordinates(50, -2000);
        });
        assertThat(exception.getMessage()).isEqualTo("crud.address.invalidCoordinates");
    }

    @Test
    public void testAddress_create_throwsInvalidParameterErrorWhenLatitudeIsAbovePositive180() {
        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            new Address(UUID.randomUUID().toString())
                    .setCoordinates(50, 2000);
        });
        assertThat(exception.getMessage()).isEqualTo("crud.address.invalidCoordinates");
    }

    @Test
    public void testAddress_buildCopy_copiesPlaceName() {
        Address address = new Address(UUID.randomUUID().toString())
                .setCoordinates(-1.94, 30.0)
                .setPlaceName("Hotel Ikaze");

        Address copy = address.buildCopy(false);

        assertThat(copy.getPlaceName()).isEqualTo("Hotel Ikaze");
    }
}
