package com.vanoma.api.order.contacts;

import com.vanoma.api.order.contacts.*;
import com.vanoma.api.order.utils.annotations.PatchMappingJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.json.JsonPatch;
import java.util.Map;

@RestController
public class ContactController {

    @Autowired
    private IContactAddressService contactAddressService;

    @PostMapping(value = "/customers/{customerId}/contacts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Contact> createContact(
            @PathVariable String customerId, @RequestBody ContactJson contactJson) {
        return this.contactAddressService.createAndSaveContact(customerId, contactJson);
    }

    @PatchMapping(path = "/contacts/{contactId}",
            consumes = "application/json-patch+json",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> updateContact(
            @PathVariable String contactId, @RequestBody JsonPatch jsonPatch) {
        return this.contactAddressService.updateContact(contactId, jsonPatch);
    }

    @GetMapping(value = "/contacts/{contactId}")
    public ResponseEntity<Contact> getContact(@PathVariable String contactId) {
        return this.contactAddressService.getContact(contactId);
    }

    @GetMapping(value = "/customers/{customerId}/contacts")
    public ResponseEntity<Map<String, Object>> getCustomerContacts(
            @PathVariable String customerId,
            @RequestParam(required = false) String isDefault,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit) {
        if ("true".equalsIgnoreCase(isDefault)) {
            return this.contactAddressService.getCustomerDefaultContact(customerId);
        }
        return this.contactAddressService.getCustomerContacts(customerId, offset, limit);
    }

    @PostMapping(value = "/contacts/{contactId}/addresses", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Address> createAddress(
            @PathVariable String contactId, @RequestBody AddressJson addressJson) {
        return this.contactAddressService.createAndSaveAddress(contactId, addressJson);
    }

    @PatchMappingJson(path = "/addresses/{addressId}")
    public ResponseEntity<Address> updateAddress(
            @PathVariable String addressId,
            @RequestBody AddressJson json) {
        return this.contactAddressService.updateAddress(addressId, json);
    }

    @GetMapping(value = "/addresses/{addressId}")
    public ResponseEntity<Address> getAddress(@PathVariable String addressId) {
        return this.contactAddressService.getAddress(addressId);
    }


    @GetMapping(value = "/contacts/{contactId}/addresses")
    public ResponseEntity<Map<String, Object>> getContactAddresses(
            @PathVariable String contactId,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit) {
        return this.contactAddressService.getContactAddresses(contactId, offset, limit);
    }


    @GetMapping(value = "/customers/{customerId}/addresses")
    public ResponseEntity<Map<String, Object>> getCustomerAddresses(
            @PathVariable String customerId,
            @RequestParam(required = false) String isDefault,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit) {
        if ("true".equalsIgnoreCase(isDefault)) {
            return this.contactAddressService.getCustomerDefaultAddress(customerId);
        }
        return this.contactAddressService.getCustomerAddresses(customerId, offset, limit);
    }

    @GetMapping(value = "/contact-addresses")
    public ResponseEntity<ContactAddress> getContactAddress(
            @RequestParam(required = true) String contactId,
            @RequestParam(required = true) String addressId
    ) {
        return this.contactAddressService.getContactAddress(contactId, addressId);
    }

    @PostMapping(value = "/addresses/{addressId}/removal", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> removeAddress(
            @PathVariable String addressId, @RequestBody AddressRemovalJson addressRemovalJson) {
        return this.contactAddressService.removeAddressFromContact(addressId, addressRemovalJson);
    }

    @PostMapping(value = "/contacts/{contactId}/removal", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> removeContact(
            @PathVariable String contactId) {
        return this.contactAddressService.removeContact(contactId);
    }

}
