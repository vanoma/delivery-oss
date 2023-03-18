package com.vanoma.api.order.external;

import com.vanoma.api.utils.httpwrapper.HttpResult;
import com.vanoma.api.utils.httpwrapper.IHttpClientWrapper;
import lombok.Builder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Builder
@Service
public class DeliveryApiCaller implements IDeliveryApiCaller {
    private String deliveryApiUrl;
    private IHttpClientWrapper httpClient;

    @Override
    public HttpResult cancelAssignment(String assignmentId) {
        String fullUrl = String.format("%s/current-assignments/%s/cancellation", this.deliveryApiUrl, assignmentId);
        return this.httpClient.post(fullUrl, Map.of());
    }
}
