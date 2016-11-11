package com.benromberg.cordonbleu.main.resource.commit;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadLineSpacerResponse {
    @JsonProperty
    private Optional<Integer> beginIndex;

    @JsonProperty
    private Optional<Integer> endIndex;

    @JsonCreator
    public ReadLineSpacerResponse(Optional<Integer> beginIndex, Optional<Integer> endIndex) {
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
    }

    public Optional<Integer> getBeginIndex() {
        return beginIndex;
    }

    public Optional<Integer> getEndIndex() {
        return endIndex;
    }
}
