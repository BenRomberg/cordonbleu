package com.benromberg.cordonbleu.data.util.jackson;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import org.mongojack.internal.DateDeserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    private DateDeserializer dateDeserializer = new DateDeserializer();

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        Date date = dateDeserializer.deserialize(parser, context);
        return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
    }
}
