package com.vanoma.api.order.external;

import com.vanoma.api.utils.httpwrapper.HttpResult;
import com.vanoma.api.utils.httpwrapper.IHttpClientWrapper;
import lombok.Builder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Builder
@Service
public class AuthApiCaller implements IAuthApiCaller {
    private IHttpClientWrapper httpClient;
    private String authURL;

    @Override
    public HttpResult createLogin(Map<String, Object> data) {
        String fullUrl = this.authURL + "/login-creation";
        return this.httpClient.post(fullUrl, data);
    }

    @Override
    public HttpResult deleteLogin(Map<String, Object> data) {
        String fullUrl = this.authURL + "/login-deletion";
        return this.httpClient.post(fullUrl, data);
    }
}
