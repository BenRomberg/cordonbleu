package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.service.diff.SpacerCodeLine;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LineSpacerResponse {
    private SpacerCodeLine spacer;

    public LineSpacerResponse(SpacerCodeLine spacer) {
        this.spacer = spacer;
    }

    @JsonProperty
    public Optional<Integer> getBeginIndex() {
        return spacer.getBeginIndex();
    }

    @JsonProperty
    public Optional<Integer> getEndIndex() {
        return spacer.getEndIndex();
    }
}
