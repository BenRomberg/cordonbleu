package com.benromberg.cordonbleu.data.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.Optional;

import javax.persistence.EntityExistsException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import com.benromberg.cordonbleu.data.dao.MongoDao;
import com.benromberg.cordonbleu.data.model.Entity;
import com.benromberg.cordonbleu.data.util.jackson.CustomModule;
import com.benromberg.cordonbleu.data.validation.Validation;
import com.benromberg.cordonbleu.data.validation.ValidationFailedException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class MongoDaoTest {
    private static final String OTHER_VALUE = "other value";
    private static final String OTHER_ID = "other id";
    private static final String DUMMY_VALUE = "value";
    private static final String DUMMY_ID = "dummy id";

    @Rule
    public DaoRule databaseRule = new DaoRule().withDropIndexes();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final DummyValidation validation = new DummyValidation();
    private final MongoDao<String, DummyElement> mongoDao = new MongoDao<>(databaseRule.getDatabaseWithMigration(),
            String.class, DummyElement.class, "dummy", new CustomModule(), validation);

    @Test
    public void enabledValidation_OnInsert_ThrowsException() throws Exception {
        validation.enable();
        assertThatThrownBy(() -> mongoDao.insert(createDummyElement()));
    }

    @Test
    public void enabledValidation_OnInsertIfNotExists_ThrowsException() throws Exception {
        validation.enable();
        assertThatThrownBy(() -> mongoDao.insertIfNotExists(createDummyElement()));
    }

    @Test
    public void insertedElement_CanBeFoundById() throws Exception {
        mongoDao.insert(createDummyElement());
        Optional<DummyElement> foundElement = mongoDao.findById(DUMMY_ID);
        assertThat(foundElement.isPresent()).isTrue();
        assertThat(foundElement.get().getValue()).isEqualTo(DUMMY_VALUE);
    }

    @Test
    public void insertSameElementTwice_ThrowsEntityExistsException() throws Exception {
        mongoDao.insert(createDummyElement());
        thrown.expect(EntityExistsException.class);
        mongoDao.insert(createDummyElement());
    }

    @Test
    public void insertIfNotExists_CanBeFoundById() throws Exception {
        mongoDao.insertIfNotExists(createDummyElement());
        Optional<DummyElement> foundElement = mongoDao.findById(DUMMY_ID);
        assertThat(foundElement.isPresent()).isTrue();
        assertThat(foundElement.get().getValue()).isEqualTo(DUMMY_VALUE);
    }

    @Test
    public void insertIfNotExistsTwice_UpdatesDocument() throws Exception {
        mongoDao.insertIfNotExists(createDummyElement());
        mongoDao.insertIfNotExists(new DummyElement(DUMMY_ID, OTHER_VALUE));
        Optional<DummyElement> foundElement = mongoDao.findById(DUMMY_ID);
        assertThat(foundElement.get().getValue()).isEqualTo(OTHER_VALUE);
    }

    @Test
    public void updateById_ToSameValue_ThrowsEntityExistsException() throws Exception {
        mongoDao.createUniqueIndex(DummyElement.VALUE_PROPERTY);
        mongoDao.insert(createDummyElement());
        mongoDao.insert(new DummyElement(OTHER_ID, OTHER_VALUE));
        thrown.expect(EntityExistsException.class);
        mongoDao.update(OTHER_ID, DBUpdate.set(DummyElement.VALUE_PROPERTY, DUMMY_VALUE));
    }

    @Test
    public void updateByFind_ToSameValue_ThrowsEntityExistsException() throws Exception {
        mongoDao.createUniqueIndex(DummyElement.VALUE_PROPERTY);
        mongoDao.insert(createDummyElement());
        mongoDao.insert(new DummyElement(OTHER_ID, OTHER_VALUE));
        thrown.expect(EntityExistsException.class);
        mongoDao.update(DBQuery.is("_id", OTHER_ID), DBUpdate.set(DummyElement.VALUE_PROPERTY, DUMMY_VALUE));
    }

    @Test
    public void update_WithoutEntity_ReturnsEmptyOptional() throws Exception {
        Optional<DummyElement> updatedElement = mongoDao.update("non-existing id",
                DBUpdate.set(DummyElement.VALUE_PROPERTY, DUMMY_VALUE));
        assertThat(updatedElement.isPresent()).isFalse();
    }

    @Test
    public void update_WithoutEntity_DoesNotUpsert() throws Exception {
        mongoDao.update("non-existing id", DBUpdate.set(DummyElement.VALUE_PROPERTY, DUMMY_VALUE));
        assertThat(mongoDao.count()).isZero();
    }

    @Test
    public void updateMulti_WithMultipleMatchedElements_UpdatesAllOfThem() throws Exception {
        mongoDao.insert(createDummyElement());
        mongoDao.insert(new DummyElement(OTHER_ID, DUMMY_VALUE));
        mongoDao.updateMulti(DBQuery.empty(), DBUpdate.set(DummyElement.VALUE_PROPERTY, OTHER_VALUE));
        assertThat(mongoDao.find().toArray()).extracting(DummyElement::getValue).containsExactly(OTHER_VALUE,
                OTHER_VALUE);
    }

    @Test
    public void updateMulti_ToSameValue_ThrowsDuplicateKeyException() throws Exception {
        mongoDao.createUniqueIndex(DummyElement.VALUE_PROPERTY);
        mongoDao.insert(createDummyElement());
        mongoDao.insert(new DummyElement(OTHER_ID, OTHER_VALUE));
        thrown.expect(EntityExistsException.class);
        mongoDao.updateMulti(DBQuery.is(DummyElement.VALUE_PROPERTY, OTHER_VALUE),
                DBUpdate.set(DummyElement.VALUE_PROPERTY, DUMMY_VALUE));
    }

    @Test
    public void deleteWithValidId_ReturnsTrue() throws Exception {
        mongoDao.insert(createDummyElement());

        boolean result = mongoDao.remove(DUMMY_ID);

        assertThat(result).isTrue();
        assertThat(mongoDao.count()).isZero();
    }

    @Test
    public void deleteWithInvalidId_ReturnsFalse() throws Exception {
        boolean result = mongoDao.remove(DUMMY_ID);

        assertThat(result).isFalse();
    }

    private static class DummyValidation implements Validation<DummyElement> {
        private boolean enabled;

        @Override
        public void validateEntity(DummyElement entity) {
            if (enabled) {
                throw new ValidationFailedException("");
            }
        }

        public void enable() {
            enabled = true;
        }
    }

    private DummyElement createDummyElement() {
        return new DummyElement(DUMMY_ID, DUMMY_VALUE);
    }

    public static class DummyElement extends Entity<String> {
        public static final String VALUE_PROPERTY = "value";

        @JsonProperty(VALUE_PROPERTY)
        private String value;

        @JsonCreator
        public DummyElement(@JsonProperty("_id") String id, @JsonProperty(VALUE_PROPERTY) String value) {
            super(id);
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @JsonSerialize(using = DummyId.Serializer.class)
    public static class DummyId {
        private String id;

        public DummyId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public static class Serializer extends JsonSerializer<DummyId> {

            @Override
            public void serialize(DummyId dummyKey, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                    throws IOException {
                jsonGenerator.writeString(dummyKey.getId());
            }
        }
    }
}