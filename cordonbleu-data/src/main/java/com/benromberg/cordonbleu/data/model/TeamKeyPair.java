package com.benromberg.cordonbleu.data.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TeamKeyPair {
    @JsonProperty
    private final String privateKey;

    @JsonProperty
    private final String publicKey;

    @JsonCreator
    public TeamKeyPair(String privateKey, String publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }
}
