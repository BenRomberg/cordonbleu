package com.benromberg.cordonbleu.data.migration.change0009;

import com.benromberg.cordonbleu.data.util.MongoCommand;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TeamAfter {
    @JsonProperty(MongoCommand.ID_PROPERTY)
    private final String id;

    @JsonProperty
    private final String name;

    @JsonCreator
    public TeamAfter(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
