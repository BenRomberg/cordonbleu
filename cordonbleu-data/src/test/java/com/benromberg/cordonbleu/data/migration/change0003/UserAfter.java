package com.benromberg.cordonbleu.data.migration.change0003;

import com.benromberg.cordonbleu.data.util.MongoCommand;
import com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserAfter {
    @JsonProperty(MongoCommand.ID_PROPERTY)
    private String id;

    @JsonProperty
    private CaseInsensitiveUniqueValue name;

    @JsonProperty
    private String otherField;

    private CaseInsensitiveUniqueValue email;

    @JsonCreator
    public UserAfter(String id, CaseInsensitiveUniqueValue name, CaseInsensitiveUniqueValue email, String otherField) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.otherField = otherField;
    }

    public String getId() {
        return id;
    }

    public String getOtherField() {
        return otherField;
    }

    public CaseInsensitiveUniqueValue getName() {
        return name;
    }

    public CaseInsensitiveUniqueValue getEmail() {
        return email;
    }
}
