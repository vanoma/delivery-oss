package com.vanoma.api.order.customers;

import com.vanoma.api.utils.exceptions.InvalidParameterException;
import lombok.Getter;

@Getter
public class BranchJson {
    private String branchName;
    private String contactId;
    private String addressId;

    public void validate() {
        if (branchName == null || branchName.trim().isEmpty()) {
            throw new InvalidParameterException("crud.branch.branchName.required");
        }
        if (contactId == null || contactId.trim().isEmpty()) {
            throw new InvalidParameterException("crud.branch.contactId.required");
        }
        if (addressId == null || addressId.trim().isEmpty()) {
            throw new InvalidParameterException("crud.branch.addressId.required");
        }
    }
}
