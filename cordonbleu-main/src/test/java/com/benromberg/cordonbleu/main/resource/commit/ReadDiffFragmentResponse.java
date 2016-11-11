package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.service.diff.DiffStatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadDiffFragmentResponse {
    @JsonProperty
    private DiffStatus status;

    @JsonProperty
    private String text;

    @JsonCreator
    public ReadDiffFragmentResponse(DiffStatus status, String text) {
        this.status = status;
        this.text = text;
    }

    public DiffStatus getStatus() {
        return status;
    }

    public String getText() {
        return text;
    }

}
