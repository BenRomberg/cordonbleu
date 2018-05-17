package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.main.resource.team.ReadCommitAuthorResponse;
import com.benromberg.cordonbleu.main.resource.team.ReadUserResponse;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ReadGroupAssignmentResponse {

    @JsonProperty
    private final ReadUserResponse assignee;

    @JsonProperty
    private final ReadCommitAuthorResponse commitAuthor;

    @JsonProperty
    private final List<Commit> commits;

    @JsonCreator
    public ReadGroupAssignmentResponse(ReadUserResponse assignee, ReadCommitAuthorResponse commitAuthor, List<Commit> commits) {
        this.assignee = assignee;
        this.commitAuthor = commitAuthor;
        this.commits = commits;
    }

    public ReadUserResponse getAssignee() {
        return assignee;
    }

    public ReadCommitAuthorResponse  getCommitAuthor() {
        return commitAuthor;
    }

    public List<Commit> getCommits() {
        return commits;
    }
}
