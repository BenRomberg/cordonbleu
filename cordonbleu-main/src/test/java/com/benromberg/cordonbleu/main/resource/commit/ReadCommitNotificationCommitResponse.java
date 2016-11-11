package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.main.resource.team.ReadCommitAuthorResponse;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadCommitNotificationCommitResponse {
    @JsonProperty
    private final String hash;

    @JsonProperty
    private final String teamName;

    @JsonProperty
    private final String message;

    @JsonProperty
    private final ReadCommitAuthorResponse author;

    @JsonProperty
    private final LocalDateTime created;

    @JsonProperty
    private final boolean approved;

    @JsonCreator
    public ReadCommitNotificationCommitResponse(String hash, String teamName, String message,
            ReadCommitAuthorResponse author, LocalDateTime created, boolean approved) {
        this.hash = hash;
        this.teamName = teamName;
        this.message = message;
        this.author = author;
        this.created = created;
        this.approved = approved;
    }

    public String getHash() {
        return hash;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getMessage() {
        return message;
    }

    public ReadCommitAuthorResponse getAuthor() {
        return author;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public boolean isApproved() {
        return approved;
    }

}
