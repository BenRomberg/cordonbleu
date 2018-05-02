package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitAuthor;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.main.resource.team.CommitAuthorResponse;
import com.benromberg.cordonbleu.main.resource.team.UserResponse;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GroupAssignmentResponse {
    private final UserResponse assignee;

    private final CommitAuthorResponse commitAuthor;

    private final List<Commit> commits;

    @JsonCreator
    public GroupAssignmentResponse(User assignee, CommitAuthor commitAuthor, List<Commit> commits) {
        this.assignee = new UserResponse(assignee);
        this.commitAuthor = new CommitAuthorResponse(commitAuthor);
        this.commits = commits;
    }

    @JsonProperty
    public UserResponse getAssignee() {
        return assignee;
    }

    @JsonProperty
    public CommitAuthorResponse getCommitAuthor() {
        return commitAuthor;
    }

    @JsonProperty
    public List<Commit> getCommits() {
        return commits;
    }
}
