package com.vanoma.api.order.external;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PaymentMethodParams {
    private String customerId;
    private String type;
    private String phoneNumber;
    private boolean isDefault;
}
