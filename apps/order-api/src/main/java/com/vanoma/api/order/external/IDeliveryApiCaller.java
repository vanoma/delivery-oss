package com.vanoma.api.order.external;

import com.vanoma.api.utils.httpwrapper.HttpResult;

public interface IDeliveryApiCaller {
    HttpResult cancelAssignment(String assignmentId);
}
