package com.vanoma.api.order.customers;

import com.vanoma.api.utils.exceptions.InvalidParameterException;
import lombok.Getter;

@Getter
public class AgentJson {
    private String fullName;
    private String phoneNumber;
    private String branchId;

    public void validate() {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new InvalidParameterException("crud.agent.fullName.required");
        }
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new InvalidParameterException("crud.agent.phoneNumber.required");
        }
    }
}
