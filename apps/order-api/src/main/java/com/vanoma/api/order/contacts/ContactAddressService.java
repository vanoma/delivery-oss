package com.vanoma.api.order.contacts;

import com.vanoma.api.order.maps.AddressLine;
import com.vanoma.api.order.maps.Coordinates;
import com.vanoma.api.order.maps.IGeocodingService;
import com.vanoma.api.order.utils.JsonPatchMapper;
import com.vanoma.api.utils.exceptions.ExceptionUtils;
import com.vanoma.api.utils.exceptions.InvalidParameterException;
import com.vanoma.api.utils.exceptions.ResourceNotFoundException;
import com.vanoma.api.utils.httpwrapper.HttpResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.stereotype.Repository;

import javax.json.JsonPatch;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ContactAddressService implements IContactAddressService {

    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private ContactAddressRepository contactAddressRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private JsonPatchMapper<Contact> contactJsonPatchMapper;
    @Autowired
    private JsonPatchMapper<Address> addressJsonPatchMapper;
    @Autowired
    private IGeocodingService geocodingService;

    @Override
    public Contact saveContact(Contact contact) {
        return this.contactRepository.save(contact);
    }

    @Override
    public Contact findContactById(String contactId) {
        return this.contactRepository.findById(contactId).orElse(null);
    }

    @Override
    public Contact getContactById(String contactId) {
        return this.contactRepository.getById(contactId);
    }

    @Override
    public ResponseEntity<Contact> createAndSaveContact(String customerId, ContactJson contactJson) {
        this.validateContactCreate(contactJson);
        Contact contact = Contact.create(customerId, contactJson);
        this.validateNotExactExistingContact(contact);
        this.invalidatePreviousDefaultContact(contact, customerId);
        this.saveContact(contact);
        return new ResponseEntity<>(contact, HttpStatus.CREATED);
    }

    private void validateContactCreate(ContactJson contactJson) {
        if (contactJson.getPhoneNumberOne() == null || contactJson.getPhoneNumberOne().trim().isEmpty()) {
            throw new InvalidParameterException("crud.contact.phoneNumberOne.required");
        }
    }

    private void validateNotExactExistingContact(Contact contact) {
        Contact existingContact = this.contactRepository
                .findFirstByCustomerIdAndPhoneNumberOneAndIsSaved(
                        contact.getCustomerId(), contact.getPhoneNumberOne(), true
                );
        if (existingContact != null) {
            throw new InvalidParameterException("crud.contact.existingContact");
        }
    }

    private void invalidatePreviousDefaultContact(Contact contact, String customerId) {
        if (contact.getIsDefault() != null && contact.getIsDefault()) {
            Contact existingDefault = this.contactRepository
                    .findFirstByCustomerIdAndIsSavedAndIsDefault(customerId, true, true);
            if (existingDefault != null) {
                existingDefault.setIsDefault(false);
                this.saveContact(existingDefault);
            }
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> updateContact(String contactId, JsonPatch jsonPatch) {
        validateContactPatch(jsonPatch);
        Contact contact = this.findContactById(contactId);
        if (contact == null) throw new ResourceNotFoundException("crud.contact.notFound");
        // TODO Check if is default & invalidate previous one -- if any.
        Contact updatedContact = this.contactJsonPatchMapper.apply(contact, jsonPatch);
        this.saveContact(updatedContact);
        Map<String, Object> response = new HashMap<>();
        response.put("contactId", contactId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void validateContactPatch(JsonPatch jsonPatch) {
        for (JsonValue item : jsonPatch.toJsonArray()) {
            String path = item.asJsonObject().getString("path");
            if ("/contactId".equals(path) || "/customerId".equals(path)) {
                throw new InvalidParameterException("crud.model.forbiddenPatchPath");
            }
        }
    }

    @Override
    public ResponseEntity<Contact> getContact(String contactId) {
        Contact contact = this.findContactById(contactId);
        if (contact == null) throw new ResourceNotFoundException("crud.contact.notFound");
        return new ResponseEntity<>(contact, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Map<String, Object>> getCustomerContacts(String customerId, Integer offset, Integer limit) {
        // Implement pagination: https://stackoverflow.com/questions/25008472/pagination-in-spring-data-jpa-limit-and-offset
        List<Contact> contacts = this.contactRepository.findByCustomerIdAndIsSavedOrderByNameAsc(customerId, true);
        Map<String, Object> response = new HashMap();
        response.put("contacts", contacts);
        response.put("totalCount", contacts.size());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Map<String, Object>> getCustomerDefaultContact(String customerId) {
        Contact contact = this.contactRepository.findFirstByCustomerIdAndIsSavedAndIsDefault(customerId, true, true);
        List<Contact> contacts = new ArrayList<>();
        int totalCount = 0;
        if (contact != null) {
            totalCount = 1;
            contacts.add(contact);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("contacts", contacts);
        response.put("totalCount", totalCount);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public Address saveAddress(Address address) {
        return this.addressRepository.save(address);
    }

    @Override
    public Address findAddressById(String addressId) {
        return this.addressRepository.findById(addressId).orElse(null);
    }

    @Override
    public ResponseEntity<Address> getAddress(String addressId) {
        Address address = this.findAddressById(addressId);
        if (address == null) throw new ResourceNotFoundException("crud.address.notFound");
        return new ResponseEntity<>(address, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Address> createAndSaveAddress(String contactId, AddressJson addressJson) {
        Contact contact = this.findContactById(contactId);
        if (contact == null) throw new ResourceNotFoundException("crud.contact.notFound");
        Address address = this.buildAddress(contact.getCustomerId(), addressJson);
        // TODO Check if is default & invalidate previous one -- if any.
        this.invalidatePreviousDefaultAddress(address, contact.getCustomerId());
        this.saveAddress(address);
        this.createContactAddress(contact.getContactId(), address);
        return new ResponseEntity<>(address, HttpStatus.CREATED);
    }


    @Override
    public Address buildAddress(String customerId, AddressJson addressJson) {
        this.addMissingFields(addressJson);
        return Address.create(customerId, addressJson);
    }

    private void addMissingFields(AddressJson addressJson) {
        if (addressJson.getLatitude() == null || addressJson.getLongitude() == null) {
            this.useDataFromGeocoding(addressJson);
        }
        if (!addressJson.hasDistrict()) {
            this.useDataFromReverseGeocoding(addressJson);
        }
    }

    private void useDataFromGeocoding(AddressJson addressJson) {
        AddressLine addressLine = new AddressLine()
                .setHouseNumber(addressJson.getHouseNumber())
                .setStreetName(addressJson.getStreetName());
        HttpResult result = this.geocodingService.geocode(addressLine);

        if (result.isSuccess()) {
            addressJson.setLatitude((Double) result.getBody().get("latitude"));
            addressJson.setLongitude((Double) result.getBody().get("longitude"));
            if (addressJson.getDistrict() == null) {
                addressJson.setDistrict((String) result.getBody().get("district"));
            }
        }
    }

    private void useDataFromReverseGeocoding(AddressJson addressJson) {
        HttpResult result = this.geocodingService.reverseGeocode(new Coordinates()
                .setLat(addressJson.getLatitude())
                .setLng(addressJson.getLongitude()));

        if (result.isSuccess()) {
            addressJson.setDistrict((String) result.getBody().get("district"));
            String streetName = (String) result.getBody().get("streetName");
            streetName = StringUtils.isEmpty(addressJson.getStreetName()) ?
                    streetName : addressJson.getStreetName();
            addressJson.setStreetName(streetName);
        }
    }

    private void invalidatePreviousDefaultAddress(Address address, String customerId) {
        if (address.getIsDefault() != null && address.getIsDefault()) {
            Address existingDefault = this.addressRepository
                    .findFirstByCustomerIdAndIsSavedAndIsDefault(customerId, true, true);
            if (existingDefault != null) {
                existingDefault.setIsDefault(false);
                this.saveAddress(existingDefault);
            }
        }
    }

    @Override
    public ResponseEntity<Address> updateAddress(String addressId, AddressJson json) {
        Address address = this.addressRepository.getById(addressId);

        if (json.getCoordinates() != null) {
            address.setCoordinates(json.getCoordinates().toMap());
        }
        if (json.getIsConfirmed() != null) {
            address.setIsConfirmed(Boolean.parseBoolean(json.getIsConfirmed()));
        }
        if (json.getHouseNumber() != null) {
            address.setHouseNumber(json.getHouseNumber());
        }

        return ResponseEntity.ok(this.saveAddress(address));
    }

    @Override
    public Address getAddressById(String addressId) {
        return this.addressRepository.getById(addressId);
    }

    @Override
    public ResponseEntity<Map<String, Object>> getContactAddresses(String contactId, Integer offset, Integer limit) {
        // Implement pagination: https://stackoverflow.com/questions/25008472/pagination-in-spring-data-jpa-limit-and-offset
        Contact contact = this.getContactById(contactId);
        List<ContactAddress> contactAddresses = this.contactAddressRepository.findByContactOrderByCreatedAtDesc(contact);
        List<Address> addresses = contactAddresses.stream().map(ContactAddress::getAddress).collect(Collectors.toList());
        Map<String, Object> response = new HashMap();
        response.put("addresses", addresses);
        response.put("totalCount", addresses.size());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Map<String, Object>> getCustomerAddresses(String customerId, Integer offset, Integer limit) {
        // Implement pagination: https://stackoverflow.com/questions/25008472/pagination-in-spring-data-jpa-limit-and-offset
        List<ContactAddress> contactAddresses = this.contactAddressRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        List<Address> addresses = contactAddresses.stream().map(ContactAddress::getAddress).collect(Collectors.toList());
        Map<String, Object> response = new HashMap();
        response.put("addresses", addresses);
        response.put("totalCount", addresses.size());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Map<String, Object>> getCustomerDefaultAddress(String customerId) {
        Address address = this.addressRepository.findFirstByCustomerIdAndIsSavedAndIsDefault(customerId, true, true);
        List<Address> addresses = new ArrayList<>();
        int totalCount = 0;
        if (address != null) {
            totalCount = 1;
            addresses.add(address);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("addresses", addresses);
        response.put("totalCount", totalCount);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void createContactAddress(String contactId, Address address) {
        ContactAddress contactAddress = new ContactAddress(
                address.getCustomerId(), this.getContactById(contactId), address);
        try {
            this.createContactAddress(contactAddress);
        } catch (DataIntegrityViolationException | JpaObjectRetrievalFailureException ex) {
            throw new ResourceNotFoundException("crud.contact.notFound");
        }
    }

    @Override
    public ContactAddress createContactAddress(ContactAddress contactAddress) {
        return this.contactAddressRepository.save(contactAddress);
    }

    @Override
    public List<ContactAddress> saveContactAddressAll(List<ContactAddress> contactAddresses) {
        return this.contactAddressRepository.saveAll(contactAddresses);
    }

    @Override
    public List<ContactAddress> getContactAddressByContact(Contact contact) {
        return this.contactAddressRepository.findByContact(contact);
    }

    @Override
    public ContactAddress getFirstContactAddressByContactAndAddress(Contact contact, Address address) {
        return this.contactAddressRepository.findFirstByContactAndAddress(contact, address);
    }

    @Override
    public ResponseEntity<ContactAddress> getContactAddress(String contactId, String addressId) {
        ContactAddress contactAddress;
        try {
            contactAddress = this.getFirstContactAddressByContactAndAddress(
                    this.contactRepository.getById(contactId),
                    this.addressRepository.getById(addressId)
            );
        } catch (JpaObjectRetrievalFailureException ex) {
            String entityName = ExceptionUtils.getEntityNameFromEntityNotFoundException(ex.getMessage());
            throw new ResourceNotFoundException("crud." + entityName.toLowerCase() + ".notFound");
        }
        if (contactAddress == null) throw new ResourceNotFoundException("crud.contactAddress.notFound");
        return new ResponseEntity<>(contactAddress, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> removeAddressFromContact(String addressId, AddressRemovalJson addressRemovalJson) {
        this.validateAddressRemovalJson(addressRemovalJson);
        String contactId = addressRemovalJson.getContactId();
        ContactAddress contactAddress = fetchContactAddress(addressId, contactId);
        if (contactAddress == null) throw new ResourceNotFoundException("crud.address.notFound");
        this.contactAddressRepository.delete(contactAddress);
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<String> removeContact(String contactId) {
        Contact contact = this.findContactById(contactId);
        if (contact == null) {
            throw new InvalidParameterException("crud.contact.notFound");
        }
        contact.setIsSaved(false);
        this.saveContact(contact);
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    private ContactAddress fetchContactAddress(String addressId, String contactId) {
        Contact lazyContact = this.getContactById(contactId);
        Address lazyAddress = this.getAddressById(addressId);
        ContactAddress contactAddress;
        try {
            contactAddress = this.contactAddressRepository
                    .findFirstByContactAndAddress(lazyContact, lazyAddress);

        } catch (JpaObjectRetrievalFailureException ex) {
            String entityName = ExceptionUtils.getEntityNameFromEntityNotFoundException(ex.getMessage());
            throw new ResourceNotFoundException("crud." + entityName.toLowerCase() + ".notFound");
        }
        return contactAddress;
    }

    private void validateAddressRemovalJson(AddressRemovalJson addressRemovalJson) {
        if (addressRemovalJson == null || addressRemovalJson.getContactId() == null) {
            throw new InvalidParameterException("crud.address.removal.contactId.required");
        }
    }
}
