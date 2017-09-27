package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.main.resource.team.ReadCommitAuthorResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadCommitDetailResponse {
    @JsonProperty
    private final ReadCommitAuthorResponse author;

    @JsonProperty
    private final String hash;

    @JsonProperty
    private final LocalDateTime created;

    @JsonProperty
    private final List<ReadCommitRepository> repositories;

    @JsonProperty
    private final String messageAsHtml;

    @JsonProperty
    private final Optional<ReadCommitApprovalResponse> approval;
    
    @JsonProperty
    private final boolean collectiveReview;

    @JsonProperty
    private final List<ReadCommitFileResponse> files;

    @JsonCreator
    public ReadCommitDetailResponse(ReadCommitAuthorResponse author, String hash, LocalDateTime created,
            List<ReadCommitRepository> repositories, String messageAsHtml,
            Optional<ReadCommitApprovalResponse> approval, List<ReadCommitFileResponse> files, boolean collectiveReview) {
        this.author = author;
        this.hash = hash;
        this.created = created;
        this.repositories = repositories;
        this.messageAsHtml = messageAsHtml;
        this.approval = approval;
        this.files = files;
        this.collectiveReview=collectiveReview;
    }

    public ReadCommitAuthorResponse getAuthor() {
        return author;
    }

    public String getHash() {
        return hash;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public List<ReadCommitRepository> getRepositories() {
        return repositories;
    }

    public String getMessageAsHtml() {
        return messageAsHtml;
    }

    public Optional<ReadCommitApprovalResponse> getApproval() {
        return approval;
    }

    public List<ReadCommitFileResponse> getFiles() {
        return files;
    }

	public boolean isCollectiveReview() {
		return collectiveReview;
	}
    
}
