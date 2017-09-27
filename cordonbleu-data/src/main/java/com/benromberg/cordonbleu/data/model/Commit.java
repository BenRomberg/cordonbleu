package com.benromberg.cordonbleu.data.model;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Commit extends Entity<CommitId> {
    public static final String REMOVED_PROPERTY = "removed";
    public static final String APPROVAL_PROPERTY = "approval";
    public static final String AUTHOR_PROPERTY = "author";
    public static final String COMMENTS_PROPERTY = "comments";
    public static final String REPOSITORIES_PROPERTY = "repositories";
    public static final String CREATED_PROPERTY = "created";
    public static final String COLLECTIVE_REVIEW = "proposeToCollectiveReview";

    @JsonProperty(REPOSITORIES_PROPERTY)
    private final List<CommitRepository> repositories;

    @JsonProperty(AUTHOR_PROPERTY)
    private final CommitAuthor author;

    @JsonProperty(CREATED_PROPERTY)
    private final LocalDateTime created;

    @JsonProperty
    private final String message;

    @JsonProperty(APPROVAL_PROPERTY)
    private final Optional<CommitApproval> approval;

    @JsonProperty(COMMENTS_PROPERTY)
    private final List<Comment> comments;

    @JsonProperty(REMOVED_PROPERTY)
    private final boolean removed;

    @JsonProperty(COLLECTIVE_REVIEW)
    private final boolean proposeToCollectiveReview;
    
    @JsonCreator
    private Commit(CommitId id, @JsonProperty(REPOSITORIES_PROPERTY) List<CommitRepository> repositories,
            @JsonProperty(AUTHOR_PROPERTY) CommitAuthor author, @JsonProperty(CREATED_PROPERTY) LocalDateTime created,
            String message, @JsonProperty(APPROVAL_PROPERTY) Optional<CommitApproval> approval,
            @JsonProperty(COMMENTS_PROPERTY) List<Comment> comments, @JsonProperty(REMOVED_PROPERTY) boolean removed, 
            @JsonProperty(COLLECTIVE_REVIEW) boolean proposeToCollectiveReview) {
        super(id);
        this.repositories = repositories;
        this.author = author;
        this.created = created;
        this.message = message;
        this.approval = approval;
        this.comments = comments;
        this.removed = removed;
        this.proposeToCollectiveReview = proposeToCollectiveReview;
    }

    public Commit(CommitId id, List<CommitRepository> repositories, CommitAuthor author, LocalDateTime created,
            String message) {
        this(id, repositories, author, created, message, empty(), emptyList(), false, false);
    }

    public CommitAuthor getAuthor() {
        return author;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public String getMessage() {
        return message;
    }

    public List<CommitRepository> getRepositories() {
        return repositories;
    }

    public Optional<CommitApproval> getApproval() {
        return approval;
    }

    public List<Comment> getComments() {
        return Collections.unmodifiableList(comments);
    }

    public boolean isRemoved() {
        return removed;
    }

	public boolean getProposeToCollectiveReview() {
		return proposeToCollectiveReview;
	}
    
    
}
