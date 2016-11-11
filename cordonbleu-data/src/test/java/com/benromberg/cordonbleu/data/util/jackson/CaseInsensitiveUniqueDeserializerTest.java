package com.benromberg.cordonbleu.data.util.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueDeserializer;
import com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueSerializer;
import com.benromberg.cordonbleu.data.util.jackson.JsonMapper;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class CaseInsensitiveUniqueDeserializerTest {
    private static final String EMAIL = "dummy@EMAIL.com";

    @Test
    public void deserialize_ToString() throws Exception {
        String serializedValue = "{\"email\":{\"unique\":\"" + EMAIL.toLowerCase() + "\",\"value\":\"" + EMAIL + "\"}}";
        DummyClass deserialized = JsonMapper.getInstance().readValue(serializedValue, DummyClass.class);
        assertThat(deserialized.getEmail()).isEqualTo(EMAIL);
    }

    public static class DummyClass {
        @JsonProperty
        @JsonSerialize(using = CaseInsensitiveUniqueSerializer.class)
        @JsonDeserialize(using = CaseInsensitiveUniqueDeserializer.class)
        private String email;

        @JsonCreator
        public DummyClass(String email) {
            this.email = email;
        }

        public String getEmail() {
            return email;
        }
    }
}
