package com.benromberg.cordonbleu.main.util.jackson;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class LocalDateTimeConverter {
    public static long toMillis(LocalDateTime dateTime) {
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public static LocalDateTime fromMillis(long millis) {
        return LocalDateTime.ofEpochSecond(millis / 1000, (int) (millis % 1000) * 1000_000, ZoneOffset.UTC);
    }
}
