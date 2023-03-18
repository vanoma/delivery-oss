package com.vanoma.api.order.external;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class CallbackParams {
    private String callbackUrl;
    private Map<String, Object> payload;
}
