package com.benromberg.cordonbleu.data.model;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;

import java.time.Instant;
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
    public static final String ASSIGNEE_PROPERTY = "assignee";
    public static final String COMMENTS_PROPERTY = "comments";
    public static final String REPOSITORIES_PROPERTY = "repositories";
    public static final String CREATED_PROPERTY = "created";
    public static final String FETCHED_AT_PROPERTY = "fetchedAt";

    @JsonProperty(REPOSITORIES_PROPERTY)
    private final List<CommitRepository> repositories;

    @JsonProperty(AUTHOR_PROPERTY)
    private final CommitAuthor author;

    @JsonProperty(ASSIGNEE_PROPERTY)
    private final Optional<User> assignee;

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

    @JsonProperty(FETCHED_AT_PROPERTY)
    private final Optional<LocalDateTime> fetchedAt;

    @JsonCreator
    private Commit(CommitId id, @JsonProperty(REPOSITORIES_PROPERTY) List<CommitRepository> repositories,
            @JsonProperty(AUTHOR_PROPERTY) CommitAuthor author, @JsonProperty(ASSIGNEE_PROPERTY) Optional<User> assignee,
            @JsonProperty(CREATED_PROPERTY) LocalDateTime created, String message,
            @JsonProperty(APPROVAL_PROPERTY) Optional<CommitApproval> approval, @JsonProperty(COMMENTS_PROPERTY) List<Comment> comments,
            @JsonProperty(REMOVED_PROPERTY) boolean removed, @JsonProperty(FETCHED_AT_PROPERTY) Optional<LocalDateTime> fetchedAt) {
        super(id);
        this.repositories = repositories;
        this.author = author;
        this.assignee = assignee;
        this.created = created;
        this.message = message;
        this.approval = approval;
        this.comments = comments;
        this.removed = removed;
        this.fetchedAt = fetchedAt;
    }

    public Commit(CommitId id, List<CommitRepository> repositories, CommitAuthor author, Optional<User> assignee, LocalDateTime created,
            String message, LocalDateTime fetchedAt) {
        this(id, repositories, author, assignee, created, message, empty(), emptyList(), false, Optional.of(fetchedAt));
    }

    public CommitAuthor getAuthor() {
        return author;
    }

    public Optional<User> getAssignee() {
        return assignee;
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

    public Optional<LocalDateTime> getFetchedAt() {
        return fetchedAt;
    }
}
