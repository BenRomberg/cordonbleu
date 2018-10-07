package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.main.resource.team.CommitAuthorResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class CommitListItemResponse {
    private final Commit commit;
    private final CommitAuthorResponse author;
    private final List<String> repositories;

    public CommitListItemResponse(Commit commit) {
        this.commit = commit;
        author = new CommitAuthorResponse(commit.getAuthor());
        repositories = commit.getRepositories().stream().map(repository -> repository.getRepository().getName())
                .collect(toList());
    }

    @JsonProperty
    public CommitAuthorResponse getAuthor() {
        return author;
    }

    @JsonProperty
    public Optional<CommitAssignmentResponse> getAssignment() {
        return commit.getAssignee().map(CommitAssignmentResponse::new);
    }

    @JsonProperty
    public String getHash() {
        return commit.getId().getHash();
    }

    @JsonProperty
    public LocalDateTime getCreated() {
        return commit.getCreated();
    }

    @JsonProperty
    public List<String> getRepositories() {
        return repositories;
    }

    @JsonProperty
    public int getNumComments() {
        return commit.getComments().size();
    }

    @JsonProperty
    public boolean isApproved() {
        return commit.getApproval().isPresent();
    }

    @JsonProperty
    public boolean isRemoved() {
        return commit.isRemoved();
    }
}
