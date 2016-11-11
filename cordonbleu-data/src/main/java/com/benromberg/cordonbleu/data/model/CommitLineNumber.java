package com.benromberg.cordonbleu.data.model;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitLineNumber {
    @JsonProperty
    private final Optional<Integer> beforeLineNumber;

    @JsonProperty
    private final Optional<Integer> afterLineNumber;

    @JsonCreator
    public CommitLineNumber(Optional<Integer> beforeLineNumber, Optional<Integer> afterLineNumber) {
        this.beforeLineNumber = beforeLineNumber;
        this.afterLineNumber = afterLineNumber;
    }

    public Optional<Integer> getBeforeLineNumber() {
        return beforeLineNumber;
    }

    public Optional<Integer> getAfterLineNumber() {
        return afterLineNumber;
    }

    @Override
    public int hashCode() {
        return afterLineNumber.hashCode() + 31 * beforeLineNumber.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        CommitLineNumber other = (CommitLineNumber) obj;
        return afterLineNumber.equals(other.afterLineNumber) && beforeLineNumber.equals(other.beforeLineNumber);
    }
}
