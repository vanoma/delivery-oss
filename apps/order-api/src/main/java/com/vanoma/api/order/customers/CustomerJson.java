package com.vanoma.api.order.customers;

import com.vanoma.api.utils.exceptions.InvalidParameterException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

@Getter
public class CustomerJson {
    public static final String NO_STRING_VALUE = "NO_VALUE";
    public static final BigDecimal NO_DECIMAL_VALUE = new BigDecimal("0.00");

    private String businessName;
    private String phoneNumber;
    private String otpId;
    private String otpCode;
    private BigDecimal weightingFactor;
    private Integer billingInterval;
    private Integer billingGracePeriod;
    private String postpaidExpiry = NO_STRING_VALUE;
    private BigDecimal fixedPriceAmount = NO_DECIMAL_VALUE;
    private String fixedPriceExpiry = NO_STRING_VALUE;

    public void validateForCreation() {
        if (businessName == null || businessName.trim().isEmpty()) {
            throw new InvalidParameterException("crud.customer.businessName.required");
        }
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new InvalidParameterException("crud.customer.phoneNumber.required");
        }
        if (otpId == null || otpId.trim().isEmpty()) {
            throw new InvalidParameterException("crud.otp.otpId.required");
        }
        if (otpCode == null || otpCode.trim().isEmpty()) {
            throw new InvalidParameterException("crud.otp.otpCode.required");
        }
    }

    public void validateForUpdate() {
        if ((fixedPriceAmount != NO_DECIMAL_VALUE && Objects.equals(fixedPriceExpiry, NO_STRING_VALUE)) || (fixedPriceAmount == NO_DECIMAL_VALUE && !Objects.equals(fixedPriceExpiry, NO_STRING_VALUE))) {
            throw new InvalidParameterException("crud.customer.fixedPriceFields.required");
        }
        if ((fixedPriceAmount != null && fixedPriceExpiry == null) || (fixedPriceAmount == null && fixedPriceExpiry != null)) {
            throw new InvalidParameterException("crud.customer.fixedPriceFields.required");
        }
    }
}
