package com.benromberg.cordonbleu.main.resource.commit;

import static java.util.stream.Collectors.toList;

import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.main.permission.UserWithPermissions;
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
    private final boolean onlyAssignedToMe;

    @JsonProperty
    private final Optional<String> lastCommitHash;

    @JsonProperty
    private final Optional<String> fetchedAfterCommitHash;

    @JsonProperty
    private final int limit;

    @JsonCreator
    public CommitListRequest(List<String> repository, List<CommitAuthorRequest> author, List<String> user,
            boolean approved, boolean onlyAssignedToMe, Optional<String> lastCommitHash, Optional<String> fetchedAfterCommitHash, int limit) {
        this.repository = repository;
        this.author = author;
        this.user = user;
        this.approved = approved;
        this.onlyAssignedToMe = onlyAssignedToMe;
        this.lastCommitHash = lastCommitHash;
        this.fetchedAfterCommitHash = fetchedAfterCommitHash;
        this.limit = limit;
    }

    public RawCommitFilter toFilterFor(UserWithPermissions requestUser) {
        Optional<User> assignedTo = requestUser.isKnown() && onlyAssignedToMe ? Optional.of(requestUser.getUser()) : Optional.empty();
        return new RawCommitFilter(author.stream().map(CommitAuthorRequest::toAuthor).collect(toList()), user, approved,
                lastCommitHash, fetchedAfterCommitHash, limit, assignedTo);
    }

    public List<String> getRepositories() {
        return repository;
    }
}
