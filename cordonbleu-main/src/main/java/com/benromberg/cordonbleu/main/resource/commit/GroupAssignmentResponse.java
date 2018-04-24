package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitAuthor;
import com.benromberg.cordonbleu.data.model.User;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GroupAssignmentResponse {
    private final User assignee;

    private final CommitAuthor commitAuthor;

    private final List<Commit> commits;

    @JsonCreator
    public GroupAssignmentResponse(User assignee, CommitAuthor commitAuthor, List<Commit> commits) {
        this.assignee = assignee;
        this.commitAuthor = commitAuthor;
        this.commits = commits;
    }

    @JsonProperty
    public User getAssignee() {
        return assignee;
    }

    @JsonProperty
    public CommitAuthor getCommitAuthor() {
        return commitAuthor;
    }

    @JsonProperty
    public List<Commit> getCommits() {
        return commits;
    }
}
