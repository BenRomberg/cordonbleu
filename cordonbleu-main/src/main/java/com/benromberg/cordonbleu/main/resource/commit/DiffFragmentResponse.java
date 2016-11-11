package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.service.diff.DiffFragment;
import com.benromberg.cordonbleu.service.diff.DiffStatus;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DiffFragmentResponse {
    private DiffFragment fragment;

    public DiffFragmentResponse(DiffFragment fragment) {
        this.fragment = fragment;
    }

    @JsonProperty
    public DiffStatus getStatus() {
        return fragment.getStatus();
    }

    @JsonProperty
    public String getText() {
        return fragment.getText();
    }

}
