package com.benromberg.cordonbleu.data.util.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CaseInsensitiveUniqueValue {
    public static final String PROPERTY_VALUE = "value";
    public static final String PROPERTY_UNIQUE = "unique";

    @JsonProperty(PROPERTY_UNIQUE)
    private String unique;

    @JsonProperty(PROPERTY_VALUE)
    private String value;

    @JsonCreator
    private CaseInsensitiveUniqueValue(String unique, String value) {
        this.unique = unique;
        this.value = value;
    }

    public CaseInsensitiveUniqueValue(String value) {
        this.unique = uniqueValue(value);
        this.value = value;
    }

    public String getUnique() {
        return unique;
    }

    public String getValue() {
        return value;
    }

    public static String uniqueProperty(String property) {
        return property + "." + PROPERTY_UNIQUE;
    }

    public static String uniqueValue(String value) {
        return value.toLowerCase();
    }
}
