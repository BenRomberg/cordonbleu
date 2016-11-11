package com.benromberg.cordonbleu.data.migration.change0014;

import com.benromberg.cordonbleu.data.util.MongoCommand;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TeamAfter {
    @JsonProperty(MongoCommand.ID_PROPERTY)
    private final String id;

    @JsonProperty
    private final String otherField;

    @JsonProperty
    private final KeyPairAfter keyPair;

    @JsonCreator
    public TeamAfter(String id, String otherField, KeyPairAfter keyPair) {
        this.id = id;
        this.otherField = otherField;
        this.keyPair = keyPair;
    }

    public String getId() {
        return id;
    }

    public String getOtherField() {
        return otherField;
    }

    public KeyPairAfter getKeyPair() {
        return keyPair;
    }
}
