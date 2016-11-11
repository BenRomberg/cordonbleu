package com.benromberg.cordonbleu.data.dao.support;

import static com.benromberg.cordonbleu.util.ExceptionUtil.convertException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.mongojack.DBUpdate;
import org.mongojack.internal.object.BsonObjectGenerator;
import org.mongojack.internal.update.UpdateOperationValue;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBObject;

public class UpdateBuilder extends DBUpdate.Builder {
    private final Map<String, Object> additionalUpdates = new HashMap<>();
    private final ObjectMapper jsonMapper;

    public UpdateBuilder(ObjectMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public UpdateBuilder setOnInsert(Object value, String... removeFields) {
        additionalUpdates.put("$setOnInsert", convertToDbObject(value, removeFields));
        return this;
    }

    private Object convertToDbObject(Object object, String... removeFields) {
        BsonObjectGenerator generator = new BsonObjectGenerator();
        convertException(() -> jsonMapper.writer().writeValue(generator, object));
        return removeFields(generator.getValue(), removeFields);
    }

    private Object removeFields(Object value, String... removeFields) {
        if (removeFields.length == 0) {
            return value;
        }
        if (!(value instanceof DBObject)) {
            throw new IllegalStateException(String.format(
                    "Cannot removed fields %s from object %s as it cannot be cast to a DBObject.", removeFields, value));
        }
        DBObject dbObject = (DBObject) value;
        Stream.of(removeFields).forEach(removeField -> dbObject.removeField(removeField));
        return dbObject;
    }

    @Override
    public DBObject serialiseAndGet(ObjectMapper objectMapper, JavaType javaType) {
        DBObject parentResult = super.serialiseAndGet(objectMapper, javaType);
        parentResult.putAll(additionalUpdates);
        return parentResult;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && additionalUpdates.isEmpty();
    }

    @Override
    public UpdateBuilder inc(String field) {
        super.inc(field);
        return this;
    }

    @Override
    public UpdateBuilder inc(String field, int by) {
        super.inc(field, by);
        return this;
    }

    @Override
    public UpdateBuilder set(String field, Object value) {
        super.set(field, value);
        return this;
    }

    @Override
    public UpdateBuilder unset(String field) {
        super.unset(field);
        return this;
    }

    @Override
    public UpdateBuilder push(String field, Object value) {
        super.push(field, value);
        return this;
    }

    @Override
    public UpdateBuilder pushAll(String field, Object... values) {
        super.pushAll(field, values);
        return this;
    }

    @Override
    public UpdateBuilder pushAll(String field, List<?> values) {
        super.pushAll(field, values);
        return this;
    }

    @Override
    public UpdateBuilder addToSet(String field, Object value) {
        super.addToSet(field, value);
        return this;
    }

    @Override
    public UpdateBuilder addToSet(String field, Object... values) {
        super.addToSet(field, values);
        return this;
    }

    @Override
    public UpdateBuilder addToSet(String field, List<?> values) {
        super.addToSet(field, values);
        return this;
    }

    @Override
    public UpdateBuilder popFirst(String field) {
        super.popFirst(field);
        return this;
    }

    @Override
    public UpdateBuilder popLast(String field) {
        super.popLast(field);
        return this;
    }

    @Override
    public UpdateBuilder pull(String field, Object value) {
        super.pull(field, value);
        return this;
    }

    @Override
    public UpdateBuilder pullAll(String field, Object... values) {
        super.pullAll(field, values);
        return this;
    }

    @Override
    public UpdateBuilder pullAll(String field, List<?> values) {
        super.pullAll(field, values);
        return this;
    }

    @Override
    public UpdateBuilder rename(String oldFieldName, String newFieldName) {
        super.rename(oldFieldName, newFieldName);
        return this;
    }

    @Override
    public UpdateBuilder bit(String field, String operation, int value) {
        super.bit(field, operation, value);
        return this;
    }

    @Override
    public UpdateBuilder bit(String field, String operation1, int value1, String operation2, int value2) {
        super.bit(field, operation1, value1, operation2, value2);
        return this;
    }

    @Override
    public UpdateBuilder bitwiseAnd(String field, int value) {
        super.bitwiseAnd(field, value);
        return this;
    }

    @Override
    public UpdateBuilder bitwiseOr(String field, int value) {
        super.bitwiseOr(field, value);
        return this;
    }

    @Override
    public UpdateBuilder addRawOperation(String op, String field, Object value) {
        super.addRawOperation(op, field, value);
        return this;
    }

    @Override
    public UpdateBuilder addOperation(String modifier, String field, UpdateOperationValue value) {
        super.addOperation(modifier, field, value);
        return this;
    }

}
