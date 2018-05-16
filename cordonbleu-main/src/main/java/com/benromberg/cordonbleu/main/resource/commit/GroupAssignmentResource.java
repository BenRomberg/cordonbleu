package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.main.permission.TeamPermissionGuard;
import com.benromberg.cordonbleu.main.permission.UserWithPermissions;
import com.benromberg.cordonbleu.service.assignment.CommitBatchAssignment;
import com.benromberg.cordonbleu.service.assignment.CommitBatchAssignmentService;
import com.benromberg.cordonbleu.service.commit.CommitService;
import com.benromberg.cordonbleu.service.user.UserService;
import com.codahale.metrics.annotation.Timed;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.dropwizard.auth.Auth;

@Path("/groupAssignment")
@Produces(MediaType.APPLICATION_JSON)
public class GroupAssignmentResource {

    private final CommitService commitService;
    private final UserService userService;
    private final TeamPermissionGuard teamPermissionGuard;
    private final CommitBatchAssignmentService commitBatchAssignmentService;

    @Inject
    public GroupAssignmentResource(CommitService commitService, UserService userService, TeamPermissionGuard teamPermissionGuard,
            CommitBatchAssignmentService commitBatchAssignmentService) {
        this.commitService = commitService;
        this.userService = userService;
        this.teamPermissionGuard = teamPermissionGuard;
        this.commitBatchAssignmentService = commitBatchAssignmentService;
    }

    @POST
    @Timed
    public List<GroupAssignmentResponse> groupAssignment(GroupAssignmentRequest request, @Auth UserWithPermissions user) {
        Team team = teamPermissionGuard.guardGroupAssignment(user, request.getTeamId()).getTeam();
        List<Commit> commitsToAssign = commitService.findRecentCommitsToAssign(team);
        List<User> users = request.getUserIds()
                .stream()
                .distinct()
                .map(userService::findUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (commitsToAssign.isEmpty() || users.isEmpty()) {
            return Collections.emptyList();
        }

        List<CommitBatchAssignment> assignments = commitBatchAssignmentService.generateCommitBatchAssignments(commitsToAssign, users);
        assignments.forEach(batch -> commitService.assignCommitBatch(batch, user.getUser()));
        return assignments.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private GroupAssignmentResponse toResponse(CommitBatchAssignment assignment) {
        return new GroupAssignmentResponse(assignment.getAssignee(), assignment.getCommitAuthor(), assignment.getCommits());
    }
}
