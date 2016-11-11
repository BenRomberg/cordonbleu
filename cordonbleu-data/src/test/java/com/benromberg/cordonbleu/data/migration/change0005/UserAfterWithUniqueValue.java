package com.benromberg.cordonbleu.data.migration.change0005;

import java.util.List;

import com.benromberg.cordonbleu.data.util.MongoCommand;
import com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserAfterWithUniqueValue {
    @JsonProperty(MongoCommand.ID_PROPERTY)
    private final String id;

    @JsonProperty
    private final List<CaseInsensitiveUniqueValue> emailAliases;

    @JsonProperty
    private final String otherField;

    @JsonCreator
    public UserAfterWithUniqueValue(String id, List<CaseInsensitiveUniqueValue> emailAliases, String otherField) {
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

    public List<CaseInsensitiveUniqueValue> getEmailAliases() {
        return emailAliases;
    }
}
