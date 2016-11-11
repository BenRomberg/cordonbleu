package com.benromberg.cordonbleu.main.resource.team;

import com.benromberg.cordonbleu.data.model.CommitAuthor;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitAuthorResponse {
    private CommitAuthor author;

    public CommitAuthorResponse(CommitAuthor author) {
        this.author = author;
    }

    @JsonProperty
    public String getName() {
        return author.getName();
    }

    @JsonProperty
    public String getEmail() {
        return author.getEmail();
    }
}
