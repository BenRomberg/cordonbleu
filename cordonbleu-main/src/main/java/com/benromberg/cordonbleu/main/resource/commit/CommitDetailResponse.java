package com.benromberg.cordonbleu.main.resource.commit;

import static java.util.stream.Collectors.toList;
import com.benromberg.cordonbleu.service.commit.HighlightedCommit;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.benromberg.cordonbleu.main.resource.team.CommitAuthorResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitDetailResponse {
    private final HighlightedCommit commit;
    private final List<CommitFileResponse> files;
    private final Optional<CommitApprovalResponse> approval;
    private final boolean collectiveReview;
    private final CommitAuthorResponse author;
    private final List<CommitRepositoryResponse> repositories;

    public CommitDetailResponse(HighlightedCommit commit, List<CommitFileResponse> files) {
        this.commit = commit;
        this.files = files;
        approval = commit.getApproval().map(commitApproval -> new CommitApprovalResponse(commitApproval));
        author = new CommitAuthorResponse(commit.getAuthor());
        repositories = commit.getRepositories().stream().map(CommitRepositoryResponse::new).collect(toList());
        this.collectiveReview = commit.getProposeToCollectiveReview();
    }

    @JsonProperty
    public CommitAuthorResponse getAuthor() {
        return author;
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
    public List<CommitRepositoryResponse> getRepositories() {
        return repositories;
    }

    @JsonProperty
    public String getMessageAsHtml() {
        return commit.getMessage().getHighlightedMessage();
    }

    @JsonProperty
    public Optional<CommitApprovalResponse> getApproval() {
        return approval;
    }

    @JsonProperty
    public List<CommitFileResponse> getFiles() {
        return files;
    }
    
    @JsonProperty
    public boolean getCollectiveReview() {
    	return this.collectiveReview;
    }
}
