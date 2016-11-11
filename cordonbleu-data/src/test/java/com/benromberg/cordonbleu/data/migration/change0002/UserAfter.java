package com.benromberg.cordonbleu.data.migration.change0002;

import java.util.List;

import com.benromberg.cordonbleu.data.util.MongoCommand;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserAfter {
    @JsonProperty(MongoCommand.ID_PROPERTY)
    private String id;

    @JsonProperty
    private List<String> emailAliases;

    @JsonProperty
    private String otherField;

    @JsonCreator
    public UserAfter(String id, List<String> emailAliases, String otherField) {
        this.id = id;
        this.emailAliases = emailAliases;
        this.otherField = otherField;
    }

    public String getId() {
        return id;
    }

    public List<String> getEmailAliases() {
        return emailAliases;
    }

    public String getOtherField() {
        return otherField;
    }
}
