package com.otherpackage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClassInOtherPackage {
    @JsonProperty
    private String field;

    public String getField() {
        return field;
    }
}
