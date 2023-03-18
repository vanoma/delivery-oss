package com.vanoma.api.order.external;

import com.vanoma.api.utils.httpwrapper.HttpResult;

import java.util.Map;

public interface IAuthApiCaller {
    HttpResult createLogin(Map<String, Object> data);

    HttpResult deleteLogin(Map<String, Object> data);
}
