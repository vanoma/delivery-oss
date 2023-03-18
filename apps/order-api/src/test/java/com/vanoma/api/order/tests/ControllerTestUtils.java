package com.vanoma.api.order.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ControllerTestUtils {

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static Map<String, Object> parseResponseBody(MockHttpServletResponse result) throws JsonProcessingException, UnsupportedEncodingException {
        return objectMapper.readValue(result.getContentAsString(StandardCharsets.UTF_8), Map.class);
    }

    public static String stringifyRequestBody(Map<String, Object> requestBody) throws JsonProcessingException {
        return objectMapper.writeValueAsString(requestBody);
    }
}
