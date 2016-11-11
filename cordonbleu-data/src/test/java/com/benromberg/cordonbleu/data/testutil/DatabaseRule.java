package com.benromberg.cordonbleu.data.testutil;

import org.junit.rules.ExternalResource;

import com.github.fakemongo.Fongo;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.connection.ServerVersion;

public class DatabaseRule extends ExternalResource {
    public static final String FONGO_NAME = "fongo server";
    public static final String DBNAME = "unittest";
    private static MongoClient mongo = new Fongo(FONGO_NAME, new ServerVersion(2, 6)).getMongo();

    private boolean dropIndexes;

    @Override
    protected void after() {
        clearAllCollections();
    }

    public DatabaseRule withRealMongo() {
        mongo = new MongoClient();
        return this;
    }

    public DatabaseRule withDropIndexes() {
        dropIndexes = true;
        return this;
    }

    private void clearAllCollections() {
        DB db = getDB();
        db.getCollectionNames().forEach(collectionName -> clearCollection(db, collectionName));
    }

    private void clearCollection(DB db, String collectionName) {
        if ("system.indexes".equals(collectionName)) {
            return;
        }
        DBCollection collection = db.getCollection(collectionName);
        collection.remove(new BasicDBObject());
        if (dropIndexes) {
            collection.dropIndexes();
        }
    }

    @SuppressWarnings("deprecation")
    public static DB getDB() {
        return mongo.getDB(DBNAME);
    }

    public static MongoClient getMongoClient() {
        return mongo;
    }
}