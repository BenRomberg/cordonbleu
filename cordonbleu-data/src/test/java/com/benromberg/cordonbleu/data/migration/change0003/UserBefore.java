package com.benromberg.cordonbleu.data.migration.change0003;

import java.util.Optional;

import com.benromberg.cordonbleu.data.util.MongoCommand;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserBefore {
    @JsonProperty(MongoCommand.ID_PROPERTY)
    private String id;

    @JsonProperty
    private Optional<String> name;

    @JsonProperty
    private String email;

    @JsonProperty
    private String otherField;

    @JsonCreator
    public UserBefore(String id, Optional<String> name, String email, String otherField) {
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

    public Optional<String> getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

}
