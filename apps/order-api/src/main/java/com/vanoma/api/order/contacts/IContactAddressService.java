package com.vanoma.api.order.contacts;

import org.springframework.http.ResponseEntity;

import javax.json.JsonPatch;
import java.util.List;
import java.util.Map;

public interface IContactAddressService {

    // Contact Entity Methods
    Contact saveContact(Contact contact);

    Contact findContactById(String contactId);

    Contact getContactById(String contactId);

    Address buildAddress(String customerId, AddressJson addressJson);

    ResponseEntity<Contact> createAndSaveContact(String customerId, ContactJson contactJson);

    ResponseEntity<Map<String, Object>> updateContact(String contactId, JsonPatch jsonPatch);

    ResponseEntity<Contact> getContact(String contactId);

    ResponseEntity<Map<String, Object>> getCustomerContacts(String customerId, Integer offset, Integer limit);

    ResponseEntity<Map<String, Object>> getCustomerDefaultContact(String customerId);

    // Address Entity Methods
    Address saveAddress(Address address);

    Address findAddressById(String addressId);

    ResponseEntity<Address> getAddress(String addressId);

    ResponseEntity<Address> createAndSaveAddress(String contactId, AddressJson addressJson);

    ResponseEntity<Address> updateAddress(String addressId, AddressJson json);

    Address getAddressById(String addressId);

    ResponseEntity<Map<String, Object>> getContactAddresses(String contactId, Integer offset, Integer limit);

    ResponseEntity<Map<String, Object>> getCustomerAddresses(String customerId, Integer offset, Integer limit);

    ResponseEntity<Map<String, Object>> getCustomerDefaultAddress(String customerId);

    // ContactAddress Entity Methods
    List<ContactAddress> saveContactAddressAll(List<ContactAddress> contactAddresses);

    ContactAddress createContactAddress(ContactAddress contactAddress);

    List<ContactAddress> getContactAddressByContact(Contact contact);

    ContactAddress getFirstContactAddressByContactAndAddress(Contact contact, Address address);

    ResponseEntity<ContactAddress> getContactAddress(String contactId, String addressId);

    ResponseEntity<String> removeAddressFromContact(String addressId, AddressRemovalJson addressRemovalJson);

    ResponseEntity<String> removeContact(String contactId);
}
