package com.benromberg.cordonbleu.service.coderepository;

import com.benromberg.cordonbleu.data.dao.CommitFilter;
import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;
import com.benromberg.cordonbleu.data.model.CommitAuthor;
import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.model.User;

import java.util.List;
import java.util.Optional;

public class RawCommitFilter {
    private final List<CommitAuthor> authors;
    private final boolean approved;
    private final Optional<String> lastCommitHash;
    private final Optional<String> fetchedAfterCommitHash;
    private final int limit;
    private final List<String> userIds;
    private final Optional<User> assignedTo;

    public RawCommitFilter(List<CommitAuthor> authors, List<String> userIds, boolean approved,
            Optional<String> lastCommitHash, Optional<String> fetchedAfterCommitHash, int limit, Optional<User> assignedTo) {
        this.authors = authors;
        this.userIds = userIds;
        this.approved = approved;
        this.lastCommitHash = lastCommitHash;
        this.fetchedAfterCommitHash = fetchedAfterCommitHash;
        this.limit = limit;
        this.assignedTo = assignedTo;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public CommitFilter toCommitFilter(Team team, List<CodeRepositoryMetadata> repositories, List<User> users) {
        return new CommitFilter(team, repositories, authors, users, approved, lastCommitHash, fetchedAfterCommitHash, limit, assignedTo);
    }
}
