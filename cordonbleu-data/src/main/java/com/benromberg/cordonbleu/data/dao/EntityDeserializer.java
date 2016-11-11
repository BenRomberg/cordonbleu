package com.benromberg.cordonbleu.data.dao;

import java.io.IOException;

import com.benromberg.cordonbleu.data.model.Entity;
import com.benromberg.cordonbleu.data.util.jackson.ReferenceResolver;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class EntityDeserializer<T extends Entity<String>> extends JsonDeserializer<T> {
    private final ReferenceResolver<String, T> dao;

    public EntityDeserializer(ReferenceResolver<String, T> dao) {
        this.dao = dao;
    }

    @Override
    public T deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return dao.findById(parser.getValueAsString()).get();
    }
}
