package com.benromberg.cordonbleu.main.resource.commit;

import static java.util.stream.Collectors.toList;
import com.benromberg.cordonbleu.service.coderepository.RawCommitFilter;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitListRequest {
    @JsonProperty
    private final List<String> repository;

    @JsonProperty
    private final List<CommitAuthorRequest> author;

    @JsonProperty
    private final List<String> user;

    @JsonProperty
    private final boolean approved;

    @JsonProperty
    private final boolean collectiveReviewOnly;
    
    
    @JsonProperty
    private final Optional<String> lastCommitHash;

    @JsonProperty
    private final int limit;

    @JsonCreator
    public CommitListRequest(List<String> repository, List<CommitAuthorRequest> author, List<String> user,
            boolean approved, Optional<String> lastCommitHash, int limit, boolean collectiveReviewOnly) {
        this.repository = repository;
        this.author = author;
        this.user = user;
        this.approved = approved;
        this.lastCommitHash = lastCommitHash;
        this.limit = limit;
        this.collectiveReviewOnly = collectiveReviewOnly;
    }

    public RawCommitFilter toFilter() {
        return new RawCommitFilter(author.stream().map(item -> item.toAuthor()).collect(toList()), user, approved,
                lastCommitHash, limit, collectiveReviewOnly);
    }

    public List<String> getRepositories() {
        return repository;
    }
}
