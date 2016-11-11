package com.benromberg.cordonbleu.data.util;

import com.mongodb.BasicDBObject;

public interface MongoCommand {
    public static final String ID_PROPERTY = "_id";

    default BasicDBObject object(String key, Object value) {
        return new BasicDBObject(key, value);
    }

    default BasicDBObject object() {
        return new BasicDBObject();
    }
}
