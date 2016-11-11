package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.data.model.Commit;

import java.time.LocalDateTime;

import com.benromberg.cordonbleu.main.resource.team.CommitAuthorResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitNotificationCommitResponse {
    private final Commit commit;

    public CommitNotificationCommitResponse(Commit commit) {
        this.commit = commit;
    }

    @JsonProperty
    public String getHash() {
        return commit.getId().getHash();
    }

    @JsonProperty
    public String getTeamName() {
        return commit.getId().getTeam().getName();
    }

    @JsonProperty
    public String getMessage() {
        return commit.getMessage();
    }

    @JsonProperty
    public CommitAuthorResponse getAuthor() {
        return new CommitAuthorResponse(commit.getAuthor());
    }

    @JsonProperty
    public LocalDateTime getCreated() {
        return commit.getCreated();
    }

    @JsonProperty
    public boolean isApproved() {
        return commit.getApproval().isPresent();
    }

}
