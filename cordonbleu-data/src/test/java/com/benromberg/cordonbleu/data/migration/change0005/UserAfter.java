package com.benromberg.cordonbleu.data.migration.change0005;

import java.util.List;

import com.benromberg.cordonbleu.data.util.MongoCommand;
import com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueDeserializer;
import com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueSerializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class UserAfter {
    @JsonProperty(MongoCommand.ID_PROPERTY)
    private final String id;

    @JsonProperty
    @JsonSerialize(contentUsing = CaseInsensitiveUniqueSerializer.class)
    @JsonDeserialize(contentUsing = CaseInsensitiveUniqueDeserializer.class)
    private final List<String> emailAliases;

    @JsonProperty
    private final String otherField;

    @JsonCreator
    public UserAfter(String id, List<String> emailAliases, String otherField) {
        this.id = id;
        this.emailAliases = emailAliases;
        this.otherField = otherField;
    }

    public String getId() {
        return id;
    }

    public String getOtherField() {
        return otherField;
    }

    public List<String> getEmailAliases() {
        return emailAliases;
    }
}
