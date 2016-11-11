package com.benromberg.cordonbleu.data.util.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.benromberg.cordonbleu.data.util.jackson.IgnoreSuperClassFields;
import com.benromberg.cordonbleu.data.util.jackson.JsonMapper;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.otherpackage.ClassInOtherPackage;

public class NullPropertyPreventingModuleTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void existingNull_ThrowsException() throws Exception {
        ObjectMapper mapper = JsonMapper.getInstance();
        expectedException.expect(JsonMappingException.class);
        mapper.readValue("{\"field\": null }", DummyClass.class);
    }

    @Test
    public void omittedNull_ThrowsException() throws Exception {
        ObjectMapper mapper = JsonMapper.getInstance();
        expectedException.expect(JsonMappingException.class);
        mapper.readValue("{ }", DummyClass.class);
    }

    @Test
    public void existingNullCollection_ThrowsException() throws Exception {
        ObjectMapper mapper = JsonMapper.getInstance();
        expectedException.expect(JsonMappingException.class);
        mapper.readValue("{\"field\": null }", CollectionClass.class);
    }

    @Test
    public void existingNullCollectionElement_ThrowsException() throws Exception {
        ObjectMapper mapper = JsonMapper.getInstance();
        expectedException.expect(JsonMappingException.class);
        mapper.readValue("{\"field\": [null] }", CollectionClass.class);
    }

    @Test
    public void usingDefaultConstructor_ThrowsException() throws Exception {
        ObjectMapper mapper = JsonMapper.getInstance();
        expectedException.expect(JsonMappingException.class);
        mapper.readValue("{ }", DefaultConstructorClass.class);
    }

    @Test
    public void usingDefaultConstructor_InOtherPackage_IsAllowed() throws Exception {
        ObjectMapper mapper = JsonMapper.getInstance();
        ClassInOtherPackage value = mapper.readValue("{ }", ClassInOtherPackage.class);
        assertThat(value.getField()).isNull();
    }

    @Test
    public void usingFieldOmittingConstructor_ThrowsException() throws Exception {
        ObjectMapper mapper = JsonMapper.getInstance();
        expectedException.expect(JsonMappingException.class);
        mapper.readValue("{\"field\": \"value\" }", FieldOmittingConstructorClass.class);
    }

    @Test
    public void usingSuperClassWithIgnoreSuperClassFieldsAnnotation_IsAllowed() throws Exception {
        ObjectMapper mapper = JsonMapper.getInstance();
        SubClass value = mapper.readValue("{\"subField\": \"value\" }", SubClass.class);
        assertThat(value.getField()).isNull();
    }

    public static class SubClass extends DummyClass {
        @JsonProperty
        private final String subField;

        @JsonCreator
        @IgnoreSuperClassFields
        public SubClass(String subField) {
            super(null);
            this.subField = subField;
        }
    }

    public static class DummyClass {
        @JsonProperty
        private final String field;

        @JsonCreator
        public DummyClass(String field) {
            this.field = field;
        }

        public String getField() {
            return field;
        }
    }

    public static class FieldOmittingConstructorClass {
        @JsonProperty
        private final String field;

        @JsonProperty
        private String fieldOmittingConstructor;

        @JsonCreator
        public FieldOmittingConstructorClass(String field) {
            this.field = field;
        }

        public String getField() {
            return field;
        }
    }

    public static class CollectionClass {
        @JsonProperty
        private final List<String> field;

        @JsonCreator
        public CollectionClass(List<String> field) {
            this.field = field;
        }

        public List<String> getField() {
            return field;
        }
    }

    public static class DefaultConstructorClass {
        @JsonProperty
        private String field;

        public String getField() {
            return field;
        }
    }

}
