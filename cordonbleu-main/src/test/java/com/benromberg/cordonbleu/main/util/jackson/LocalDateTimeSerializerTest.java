package com.benromberg.cordonbleu.main.util.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.Test;

import com.benromberg.cordonbleu.main.util.jackson.CustomModule;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LocalDateTimeSerializerTest {
    @Test
    public void serialize() throws Exception {
        ObjectMapper mapper = CustomModule.enhanceMapper(new ObjectMapper());
        String writtenDate = new String(
                mapper.writeValueAsBytes(LocalDateTime.of(2013, 12, 4, 11, 17, 22, 883_000_000)));
        assertThat(writtenDate).isEqualTo("1386155842883");
    }
}
