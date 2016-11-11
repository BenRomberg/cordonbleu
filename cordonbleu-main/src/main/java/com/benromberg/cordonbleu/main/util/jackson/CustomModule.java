package com.benromberg.cordonbleu.main.util.jackson;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class CustomModule extends SimpleModule {
    public CustomModule() {
        addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
    }

    public static ObjectMapper enhanceMapper(ObjectMapper mapper) {
        return mapper.registerModule(new CustomModule());
    }
}
