package com.benromberg.cordonbleu.data.dao;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.benromberg.cordonbleu.data.migration.MongoMigration;
import com.mongodb.DB;

@Singleton
public class DatabaseWithMigration {
    private final DB database;

    @Inject
    public DatabaseWithMigration(DB database, MongoMigration migration) {
        this.database = database;
        migration.run();
    }

    public DB getDatabase() {
        return database;
    }
}
