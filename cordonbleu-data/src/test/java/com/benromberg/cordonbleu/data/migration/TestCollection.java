package com.benromberg.cordonbleu.data.migration;

import java.util.Map;

import org.mongojack.JacksonDBCollection;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBCollection;

public class TestCollection<T> extends JacksonDBCollection<T, String> {

    protected TestCollection(DBCollection dbCollection, JavaType type, JavaType keyType, ObjectMapper objectMapper,
            Class<?> view, Map<org.mongojack.JacksonDBCollection.Feature, Boolean> features) {
        super(dbCollection, type, keyType, objectMapper, view, features);
    }

}
