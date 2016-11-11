package com.benromberg.cordonbleu.data.dao.support;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.mongojack.DBQuery;
import org.mongojack.DBRef;
import org.mongojack.MongoJsonMappingException;
import org.mongojack.internal.ObjectIdSerializer;
import org.mongojack.internal.object.BsonObjectGenerator;
import org.mongojack.internal.query.CollectionQueryCondition;
import org.mongojack.internal.query.CompoundQueryCondition;
import org.mongojack.internal.query.QueryCondition;
import org.mongojack.internal.query.SimpleQueryCondition;
import org.mongojack.internal.util.JacksonAccessor;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Fix for https://github.com/mongojack/mongojack/issues/127
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class FixedSerializationUtils {
    private static final Set<Class<?>> BASIC_TYPES;

    static {
        Set<Class<?>> types = new HashSet<Class<?>>();
        types.add(String.class);
        types.add(Integer.class);
        types.add(Boolean.class);
        types.add(Short.class);
        types.add(Long.class);
        types.add(BigInteger.class);
        types.add(Float.class);
        types.add(Double.class);
        types.add(Byte.class);
        types.add(Character.class);
        types.add(BigDecimal.class);
        types.add(int[].class);
        types.add(boolean[].class);
        types.add(short[].class);
        types.add(long[].class);
        types.add(float[].class);
        types.add(double[].class);
        types.add(byte[].class);
        types.add(char[].class);
        types.add(Date.class);
        // Patterns are used by the regex method of the query builder
        types.add(Pattern.class);
        // Native types that we support
        types.add(ObjectId.class);
        types.add(DBRef.class);
        BASIC_TYPES = types;
    }

    public static DBObject serializeQuery(ObjectMapper objectMapper, JavaType type, DBQuery.Query query) {
        SerializerProvider serializerProvider = JacksonAccessor.getSerializerProvider(objectMapper);
        JsonSerializer serializer = JacksonAccessor.findValueSerializer(serializerProvider, type);
        return serializeQuery(serializerProvider, serializer, query);
    }

    private static DBObject serializeQuery(SerializerProvider serializerProvider, JsonSerializer<?> serializer,
            DBQuery.Query query) {
        DBObject serializedQuery = new BasicDBObject();
        for (Map.Entry<String, QueryCondition> field : query.conditions()) {
            String key = field.getKey();
            QueryCondition condition = field.getValue();
            serializedQuery.put(key, serializeQueryCondition(serializerProvider, serializer, key, condition));
        }
        return serializedQuery;
    }

    public static Object serializeQueryCondition(ObjectMapper objectMapper, JavaType type, String key,
            QueryCondition condition) {
        SerializerProvider serializerProvider = JacksonAccessor.getSerializerProvider(objectMapper);
        JsonSerializer<?> serializer = JacksonAccessor.findValueSerializer(serializerProvider, type);
        return serializeQueryCondition(serializerProvider, serializer, key, condition);
    }

    private static Object serializeQueryCondition(SerializerProvider serializerProvider, JsonSerializer<?> serializer,
            String key, QueryCondition condition) {
        if (condition instanceof SimpleQueryCondition) {
            SimpleQueryCondition simple = (SimpleQueryCondition) condition;
            if (!simple.requiresSerialization() || simple.getValue() == null) {
                return simple.getValue();
            } else {
                if (!key.startsWith("$")) {
                    serializer = findQuerySerializer(false, key, serializerProvider, serializer);
                }
                return serializeQueryField(simple.getValue(), serializer, serializerProvider, key);
            }
        } else if (condition instanceof CollectionQueryCondition) {
            CollectionQueryCondition coll = (CollectionQueryCondition) condition;
            if (!key.startsWith("$")) {
                serializer = findQuerySerializer(coll.targetIsCollection(), key, serializerProvider, serializer);
            }
            List<Object> serializedConditions = new ArrayList<Object>();
            for (QueryCondition item : coll.getValues()) {
                serializedConditions.add(serializeQueryCondition(serializerProvider, serializer, "$", item));
            }
            return serializedConditions;
        } else {
            CompoundQueryCondition compound = (CompoundQueryCondition) condition;
            if (!key.startsWith("$")) {
                serializer = findQuerySerializer(false, key, serializerProvider, serializer);
            }
            return serializeQuery(serializerProvider, serializer, compound.getQuery());
        }
    }

    private static Object serializeQueryField(Object value, JsonSerializer serializer,
            SerializerProvider serializerProvider, String op) {
        if (serializer == null) {
            if (value == null || BASIC_TYPES.contains(value.getClass())) {
                // Return as is
                return value;
            } else if (value instanceof Collection) {
                Collection<?> coll = (Collection<?>) value;
                List<Object> copy = null;
                int position = 0;
                for (Object item : coll) {
                    Object returned = serializeQueryField(item, null, serializerProvider, op);
                    if (returned != item) {
                        if (copy == null) {
                            copy = new ArrayList<Object>(coll);
                        }
                        copy.set(position, returned);
                    }
                    position++;
                }
                if (copy != null) {
                    return copy;
                } else {
                    return coll;
                }
            } else if (value.getClass().isArray()) {
                if (BASIC_TYPES.contains(value.getClass().getComponentType())) {
                    return value;
                }
                Object[] array = (Object[]) value;
                Object[] copy = null;
                for (int i = 0; i < array.length; i++) {
                    Object returned = serializeQueryField(array[i], null, serializerProvider, op);
                    if (returned != array[i]) {
                        if (copy == null) {
                            copy = new Object[array.length];
                            System.arraycopy(array, 0, copy, 0, array.length);
                        }
                        copy[i] = returned;
                    }
                }
                if (copy != null) {
                    return copy;
                } else {
                    return array;
                }
            } else {
                // We don't know what it is, just find a serializer for it
                serializer = JacksonAccessor.findValueSerializer(serializerProvider, value.getClass());
            }
        }
        BsonObjectGenerator objectGenerator = new BsonObjectGenerator();
        try {
            serializer.serialize(value, objectGenerator, serializerProvider);
        } catch (IOException e) {
            throw new MongoJsonMappingException("Error serializing value " + value + " in DBQuery operation " + op, e);
        }
        return objectGenerator.getValue();
    }

    private static JsonSerializer<?> findQuerySerializer(boolean targetIsCollection, String fieldPath,
            SerializerProvider serializerProvider, JsonSerializer<?> serializer) {
        if (serializer instanceof BeanSerializerBase || serializer instanceof MapSerializer) {
            JsonSerializer<?> fieldSerializer = serializer;
            // Iterate through the components of the field name
            String[] fields = fieldPath.split("\\.");
            for (String field : fields) {
                if (fieldSerializer == null) {
                    // We don't have a field serializer to look up the field on,
                    // so give up
                    return null;
                }

                boolean isIndex = field.matches("\\d+");

                // First step into the collection if there is one
                if (!isIndex) {
                    while (fieldSerializer instanceof ContainerSerializer) {
                        JsonSerializer<?> contentSerializer = ((ContainerSerializer) fieldSerializer)
                                .getContentSerializer();
                        if (contentSerializer == null) {
                            // Work it out
                            JavaType contentType = ((ContainerSerializer) fieldSerializer).getContentType();
                            if (contentType != null) {
                                contentSerializer = JacksonAccessor
                                        .findValueSerializer(serializerProvider, contentType);
                            }
                        }
                        fieldSerializer = contentSerializer;
                    }
                }

                if (isIndex) {
                    if (fieldSerializer instanceof ContainerSerializer) {
                        JsonSerializer<?> contentSerializer = ((ContainerSerializer) fieldSerializer)
                                .getContentSerializer();
                        if (contentSerializer == null) {
                            // Work it out
                            JavaType contentType = ((ContainerSerializer) fieldSerializer).getContentType();
                            if (contentType != null) {
                                contentSerializer = JacksonAccessor
                                        .findValueSerializer(serializerProvider, contentType);
                            }
                        }
                        fieldSerializer = contentSerializer;
                    } else {
                        // Give up, don't attempt to serialise it
                        return null;
                    }
                } else if (fieldSerializer instanceof BeanSerializerBase) {
                    BeanPropertyWriter writer = JacksonAccessor.findPropertyWriter(
                            (BeanSerializerBase) fieldSerializer, field);
                    if (writer != null) {
                        fieldSerializer = writer.getSerializer();
                        if (fieldSerializer == null) {
                            // Do a generic lookup
                            fieldSerializer = JacksonAccessor.findValueSerializer(serializerProvider, writer.getType());
                        }
                    } else {
                        // Give up
                        return null;
                    }
                } else if (fieldSerializer instanceof MapSerializer) {
                    fieldSerializer = ((MapSerializer) fieldSerializer).getContentSerializer();
                } else {
                    // Don't know how to find what the serialiser for this field
                    // is
                    return null;
                }
            }
            // Now we have a serializer for the field, see if we're supposed to
            // be serialising for a collection
            if (targetIsCollection) {
                if (fieldSerializer instanceof ContainerSerializer) {
                    fieldSerializer = ((ContainerSerializer) fieldSerializer).getContentSerializer();
                } else if (fieldSerializer instanceof ObjectIdSerializer) {
                    // Special case for ObjectIdSerializer, leave as is, the
                    // ObjectIdSerializer handles both single
                    // values as well as collections with no problems.
                } else {
                    // Give up
                    return null;
                }
            }
            return fieldSerializer;
        } else {
            return null;
        }
    }
}
