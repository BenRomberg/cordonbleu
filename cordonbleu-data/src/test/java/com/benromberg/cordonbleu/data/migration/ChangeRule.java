package com.benromberg.cordonbleu.data.migration;

import static com.benromberg.cordonbleu.util.ExceptionUtil.convertException;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import com.benromberg.cordonbleu.data.testutil.DatabaseRule;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mongojack.internal.MongoJackModule;

import com.benromberg.cordonbleu.data.migration.Change;
import com.benromberg.cordonbleu.data.util.jackson.JsonMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mongobee.Mongobee;
import com.mongodb.DBObject;

public class ChangeRule implements TestRule {
    private static final ObjectMapper MAPPER = createJsonMapperForMongo();

    private final DatabaseRule databaseRule = new DatabaseRule();
    private final Class<?> changeClass;

    public ChangeRule(Class<?> changeClass) {
        this.changeClass = changeClass;
    }

    public void runChanges() {
        Change.setMigration(new TestMigration());
        Mongobee runner = new Mongobee(DatabaseRule.getMongoClient());
        runner.setDbName(DatabaseRule.DBNAME);
        runner.setChangeLogsScanPackage(changeClass.getPackage().getName());
        convertException(() -> runner.execute());
    }

    public <T> TestCollection<T> getCollection(String collectionName, Class<T> targetClass) {
        return new TestCollection<T>(DatabaseRule.getDB().getCollection(collectionName),
                MAPPER.constructType(targetClass), MAPPER.constructType(String.class), MAPPER, null, null);
    }

    public boolean collectionExists(String collectionName) {
        return DatabaseRule.getDB().collectionExists(collectionName);
    }

    @SuppressWarnings("unchecked")
    public void assertIndexIsNotPresent(TestCollection<?> collection, String indexKey) {
        assertThat(collection.getIndexInfo()).extracting(dbObject -> ((DBObject) dbObject.get("key")).keySet())
                .doesNotContain(singleton(indexKey));
    }

    private static ObjectMapper createJsonMapperForMongo() {
        ObjectMapper mapper = JsonMapper.wrapInstance(new ObjectMapper());
        MongoJackModule.configure(mapper);
        return mapper;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return databaseRule.apply(base, description);
    }
}
