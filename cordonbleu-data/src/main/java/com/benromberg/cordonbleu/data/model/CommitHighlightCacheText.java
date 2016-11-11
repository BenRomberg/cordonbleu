package com.benromberg.cordonbleu.data.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitHighlightCacheText {
    @JsonProperty
    private final String text;

    @JsonProperty
    private final List<User> userReferences;

    @JsonCreator
    public CommitHighlightCacheText(String text, List<User> userReferences) {
        this.text = text;
        this.userReferences = userReferences;
    }

    public List<User> getUserReferences() {
        return userReferences;
    }

    public String getText() {
        return text;
    }
}
