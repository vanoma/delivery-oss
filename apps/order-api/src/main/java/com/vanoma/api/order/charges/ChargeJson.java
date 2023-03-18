package com.vanoma.api.order.charges;

import com.vanoma.api.utils.exceptions.InvalidParameterException;

import java.io.Serializable;
import java.math.BigDecimal;

public class ChargeJson implements Serializable {

    private String packageId;
    private String type;
    private BigDecimal transactionAmount;
    private String description;

    public ChargeJson() {
    }

    public String getPackageId() {
        return packageId;
    }

    public ChargeType getType() {
        return ChargeType.create(type);
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public String getDescription() {
        return description;
    }

    // Setters for testing mostly
    public ChargeJson setPackageId(String packageId) {
        this.packageId = packageId;
        return this;
    }

    public ChargeJson setType(String type) {
        this.type = type;
        return this;
    }

    public ChargeJson setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
        return this;
    }


    public ChargeJson setDescription(String description) {
        this.description = description;
        return this;
    }

    public void validate() {
        if (type == null || type.trim().isEmpty()) {
            throw new InvalidParameterException("crud.charge.type.required");
        }
        if (getType() == ChargeType.DELIVERY_FEE) {
            throw new InvalidParameterException("crud.charge.type.noDeliveryFee");
        }
        if (transactionAmount == null || transactionAmount.compareTo(BigDecimal.ZERO) == 0) {
            throw new InvalidParameterException("crud.charge.transactionAmount.required");
        }
    }
}
