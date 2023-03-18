package com.vanoma.api.order.external;

import com.vanoma.api.utils.httpwrapper.HttpResult;

public interface IPaymentAPICaller {
    HttpResult requestPayment(PaymentRequestParams params);

    HttpResult confirmPayment(PaymentRecordParams params);

    HttpResult createPaymentMethod(PaymentMethodParams params);
}
