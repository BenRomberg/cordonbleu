package com.benromberg.cordonbleu.data.dao;

import java.util.List;
import java.util.Optional;

import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;
import com.benromberg.cordonbleu.data.model.CommitAuthor;
import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.model.User;

public class CommitFilter {
    private final Team team;
    private final List<CodeRepositoryMetadata> repositories;
    private final List<CommitAuthor> authors;
    private final List<User> users;
    private final boolean approved;
    private final boolean collectiveReview;
    private final Optional<String> lastCommitHash;
    private final int limit;

    public CommitFilter(Team team, List<CodeRepositoryMetadata> repositories, List<CommitAuthor> authors,
            List<User> users, boolean approved, Optional<String> lastCommitHash, int limit, boolean collectiveReview) {
        this.team = team;
        this.repositories = repositories;
        this.authors = authors;
        this.users = users;
        this.approved = approved;
        this.lastCommitHash = lastCommitHash;
        this.limit = limit;
        this.collectiveReview=collectiveReview;
    }

    public Team getTeam() {
        return team;
    }

    public List<CodeRepositoryMetadata> getRepositories() {
        return repositories;
    }

    public List<CommitAuthor> getAuthors() {
        return authors;
    }

    public List<User> getUsers() {
        return users;
    }

    public boolean isApproved() {
        return approved;
    }

    public boolean isCollectiveReview() {
		return collectiveReview;
	}

	public Optional<String> getLastCommitHash() {
        return lastCommitHash;
    }

    public int getLimit() {
        return limit;
    }
}