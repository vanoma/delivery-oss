package com.vanoma.api.order.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.vanoma.api.order.charges.ChargeUtils;

import java.io.IOException;
import java.math.BigDecimal;

public class BigDecimalSerializer extends JsonSerializer<BigDecimal> {
    @Override
    public void serialize(BigDecimal bigDecimal, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeNumber(ChargeUtils.roundBigDecimal(bigDecimal));
    }
}
