package com.benromberg.cordonbleu.main.resource.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisterRequest {
    @JsonProperty
    private String email;

    @JsonProperty
    private String password;

    @JsonProperty
    private String name;

    @JsonCreator
    public RegisterRequest(String email, String name, String password) {
        this.email = email;
        this.name = name;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }
}
