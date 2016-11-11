package com.benromberg.cordonbleu.main.util.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.Test;

import com.benromberg.cordonbleu.main.util.jackson.LocalDateTimeConverter;

public class LocalDateTimeConverterTest {
    private static final long MILLIS_VALUE = 1386155842883L;
    private static final LocalDateTime LOCAL_DATE_TIME_VALUE = LocalDateTime.of(2013, 12, 4, 11, 17, 22, 883_000_000);

    @Test
    public void toMillis() throws Exception {
        assertThat(LocalDateTimeConverter.toMillis(LOCAL_DATE_TIME_VALUE)).isEqualTo(MILLIS_VALUE);
    }

    @Test
    public void fromMillis() throws Exception {
        assertThat(LocalDateTimeConverter.fromMillis(MILLIS_VALUE)).isEqualTo(LOCAL_DATE_TIME_VALUE);
    }
}
