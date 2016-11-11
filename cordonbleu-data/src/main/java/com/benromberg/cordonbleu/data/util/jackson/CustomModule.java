package com.benromberg.cordonbleu.data.util.jackson;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class CustomModule extends SimpleModule {
    public CustomModule() {
        addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
    }

    public ObjectMapper enhanceMapper(ObjectMapper mapper) {
        return mapper.registerModule(this);
    }
}
