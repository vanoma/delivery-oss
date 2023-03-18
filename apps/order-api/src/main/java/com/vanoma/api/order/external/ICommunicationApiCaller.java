package com.vanoma.api.order.external;

import com.vanoma.api.utils.httpwrapper.HttpResult;

import java.util.concurrent.CompletableFuture;

public interface ICommunicationApiCaller {

    CompletableFuture<HttpResult> sendWebPush(WebPushParams params);

    CompletableFuture<HttpResult> sendCallback(CallbackParams params);

    CompletableFuture<HttpResult> sendSMS(String text, String phoneNumber);

    CompletableFuture<HttpResult> sendEmail(EmailParams emailParams);

    HttpResult verifyOtp(String otpId, String otpCode, String phoneNumber);
}
