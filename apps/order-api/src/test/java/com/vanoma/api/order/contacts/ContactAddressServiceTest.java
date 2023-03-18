package com.vanoma.api.order.contacts;

import com.vanoma.api.order.tests.ObjectFactory;
import com.vanoma.api.utils.exceptions.InvalidParameterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ContactAddressServiceTest {

    @Autowired
    private IContactAddressService contactAddressService;

    private String customerId;

    @BeforeEach
    public void setUp() {
        this.customerId = UUID.randomUUID().toString();
    }

    @Test
    public void testRemoveAddressFromContactDeleteContactAddressObject() {
        String customerId = UUID.randomUUID().toString();
        Contact contact = ObjectFactory.createAndSaveContact(contactAddressService, customerId);
        Address addressOne = ObjectFactory.createAndSaveAddress(customerId, contactAddressService, contact.getContactId());
        Address addressTwo = ObjectFactory.createAndSaveAddress(customerId, contactAddressService, contact.getContactId());

        ContactAddress contactAddress = this.contactAddressService
                .getFirstContactAddressByContactAndAddress(contact, addressOne);
        assertThat(contactAddress).isNotNull();

        AddressRemovalJson addressRemovalJson = new AddressRemovalJson()
                .setContactId(contact.getContactId());
        ResponseEntity<String> entity = this.contactAddressService
                .removeAddressFromContact(addressOne.getAddressId(), addressRemovalJson);
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ContactAddress withAddressOne = this.contactAddressService
                .getFirstContactAddressByContactAndAddress(contact, addressOne);
        assertThat(withAddressOne).isNull();

        ContactAddress withAddressTwo = this.contactAddressService
                .getFirstContactAddressByContactAndAddress(contact, addressTwo);
        assertThat(withAddressTwo).isNotNull();
    }
}
