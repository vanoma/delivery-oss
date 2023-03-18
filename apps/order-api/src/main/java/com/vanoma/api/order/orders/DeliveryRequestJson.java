package com.vanoma.api.order.orders;


import com.vanoma.api.order.contacts.AddressJson;
import com.vanoma.api.order.contacts.ContactJson;
import com.vanoma.api.order.packages.PackageJson;
import com.vanoma.api.utils.exceptions.InvalidParameterException;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class DeliveryRequestJson implements Serializable {
    private String agentId;
    private Boolean isCustomerPaying;
    private List<PackageJson> packages;

    public void validate() {
        if (isCustomerPaying == null) {
            throw new InvalidParameterException("crud.deliveryRequest.isCustomerPaying.required");
        }
        for (PackageJson packageJson : packages) {
            ContactJson fromContact = packageJson.getFromContact();
            if (fromContact == null || StringUtils.isEmpty(fromContact.getContactId())) {
                throw new InvalidParameterException("crud.deliveryRequest.fromContact.contactId.required");
            }
            AddressJson fromAddress = packageJson.getFromAddress();
            if (fromAddress == null || StringUtils.isEmpty(fromAddress.getAddressId())) {
                throw new InvalidParameterException("crud.deliveryRequest.fromAddress.addressId.required");
            }
            ContactJson toContact = packageJson.getToContact();
            if (toContact == null) {
                throw new InvalidParameterException("crud.deliveryRequest.toContact.required");
            }
            if (StringUtils.isEmpty(toContact.getContactId()) &&
                    StringUtils.isEmpty(toContact.getPhoneNumberOne())) {
                throw new InvalidParameterException("crud.deliveryRequest.toContact.contactIdOrPhoneNumberOne.required");
            }
        }
    }
}
