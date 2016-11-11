package com.benromberg.cordonbleu.data.util.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class CaseInsensitiveUniqueDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        return parser.readValueAs(CaseInsensitiveUniqueValue.class).getValue();
    }
}
