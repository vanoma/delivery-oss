package com.vanoma.api.order.contacts;

import com.vanoma.api.utils.input.PhoneNumberUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContactTest {

    @Test
    public void testPhoneNumberUtils_normalizeReturnPhoneWithCountryCode() {
        String phoneNumber = "0788249927";
        assertEquals(25 + phoneNumber, PhoneNumberUtils.normalize(phoneNumber, true));
    }

    @Test
    public void testContact_create_createsContactFromContactJson() {
        String phoneNumberOne = "0788112233";
        ContactJson contactJson = new ContactJson()
                .setName("John Doe")
                .setPhoneNumberOne(phoneNumberOne);

        Contact contact = Contact.create("customer-id", contactJson);

        assertThat(contact.getContactId()).isNotNull();
        assertThat(contact.getName()).isEqualTo("John Doe");
        assertThat(contact.getPhoneNumberOne()).isEqualTo("25" + phoneNumberOne);
    }

    @Test
    public void testContact_create_savesPhoneNumberTwoWhenProvided() {
        String phoneNumberOne = "0788112233";
        String phoneNumberTwo = "0788445566";
        ContactJson contactJson = new ContactJson()
                .setName("John Doe")
                .setPhoneNumberOne(phoneNumberOne)
                .setPhoneNumberTwo(phoneNumberTwo);

        Contact contact = Contact.create("customer-id", contactJson);

        assertThat(contact.getPhoneNumberTwo()).isEqualTo("25" + phoneNumberTwo);
    }

    @Test
    public void testContact_create_setIsDefaultWhenProvided() {
        String phoneNumberOne = "0788112233";
        String phoneNumberTwo = "0788445566";
        ContactJson contactJson = new ContactJson()
                .setName("John Doe")
                .setPhoneNumberOne(phoneNumberOne)
                .setPhoneNumberTwo(phoneNumberTwo)
                .setIsDefault(true);

        Contact contact = Contact.create("customer-id", contactJson);

        assertTrue(contact.getIsDefault());
    }
}
