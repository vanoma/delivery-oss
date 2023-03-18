package com.vanoma.api.order.external;

import com.vanoma.api.utils.httpwrapper.HttpResult;
import com.vanoma.api.utils.httpwrapper.IHttpClientWrapper;
import lombok.Builder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Builder
@Service
public class PaymentAPICaller implements IPaymentAPICaller {

    private IHttpClientWrapper httpClient;
    private String paymentApiUrl;
    private String orderApiUrl;

    public HttpResult requestPayment(PaymentRequestParams params) {
        String fullUrl = String.format("%s/payment-requests", paymentApiUrl);
        Map<String, Object> requestBody = Map.of(
                "paymentRequestId", params.getPaymentRequestId(),
                "totalAmount", params.getTransactionBreakdown().getTotalAmount().doubleValue(),
                "transactionAmount", params.getTransactionBreakdown().getTransactionAmount().doubleValue(),
                "transactionFee", params.getTransactionBreakdown().getTransactionFee().doubleValue(),
                "paymentMethod", Map.of(
                        "paymentMethodId", params.getPaymentMethodId()
                ),
                "callbackUrl", String.format("%s/delivery-payment-requests/%s/callbacks", this.orderApiUrl, params.getPaymentRequestId()),
                "description", "Delivery transaction"
        );
        return this.httpClient.post(fullUrl, requestBody);
    }

    @Override
    public HttpResult confirmPayment(PaymentRecordParams params) {
        String fullUrl = String.format("%s/payment-records", paymentApiUrl);
        Map<String, Object> requestBody = Map.of(
                "paymentRequestId", params.getPaymentRequestId(),
                "totalAmount", params.getTransactionBreakdown().getTotalAmount().doubleValue(),
                "transactionAmount", params.getTransactionBreakdown().getTransactionAmount().doubleValue(),
                "transactionFee", params.getTransactionBreakdown().getTransactionFee().doubleValue(),
                "paymentMethod", Map.of(
                        "paymentMethodId", params.getPaymentMethodId()
                ),
                "operatorTransactionId", params.getOperatorTransactionId(),
                "paymentTime", params.getPaymentTime(),
                "description", params.getDescription()
        );
        return this.httpClient.post(fullUrl, requestBody);
    }

    @Override
    public HttpResult createPaymentMethod(PaymentMethodParams params) {
        String fullUrl = String.format("%s/users/%s/payment-methods", paymentApiUrl, params.getCustomerId());
        Map<String, Object> requestBody = Map.of(
                "isDefault", params.isDefault(),
                "type", params.getType(),
                "extra", Map.of(
                        "phoneNumber", params.getPhoneNumber()
                )
        );
        return this.httpClient.post(fullUrl, requestBody);
    }
}
