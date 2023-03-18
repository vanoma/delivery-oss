package com.vanoma.api.order.external;

import com.vanoma.api.utils.httpwrapper.HttpResult;
import com.vanoma.api.utils.httpwrapper.IHttpClientWrapper;
import lombok.Builder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Builder
@Service
public class CommunicationApiCaller implements ICommunicationApiCaller {
    private String communicationApiUrl;
    private IHttpClientWrapper httpClient;

    @Override
    @Async
    public CompletableFuture<HttpResult> sendWebPush(WebPushParams params) {
        Map<String, Object> payload = Map.of(
                "heading", params.getHeading(),
                "message", params.getMessage(),
                "receiverIds", params.getReceiverIds(),
                "jsonData", params.getJsonData(),
                "metadata", params.getMetadata()
        );
        String fullUrl = this.communicationApiUrl + "/push";
        HttpResult result = this.httpClient.post(fullUrl, payload);
        return CompletableFuture.completedFuture(result);
    }

    @Override
    @Async
    public CompletableFuture<HttpResult> sendCallback(CallbackParams params) {
        HttpResult result = this.httpClient.post(params.getCallbackUrl(), params.getPayload());
        return CompletableFuture.completedFuture(result);
    }

    @Override
    @Async
    public CompletableFuture<HttpResult> sendSMS(String text, String phoneNumber) {
        Map<String, Object> payload = Map.of(
                "message", text,
                "phoneNumbers", List.of(phoneNumber),
                "serviceName", "DELIVERY_NOTIFICATION",
                "isUnicode", false
        );
        String fullUrl = this.communicationApiUrl + "/sms";
        HttpResult result = this.httpClient.post(fullUrl, payload);
        return CompletableFuture.completedFuture(result);
    }

    @Override
    @Async
    public CompletableFuture<HttpResult> sendEmail(EmailParams emailParams) {
        Map<String, Object> payload = Map.of(
                "subject", emailParams.getSubject(),
                "content", emailParams.getContent(),
                "recipients", emailParams.getRecipients()
        );
        String fullUrl = this.communicationApiUrl + "/email";
        HttpResult result = this.httpClient.post(fullUrl, payload);
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public HttpResult verifyOtp(String otpId, String otpCode, String phoneNumber) {
        String fullUrl = String.format("%s/otp/%s/verification", this.communicationApiUrl, otpId);
        return this.httpClient.post(fullUrl, Map.of("otpCode", otpCode, "phoneNumber", phoneNumber));
    }
}
