package com.benromberg.cordonbleu.data.util;


public class KeyPair {
    private final String privateKey;
    private final String publicKey;

    public KeyPair(String privateKey, String publicKey) {
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
