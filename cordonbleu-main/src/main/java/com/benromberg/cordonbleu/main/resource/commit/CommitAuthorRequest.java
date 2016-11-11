package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.data.model.CommitAuthor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitAuthorRequest {
    @JsonProperty
    private String name;

    @JsonProperty
    private String email;

    @JsonCreator
    public CommitAuthorRequest(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public CommitAuthor toAuthor() {
        return new CommitAuthor(name, email);
    }
}
