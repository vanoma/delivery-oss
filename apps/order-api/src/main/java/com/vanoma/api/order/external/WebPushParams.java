package com.vanoma.api.order.external;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class WebPushParams {
    private String heading;
    private String message;
    private List<String> receiverIds;
    private Map<String, Object> jsonData;
    private Map<String, String> metadata;
}
