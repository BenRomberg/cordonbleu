package com.benromberg.cordonbleu.main.resource.team;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FilterResponse {
    @JsonProperty
    private final List<RepositoryFilterResponse> repositories;

    @JsonProperty
    private final List<CommitAuthorResponse> authors;

    @JsonProperty
    private final List<UserFilterResponse> users;

    public FilterResponse(List<RepositoryFilterResponse> repositories, List<CommitAuthorResponse> authors,
            List<UserFilterResponse> users) {
        this.repositories = repositories;
        this.authors = authors;
        this.users = users;
    }
}
