package com.vanoma.api.order.packages;

import com.vanoma.api.order.contacts.AddressJson;
import com.vanoma.api.order.contacts.ContactJson;
import com.vanoma.api.utils.exceptions.InvalidParameterException;
import com.vanoma.api.utils.input.TimeUtils;

import java.io.Serializable;
import java.time.OffsetDateTime;

public class PackageJson implements Serializable {
    // Default value used for driverId and assignmentId. That's because driverId and assignmentId
    // can have null as a legitimate values; delivery-api calls order-api with null values when an
    // assignment is cancelled.
    public static final String NO_VALUE = "NO_VALUE";

    private String packageId;
    private String deliveryOrderId;
    private String size;
    private String priority;
    private ContactJson fromContact;
    private ContactJson toContact;
    private AddressJson fromAddress;
    private AddressJson toAddress;
    private String fromNote;
    private String toNote;
    private String fragileContent;
    private String eventCallback;
    private String pickUpStart;
    private String driverId = NO_VALUE;
    private String assignmentId = NO_VALUE;
    private String status;
    private String staffNote;
    private Boolean isAssignable;
    private String pickUpChangeNote;
    private Boolean enableNotifications;

    PackageJson() {
    }


    public String getPackageId() {
        return packageId;
    }

    public String getDeliveryOrderId() {
        return deliveryOrderId;
    }

    public PackageSize getSize() {
        return PackageSize.create(size);
    }

    public PackagePriority getPriority() throws InvalidParameterException {
        return PackagePriority.create(priority);
    }

    public ContactJson getFromContact() {
        return fromContact;
    }

    public ContactJson getToContact() {
        return toContact;
    }

    public AddressJson getFromAddress() {
        return fromAddress;
    }

    public AddressJson getToAddress() {
        return toAddress;
    }

    public String getFromNote() {
        return fromNote;
    }

    public String getToNote() {
        return toNote;
    }

    public String getFragileContent() {
        return fragileContent;
    }

    public String getEventCallback() {
        return eventCallback;
    }

    public OffsetDateTime getPickUpStart() {
        return TimeUtils.parseISOString(pickUpStart);
    }

    public String getPickUpChangeNote() {
        return pickUpChangeNote;
    }

    public String getDriverId() {
        return driverId;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public PackageStatus getStatus() {
        return PackageStatus.create(status);
    }

    public String getStaffNote() {
        return staffNote;
    }

    public Boolean getIsAssignable() {
        return isAssignable;
    }

    public Boolean getEnableNotifications() {
        return enableNotifications;
    }


    public boolean hasPickUp() {
        return fromContact != null && !fromContact.isEmpty()
                && fromAddress != null && !fromAddress.isEmpty();
    }

    public boolean hasDropOffAddress() {
        return toAddress != null && !toAddress.isEmpty();
    }

    public boolean hasDropOffContact() {
        return toContact != null && !toContact.isEmpty();
    }

    public void validate() {
        validateFromFields();

        // TODO: Validate toContact & toAddress as well. Those fields must be available when creating a
        //  package (either as an API user or Dashboard app user). However delivery requests are expected
        //  to miss toAddress, so we should not validate it.
        // validateToFields();
    }

    private void validateFromFields() {
        if (fromContact == null) {
            throw new InvalidParameterException("crud.package.fromContact.notFound");
        }

        if ((fromContact.getContactId() == null || fromContact.getContactId().trim().isEmpty()) &&
                (fromContact.getPhoneNumberOne() == null || fromContact.getPhoneNumberOne().trim().isEmpty())) {
            throw new InvalidParameterException("crud.package.fromContact.contactId.required");
        }

        if (fromAddress == null) {
            throw new InvalidParameterException("crud.package.fromAddress.notFound");
        }

        if ((fromAddress.getAddressId() == null || fromAddress.getAddressId().trim().isEmpty()) &&
                (fromAddress.getLatitude() == null || fromAddress.getLongitude() == null)) {
            throw new InvalidParameterException("crud.package.fromAddress.addressId.required");
        }
    }

    private void validateToFields() {
        if (toContact == null) {
            throw new InvalidParameterException("crud.package.toContact.notFound");
        }

        if ((toContact.getContactId() == null || toContact.getContactId().trim().isEmpty()) &&
                (toContact.getPhoneNumberOne() == null || toContact.getPhoneNumberOne().trim().isEmpty())) {
            throw new InvalidParameterException("crud.package.toContact.contactId.required");
        }

        if (toAddress == null) {
            throw new InvalidParameterException("crud.package.toAddress.notFound");
        }

        if ((toAddress.getAddressId() == null || toAddress.getAddressId().trim().isEmpty()) &&
                (toAddress.getLatitude() == null || toAddress.getLongitude() == null)) {
            throw new InvalidParameterException("crud.package.toAddress.addressId.required");
        }
    }
}
