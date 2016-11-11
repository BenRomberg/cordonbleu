package com.benromberg.cordonbleu.data.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitHighlightCacheFile {
    @JsonProperty
    private final List<String> beforeContentHighlighted;

    @JsonProperty
    private final List<String> afterContentHighlighted;

    @JsonCreator
    public CommitHighlightCacheFile(List<String> beforeContentHighlighted, List<String> afterContentHighlighted) {
        this.beforeContentHighlighted = beforeContentHighlighted;
        this.afterContentHighlighted = afterContentHighlighted;
    }

    public List<String> getContentHighlightedBefore() {
        return beforeContentHighlighted;
    }

    public List<String> getContentHighlightedAfter() {
        return afterContentHighlighted;
    }

}
