package com.benromberg.cordonbleu.data.migration;

import static com.benromberg.cordonbleu.util.ExceptionUtil.convertException;

import javax.inject.Inject;

import com.benromberg.cordonbleu.data.util.KeyPairGenerator;
import com.github.mongobee.Mongobee;
import com.mongodb.DB;

public class MongoMigration {
    private final KeyPairGenerator keyPairGenerator;
    private final DB database;

    @Inject
    public MongoMigration(DB database, KeyPairGenerator keyPairGenerator) {
        this.database = database;
        this.keyPairGenerator = keyPairGenerator;
    }

    public void run() {
        Change.setMigration(this);
        Mongobee runner = new Mongobee(database.getMongo());
        runner.setDbName(database.getName());
        runner.setChangeLogsScanPackage(MongoMigration.class.getPackage().getName());
        convertException(() -> runner.execute());
    }

    public DB getDatabase() {
        return database;
    }

    public KeyPairGenerator getKeyPairGenerator() {
        return keyPairGenerator;
    }

}
