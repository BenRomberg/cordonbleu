package com.benromberg.cordonbleu.main.resource.team;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadFilterResponse {
    @JsonProperty
    private final List<ReadRepositoryFilterResponse> repositories;

    @JsonProperty
    private final List<ReadCommitAuthorResponse> authors;

    @JsonProperty
    private final List<ReadUserFilterResponse> users;

    @JsonCreator
    public ReadFilterResponse(List<ReadRepositoryFilterResponse> repositories, List<ReadCommitAuthorResponse> authors,
            List<ReadUserFilterResponse> users) {
        this.repositories = repositories;
        this.authors = authors;
        this.users = users;
    }

    public List<ReadRepositoryFilterResponse> getRepositories() {
        return repositories;
    }

    public List<ReadCommitAuthorResponse> getAuthors() {
        return authors;
    }

    public List<ReadUserFilterResponse> getUsers() {
        return users;
    }
}
