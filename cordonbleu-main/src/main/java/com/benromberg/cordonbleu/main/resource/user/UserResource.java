package com.benromberg.cordonbleu.main.resource.user;

import static java.util.stream.Collectors.toList;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.service.user.UserService;
import io.dropwizard.auth.Auth;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.benromberg.cordonbleu.main.permission.UserWithPermissions;
import com.benromberg.cordonbleu.main.resource.team.OwnUserResponse;
import com.benromberg.cordonbleu.main.resource.team.UserResponse;
import com.codahale.metrics.annotation.Timed;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
    private static final int AUTOCOMPLETE_LIMIT = 10;
    private final UserService userService;

    @Inject
    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @GET
    @Timed
    public OwnUserResponse getCurrentUser(@Auth UserWithPermissions user) {
        return new OwnUserResponse(user);
    }

    @POST
    @Timed
    public OwnUserResponse saveCurrentUser(@Auth UserWithPermissions user, UserRequest userRequest) {
        User updatedUser = userService.updateUser(user.getUser(), userRequest.getName(), userRequest.getEmail(),
                userRequest.getEmailAliases()).get();
        return new OwnUserResponse(new UserWithPermissions(updatedUser));
    }

    @POST
    @Path("/login")
    @Timed
    public OwnUserResponse loginUser(LoginRequest loginRequest, @Context HttpServletResponse response) {
        User user = userService.findUserByEmail(loginRequest.getEmail()).get();
        if (!userService.isLoginAllowed(user)) {
            throw new WebApplicationException(Status.FORBIDDEN);
        }
        return startSessionForUser(loginRequest.getPassword(), response, user);
    }

    @POST
    @Path("/register")
    @Timed
    public OwnUserResponse registerUser(RegisterRequest registerRequest, @Context HttpServletResponse response) {
        User user = userService.registerUser(registerRequest.getEmail(), registerRequest.getName(),
                registerRequest.getPassword());
        return startSessionForUser(registerRequest.getPassword(), response, user);
    }

    @GET
    @Timed
    @Path("/autocomplete")
    public List<UserResponse> getAutocompleteUsers(@QueryParam("prefix") String prefix) {
        return userService.findByNamePrefix(prefix, AUTOCOMPLETE_LIMIT).stream().map(UserResponse::new)
                .collect(toList());
    }

    private OwnUserResponse startSessionForUser(String password, HttpServletResponse response, User user) {
        Optional<String> sessionId = userService.startSession(user, password);
        if (!sessionId.isPresent()) {
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }
        response.addCookie(createSessionCookie(sessionId.get()));
        return new OwnUserResponse(new UserWithPermissions(user));
    }

    private Cookie createSessionCookie(String sessionId) {
        Cookie cookie = new Cookie("session", sessionId);
        cookie.setPath("/");
        cookie.setMaxAge((int) userService.getUserSessionExpiration().getSeconds());
        return cookie;
    }
}
