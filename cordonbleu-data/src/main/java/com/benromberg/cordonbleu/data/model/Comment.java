package com.benromberg.cordonbleu.data.model;

import com.benromberg.cordonbleu.util.ClockService;

import java.time.LocalDateTime;

import com.benromberg.cordonbleu.data.util.RandomIdGenerator;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Comment extends Entity<String> {
    public static final String USER_PROPERTY = "user";
    public static final String CREATED_PROPERTY = "created";
    public static final String TEXT_PROPERTY = "text";

    @JsonProperty(CREATED_PROPERTY)
    private final LocalDateTime created;

    @JsonProperty(USER_PROPERTY)
    private final User user;

    @JsonProperty(TEXT_PROPERTY)
    private final String text;

    @JsonProperty
    private final CommitFilePath commitFilePath;

    @JsonProperty
    private final CommitLineNumber commitLineNumber;

    @JsonCreator
    private Comment(String id, @JsonProperty(CREATED_PROPERTY) LocalDateTime created, User user,
            @JsonProperty(TEXT_PROPERTY) String text, CommitFilePath commitFilePath, CommitLineNumber commitLineNumber) {
        super(id);
        this.created = created;
        this.user = user;
        this.text = text;
        this.commitFilePath = commitFilePath;
        this.commitLineNumber = commitLineNumber;
    }

    public Comment(User user, String text, CommitFilePath commitFilePath, CommitLineNumber commitLineNumber) {
        this(RandomIdGenerator.generate(), ClockService.now(), user, text, commitFilePath, commitLineNumber);
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public User getUser() {
        return user;
    }

    public String getText() {
        return text;
    }

    public CommitFilePath getCommitFilePath() {
        return commitFilePath;
    }

    public CommitLineNumber getCommitLineNumber() {
        return commitLineNumber;
    }
}
