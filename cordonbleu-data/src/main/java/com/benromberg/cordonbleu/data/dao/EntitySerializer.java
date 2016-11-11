package com.benromberg.cordonbleu.data.dao;

import java.io.IOException;

import com.benromberg.cordonbleu.data.model.Entity;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class EntitySerializer<T extends Entity<String>> extends JsonSerializer<T> {
    @Override
    public void serialize(T value, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeString(value.getId());
    }
}
