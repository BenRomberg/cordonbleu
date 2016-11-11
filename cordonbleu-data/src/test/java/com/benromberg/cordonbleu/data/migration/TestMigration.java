package com.benromberg.cordonbleu.data.migration;

import com.benromberg.cordonbleu.data.migration.MongoMigration;
import com.benromberg.cordonbleu.data.util.KeyPair;

import com.benromberg.cordonbleu.data.testutil.DatabaseRule;

public class TestMigration extends MongoMigration {
    public static final String PUBLIC_KEY = "public-key";
    public static final String PRIVATE_KEY = "private-key";

    public TestMigration() {
        super(DatabaseRule.getDB(), () -> new KeyPair(PRIVATE_KEY, PUBLIC_KEY));
    }
}
