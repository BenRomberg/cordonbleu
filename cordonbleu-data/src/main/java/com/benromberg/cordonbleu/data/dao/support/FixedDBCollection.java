package com.benromberg.cordonbleu.data.dao.support;

import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Fix for https://github.com/mongojack/mongojack/issues/127
 */
public class FixedDBCollection<T, K> extends JacksonDBCollection<T, K> {
    private final ObjectMapper objectMapper;
    private final JavaType type;

    public FixedDBCollection(DBCollection dbCollection, Class<T> type, Class<K> keyType, ObjectMapper objectMapper) {
        super(dbCollection, objectMapper.constructType(type), objectMapper.constructType(keyType), objectMapper, null,
                null);
        this.objectMapper = objectMapper;
        this.type = objectMapper.constructType(type);
    }

    @Override
    public DBObject serializeQuery(DBQuery.Query query) {
        return FixedSerializationUtils.serializeQuery(objectMapper, type, query);
    }
}
