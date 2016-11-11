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

public class CaseInsensitiveUniqueSerializerTest {
    private static final String EMAIL = "dummy@EMAIL.com";

    @Test
    public void serialize_AsCaseInsensitiveUniqueValue() throws Exception {
        DummyClass entity = new DummyClass(EMAIL);
        String serializedValue = JsonMapper.getInstance().writeValueAsString(entity);
        assertThat(serializedValue).isEqualTo(
                "{\"email\":{\"unique\":\"" + EMAIL.toLowerCase() + "\",\"value\":\"" + EMAIL + "\"}}");
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
    }
}
