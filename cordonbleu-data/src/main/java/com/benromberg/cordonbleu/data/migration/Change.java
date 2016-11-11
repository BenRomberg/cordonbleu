package com.benromberg.cordonbleu.data.migration;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.benromberg.cordonbleu.data.util.KeyPairGenerator;
import com.benromberg.cordonbleu.data.util.MongoCommand;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoCommandException;

public class Change implements MongoCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(Change.class);

    // Global state, unfortunately no other way to pass fields generically.
    private static MongoMigration migration;

    public static void setMigration(MongoMigration mongoMigration) {
        Change.migration = mongoMigration;
    }

    public ChangeCollection getCollection(String collectionName) {
        return new ChangeCollection(migration.getDatabase().getCollection(collectionName));
    }

    public KeyPairGenerator getKeyPairGenerator() {
        return migration.getKeyPairGenerator();
    }

    protected BasicDBObject $setOne(String field, Object value) {
        return object("$set", object(field, value));
    }

    protected BasicDBObject $unset(String... fields) {
        BasicDBObject object = object();
        Stream.of(fields).forEach(field -> object.append(field, null));
        return object("$unset", object);
    }

    protected BasicDBObject merge(DBObject... objects) {
        BasicDBObject merged = object();
        Stream.of(objects).forEach(object -> object.keySet().forEach(key -> merged.append(key, object.get(key))));
        return merged;
    }

    public class ChangeCollection {
        private final DBCollection collection;

        private ChangeCollection(DBCollection collection) {
            this.collection = collection;
        }

        public void updateAll(DBObject update) {
            collection.updateMulti(object(), update);
        }

        public void updateAll(Function<DBObject, DBObject> callback) {
            updateWithCallback(collection.find(), callback);
        }

        private void updateWithCallback(DBCursor relevantDocuments, Function<DBObject, DBObject> callback) {
            // Snapshot mode sufficient (and important), as we're merely updating documents. Without snapshot mode,
            // we'd potentially get the same documents multiple times when updating them in between.
            // See also: https://docs.mongodb.org/manual/reference/method/cursor.snapshot/
            relevantDocuments.snapshot().forEach(document -> {
                Object id = document.get(ID_PROPERTY);
                collection.update(object(ID_PROPERTY, id), callback.apply(document));
            });
        }

        public void updateFieldIfNotSet(String field, Function<DBObject, Object> callback) {
            updateWithCallback(collection.find(object(field, null)),
                    document -> $setOne(field, callback.apply(document)));
        }

        public boolean isEmpty() {
            return collection.count() == 0;
        }

        public void insert(DBObject document) {
            collection.insert(document);
        }

        public void updateAllIds(Function<DBObject, Object> idCallback) {
            // First need to save all oldIds. Inserting while collection old IDs would return new documents as well,
            // as even snapshot-mode does not guarantee not returning new elements while querying the cursor.
            Set<Object> oldIds = collection.find(object(), object(ID_PROPERTY, 1)).toArray().stream()
                    .map(document -> document.get(ID_PROPERTY)).collect(toSet());
            oldIds.forEach(id -> {
                DBObject document = collection.findOne(id);
                document.put(ID_PROPERTY, idCallback.apply(document));
                collection.insert(document);
            });
            oldIds.forEach(oldId -> collection.remove(object(ID_PROPERTY, oldId)));
        }

        public void drop() {
            collection.drop();
        }

        public void dropIndexIfExists(String field) {
            try {
                collection.dropIndex(object(field, 1));
            } catch (MongoCommandException e) {
                LOGGER.info("Could not find index {}.{}.", collection.getName(), field);
            }
        }

        public List<Object> findAllIds() {
            return collection.find(object(), object(ID_PROPERTY, 1)).snapshot().toArray().stream()
                    .map(dbObject -> dbObject.get(ID_PROPERTY)).collect(toList());
        }
    }
}
