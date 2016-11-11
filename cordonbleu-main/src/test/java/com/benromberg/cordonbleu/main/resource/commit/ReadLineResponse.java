package com.benromberg.cordonbleu.main.resource.commit;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadLineResponse {
    @JsonProperty
    private Optional<ReadLineSpacerResponse> spacer;

    @JsonProperty
    private Optional<ReadCodeLineResponse> line;

    @JsonCreator
    public ReadLineResponse(Optional<ReadLineSpacerResponse> spacer, Optional<ReadCodeLineResponse> line) {
        this.spacer = spacer;
        this.line = line;
    }

    public Optional<ReadLineSpacerResponse> getSpacer() {
        return spacer;
    }

    public Optional<ReadCodeLineResponse> getLine() {
        return line;
    }

}
