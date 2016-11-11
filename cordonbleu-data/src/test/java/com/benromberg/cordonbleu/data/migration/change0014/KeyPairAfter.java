package com.benromberg.cordonbleu.data.migration.change0014;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class KeyPairAfter {
    @JsonProperty
    private final String privateKey;

    @JsonProperty
    private final String publicKey;

    @JsonCreator
    public KeyPairAfter(String privateKey, String publicKey) {
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
