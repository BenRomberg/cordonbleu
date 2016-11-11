package com.benromberg.cordonbleu.main.permission;

import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.service.coderepository.CodeRepositoryService;
import com.benromberg.cordonbleu.service.commit.CommitService;
import com.benromberg.cordonbleu.service.commit.RawCommitId;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;

public class CommitPermissionGuard {
    private final CommitService commitService;
    private final CodeRepositoryService repositoryService;

    @Inject
    public CommitPermissionGuard(CommitService commitService, CodeRepositoryService repositoryService) {
        this.commitService = commitService;
        this.repositoryService = repositoryService;
    }

    public List<CodeRepositoryMetadata> guardListCommits(UserWithPermissions user, List<String> repositoryIds) {
        List<CodeRepositoryMetadata> repositories = repositoryService.findRepositories(repositoryIds);
        repositories.forEach(repository -> {
            if (!user.hasTeamPermission(TeamPermission.VIEW, repository.getTeam())) {
                throw new NotFoundException();
            }
        });
        return repositories;
    }

    public Commit guardCommitDetail(UserWithPermissions user, String hash, String teamId) {
        return ensureViewPermission(user, new RawCommitId(hash, teamId));
    }

    public Commit guardApproval(UserWithPermissions user, RawCommitId rawCommitId) {
        Commit commit = ensureViewPermission(user, rawCommitId);
        if (!user.hasTeamPermission(TeamPermission.APPROVE, commit.getId().getTeam())) {
            throw new ForbiddenException();
        }
        return commit;
    }

    private Commit ensureViewPermission(UserWithPermissions user, RawCommitId rawCommitId) {
        Commit commit = commitService.findById(rawCommitId).get();
        if (!user.hasTeamPermission(TeamPermission.VIEW, commit.getId().getTeam())) {
            throw new NotFoundException();
        }
        return commit;
    }
}
