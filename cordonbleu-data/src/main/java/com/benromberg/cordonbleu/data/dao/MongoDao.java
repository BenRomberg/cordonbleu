package com.benromberg.cordonbleu.data.dao;

import static com.benromberg.cordonbleu.util.ExceptionUtil.convertException;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.persistence.EntityExistsException;

import org.mongojack.Aggregation.Pipeline;
import org.mongojack.AggregationResult;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate;
import org.mongojack.JacksonDBCollection;
import org.mongojack.internal.MongoJackModule;
import org.mongojack.internal.object.BsonObjectGenerator;

import com.benromberg.cordonbleu.data.dao.support.FixedDBCollection;
import com.benromberg.cordonbleu.data.dao.support.UpdateBuilder;
import com.benromberg.cordonbleu.data.model.Entity;
import com.benromberg.cordonbleu.data.util.MongoCommand;
import com.benromberg.cordonbleu.data.util.jackson.CustomModule;
import com.benromberg.cordonbleu.data.util.jackson.JsonMapper;
import com.benromberg.cordonbleu.data.util.jackson.ReferenceResolver;
import com.benromberg.cordonbleu.data.validation.NullValidation;
import com.benromberg.cordonbleu.data.validation.Validation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoServerException;
import com.mongodb.connection.ServerVersion;

public class MongoDao<I, E extends Entity<I>> implements ReferenceResolver<I, E>, MongoCommand {
    private static final int DUPLICATE_KEY_ERROR_CODE = 11000;
    private static final ServerVersion MONGO_MINIMUM_VERSION = new ServerVersion(2, 6);

    private final DB database;
    private Class<I> idClass;
    private final Class<E> elementClass;
    private final JacksonDBCollection<E, I> collection;
    private ObjectMapper jsonMapper;
    private Validation<? super E> validation;

    public MongoDao(DatabaseWithMigration database, Class<I> idClass, Class<E> elementClass, String collectionName) {
        this(database, idClass, elementClass, collectionName, new CustomModule());
    }

    public MongoDao(DatabaseWithMigration database, Class<I> idClass, Class<E> elementClass, String collectionName,
            CustomModule customModule) {
        this(database, idClass, elementClass, collectionName, customModule, new NullValidation<E>());
    }

    public MongoDao(DatabaseWithMigration database, Class<I> idClass, Class<E> elementClass, String collectionName,
            CustomModule customModule, Validation<? super E> validation) {
        assertMongoVersion(database.getDatabase());
        this.database = database.getDatabase();
        this.idClass = idClass;
        this.elementClass = elementClass;
        this.jsonMapper = createJsonMapperForMongo(customModule);
        this.collection = getWrappedCollection(collectionName);
        this.validation = validation;
    }

    private void assertMongoVersion(DB database) {
        String stringVersion = database.command("buildInfo").getString("version");
        ServerVersion mongoVersion = new ServerVersion(Stream.of(stringVersion.split(Pattern.quote(".")))
                .map(part -> Integer.parseInt(part)).collect(toList()));
        if (mongoVersion.compareTo(MONGO_MINIMUM_VERSION) < 0) {
            throw new IllegalStateException(String.format("Mongo version %s does not satisfy required version %s.",
                    mongoVersion, MONGO_MINIMUM_VERSION));
        }
    }

    protected Object convertToDbObject(Object object, String... removeFields) {
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

    protected long count() {
        return collection.count();
    }

    protected long getCount(DBQuery.Query query) {
        return collection.getCount(query);
    }

    @Override
    public Optional<E> findById(I id) {
        return Optional.ofNullable(collection.findOneById(id));
    }

    public boolean remove(I id) {
        return collection.findAndRemove(createIdQuery(id)) != null;
    }

    protected void remove(Query query) {
        collection.remove(query);
    }

    public void insert(E element) {
        validation.validateEntity(element);
        wrapDuplicateKeyException(() -> collection.insert(element));
    }

    public void insertIfNotExists(E element) {
        validation.validateEntity(element);
        collection.update(DBQuery.is(ID_PROPERTY, element.getId()), element, true, false);
    }

    private <T> T wrapDuplicateKeyException(Supplier<T> func) {
        try {
            return func.get();
        } catch (MongoServerException e) { // cannot use DuplicateKeyException as Mongo uses MongoCommandException on
                                           // update (not Fongo though), see https://github.com/fakemongo/fongo/pull/200
            if (e.getCode() == DUPLICATE_KEY_ERROR_CODE) {
                throw new EntityExistsException(e.getMessage(), e);
            }
            throw e;
        }
    }

    protected DBCursor<E> find() {
        return collection.find();
    }

    protected Optional<E> findOne(Query query) {
        return Optional.ofNullable(collection.findOne(query));
    }

    protected DBCursor<E> find(Query query) {
        return collection.find(query);
    }

    private JacksonDBCollection<E, I> getWrappedCollection(String collectionName) {
        return new FixedDBCollection<>(database.getCollection(collectionName), elementClass, idClass, jsonMapper);
    }

    private Query createIdQuery(I id) {
        return DBQuery.is(ID_PROPERTY, id);
    }

    protected void createIndex(String property) {
        collection.createIndex(object(property, 1), indexDefaultOptions());
    }

    protected void createUniqueIndex(String... properties) {
        Map<String, Integer> map = Stream.of(properties).collect(toMap(property -> property, property -> 1));
        collection.createIndex(new BasicDBObject(map), indexDefaultOptions().append("unique", true));
    }

    private BasicDBObject indexDefaultOptions() {
        return object("background", true);
    }

    protected Optional<E> update(I id, DBUpdate.Builder update) {
        return update(DBQuery.is(ID_PROPERTY, id), update);
    }

    protected Optional<E> update(Query find, DBUpdate.Builder update) {
        return wrapDuplicateKeyException(() -> Optional.ofNullable(collection.findAndModify(find, null, null, false,
                update, true, false)));
    }

    protected E insertOrUpdate(I id, UpdateBuilder update) {
        return collection.findAndModify(object(ID_PROPERTY, id), null, null, false, update, true, true);
    }

    protected UpdateBuilder update() {
        return new UpdateBuilder(jsonMapper);
    }

    protected void updateMulti(Query find, DBUpdate.Builder update) {
        wrapDuplicateKeyException(() -> collection.updateMulti(find, update));
    }

    protected List<?> findDistinct(String field) {
        return collection.distinct(field);
    }

    protected <S> AggregationResult<S> aggregate(Pipeline<?> pipeline, Class<S> resultType) {
        return collection.aggregate(pipeline, resultType);
    }

    private static ObjectMapper createJsonMapperForMongo(CustomModule customModule) {
        ObjectMapper mapper = JsonMapper.wrapInstance(new ObjectMapper());
        customModule.enhanceMapper(mapper);
        MongoJackModule.configure(mapper);
        return mapper;
    }

    public String getCollectionName() {
        return collection.getName();
    }
}