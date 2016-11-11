package com.benromberg.cordonbleu.main.util.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.Test;

import com.benromberg.cordonbleu.main.util.jackson.CustomModule;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LocalDateTimeDeserializerTest {
    @Test
    public void deserialize() throws Exception {
        ObjectMapper mapper = CustomModule.enhanceMapper(new ObjectMapper());
        LocalDateTime parsedDate = mapper.readValue("{ \"date\": 1386155842883 }", LocalDateTimeContainer.class)
                .getDate();
        assertThat(parsedDate).isEqualTo(LocalDateTime.of(2013, 12, 4, 11, 17, 22, 883_000_000));
    }

    private static class LocalDateTimeContainer {
        private LocalDateTime date;

        private LocalDateTime getDate() {
            return date;
        }

        @SuppressWarnings("unused")
        private void setDate(LocalDateTime date) {
            this.date = date;
        }
    }
}
