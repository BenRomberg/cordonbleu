package com.benromberg.cordonbleu.data.model;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitFilePath {
    @JsonProperty
    private final Optional<String> beforePath;

    @JsonProperty
    private final Optional<String> afterPath;

    @JsonCreator
    public CommitFilePath(Optional<String> beforePath, Optional<String> afterPath) {
        this.beforePath = beforePath;
        this.afterPath = afterPath;
    }

    public Optional<String> getBeforePath() {
        return beforePath;
    }

    public Optional<String> getAfterPath() {
        return afterPath;
    }

    @Override
    public int hashCode() {
        return afterPath.hashCode() + 31 * beforePath.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        CommitFilePath other = (CommitFilePath) obj;
        return afterPath.equals(other.afterPath) && beforePath.equals(other.beforePath);
    }

}
