package com.benromberg.cordonbleu.data.util.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class CaseInsensitiveUniqueSerializer extends JsonSerializer<String> {
    @Override
    public void serialize(String value, JsonGenerator generator, SerializerProvider serializers) throws IOException {
        CaseInsensitiveUniqueValue uniqueValue = new CaseInsensitiveUniqueValue(value);
        generator.writeStartObject();
        generator.writeStringField(CaseInsensitiveUniqueValue.PROPERTY_UNIQUE, uniqueValue.getUnique());
        generator.writeStringField(CaseInsensitiveUniqueValue.PROPERTY_VALUE, uniqueValue.getValue());
        generator.writeEndObject();
    }
}
