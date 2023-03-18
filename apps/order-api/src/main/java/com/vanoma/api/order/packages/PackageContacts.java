package com.vanoma.api.order.packages;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vanoma.api.order.contacts.Contact;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Needed by lombok builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PackageContacts {
    private String packageId;
    private String deliveryOrderId;
    private ContactPreview fromContact;
    private ContactPreview toContact;

    @Data
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ContactPreview {
        private String name;
        private String phoneNumberOne;
    }

    public static PackageContacts create(Package pkg) {
        Contact fromContact = pkg.getFromContact();
        Contact toContact = pkg.getToContact();
        return new PackageContactsBuilder()
                .packageId(pkg.getPackageId())
                .deliveryOrderId(pkg.getDeliveryOrder().getDeliveryOrderId())
                .fromContact(new ContactPreview(fromContact.getName(), fromContact.getPhoneNumberOne()))
                .toContact(new ContactPreview(toContact.getName(), toContact.getPhoneNumberOne()))
                .build();
    }
}
