package com.vanoma.api.order.tests;

import com.vanoma.api.order.contacts.Address;
import com.vanoma.api.order.contacts.Contact;
import com.vanoma.api.order.contacts.ContactAddress;
import com.vanoma.api.order.contacts.IContactAddressService;
import com.vanoma.api.order.maps.KigaliDistrict;
import com.vanoma.api.order.orders.DeliveryOrder;
import com.vanoma.api.order.packages.Package;
import com.vanoma.api.order.packages.PackagePriority;
import com.vanoma.api.order.packages.PackageRepository;
import com.vanoma.api.order.packages.PackageSize;
import com.vanoma.api.utils.input.NumberUtils;
import com.vanoma.api.utils.input.TimeUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;

public class ObjectFactory {

    public static Address createAndSaveAddress(String customerId,
                                               IContactAddressService contactAddressService, String contactId) {
        Address address = createAddress(customerId);
        contactAddressService.saveAddress(address);

        ContactAddress contactAddress = createContactAddress(contactId, address, contactAddressService);
        contactAddressService.createContactAddress(contactAddress);
        return address;
    }

    public static Address createAddress(String customerId) {
        Address address = new Address(customerId)
                .setHouseNumber(getRandomHouseNumber())
                .setStreetName(getRandomStreetName())
                .setAddressName(UUID.randomUUID().toString().substring(0, 7))
                .setDistrict(KigaliDistrict.valueOf(getRandomDistrict().toUpperCase()))
                .setCoordinates(30.058, -1.949);
        return address;
    }

    private static ContactAddress createContactAddress(String contactId, Address address,
                                                       IContactAddressService contactAddressService) {
        ContactAddress contactAddress = new ContactAddress(
                address.getCustomerId(), contactAddressService.getContactById(contactId), address);
        return contactAddressService.createContactAddress(contactAddress);
    }

    public static Contact createAndSaveContact(IContactAddressService contactAddressService, String customerId) {
        Contact contact = createContact(customerId);
        return contactAddressService.saveContact(contact);
    }

    public static Contact createContact(String customerId) {
        Contact contact = new Contact(customerId)
                .setName(UUID.randomUUID().toString().toUpperCase().substring(0, 6))
                .setPhoneNumberOne(getRandomPhoneNumber())
                .setPhoneNumberTwo(getRandomPhoneNumber());
        return contact;
    }

    public static String getRandomString() {
        return RandomStringUtils.randomAlphabetic(5) + " " + RandomStringUtils.randomAlphabetic(5);
    }

    public static String getRandomPhoneNumber() {
        return "2507" + NumberUtils.getRandomLongInRange((long) 1e7, (long) 1e8);
    }

    private static String getRandomHouseNumber() {
        return String.valueOf(NumberUtils.getRandomLongInRange(1, 999));
    }

    private static String getRandomStreetName() {
        List<String> districtOptions = Arrays.asList("KK", "KG", "KN");
        String randomDistrictAbbreviation = districtOptions.get((int) NumberUtils.getRandomLongInRange(0, 2));
        String randomStreetNumber = String.valueOf(NumberUtils.getRandomLongInRange(1, 300));
        List<String> suffixOptions = Arrays.asList("ST", "AVE");
        String randomSuffix = suffixOptions.get((int) NumberUtils.getRandomLongInRange(0, 1));
        return randomDistrictAbbreviation + " " + randomStreetNumber + " " + randomSuffix;
    }

    private static String getRandomDistrict() {
        List<String> districtOptions = Arrays.asList("Kicukiro", "Gasabo", "Nyarugenge");
        return districtOptions.get((int) NumberUtils.getRandomLongInRange(0, 2));
    }

    private static Package createPackage(DeliveryOrder deliveryOrder) {
        String customerId = deliveryOrder.getCustomerId();
        Package orderPackage = new Package(deliveryOrder)
                .setSize(PackageSize.SMALL)
                .setPriority(PackagePriority.NORMAL);
        orderPackage.setFromContact(createContact(customerId));
        orderPackage.setFromAddress(createAddress(customerId));
        orderPackage.setToContact(createContact(customerId));
        orderPackage.setToAddress(createAddress(customerId));
        orderPackage.setPickUpStart(TimeUtils.getUtcNow());
        return orderPackage;
    }

    public static Package createRandomPackage(DeliveryOrder deliveryOrder, PackageRepository packageRepository) {
        Package pkg = createPackage(deliveryOrder);
        return packageRepository.save(pkg);
    }
}
