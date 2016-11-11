package com.benromberg.cordonbleu.main.resource.usermanagement;

import static java.util.stream.Collectors.toList;
import com.benromberg.cordonbleu.service.user.UserService;
import io.dropwizard.auth.Auth;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.benromberg.cordonbleu.main.permission.UserManagementPermissionGuard;
import com.benromberg.cordonbleu.main.permission.UserWithPermissions;
import com.benromberg.cordonbleu.main.resource.team.UserResponse;
import com.codahale.metrics.annotation.Timed;

@Path("/admin/user")
@Produces(MediaType.APPLICATION_JSON)
public class UserManagementResource {
    private final UserService userService;
    private final UserManagementPermissionGuard userManagementPermissionGuard;

    @Inject
    public UserManagementResource(UserService userService, UserManagementPermissionGuard userManagementPermissionGuard) {
        this.userService = userService;
        this.userManagementPermissionGuard = userManagementPermissionGuard;
    }

    @GET
    @Timed
    public List<UserResponse> getUsers(@Auth UserWithPermissions user) {
        userManagementPermissionGuard.guardManageUsers(user);
        return userService.findAllUsers().stream().map(UserResponse::new).collect(toList());
    }

    @POST
    @Path("/updateFlag")
    @Timed
    public List<UserResponse> updateFlag(@Auth UserWithPermissions user, UpdateFlagRequest request) {
        userManagementPermissionGuard.guardManageUsers(user);
        userService.updateFlag(request.getUserId(), request.getFlag(), request.isFlagValue());
        return getUsers(user);
    }
}
