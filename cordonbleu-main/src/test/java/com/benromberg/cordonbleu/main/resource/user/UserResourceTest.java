package com.benromberg.cordonbleu.main.resource.user;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.data.model.UserFixture;
import com.benromberg.cordonbleu.data.model.UserFlag;
import com.benromberg.cordonbleu.main.CordonBleuTestRule;
import com.benromberg.cordonbleu.main.resource.team.ReadOwnUserResponse;
import com.benromberg.cordonbleu.main.resource.team.ReadUserResponse;
import com.benromberg.cordonbleu.main.resource.team.ReadUserTeamResponse;
import com.benromberg.cordonbleu.service.user.UserService;

import java.util.HashSet;
import java.util.List;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.main.permission.TeamPermission;
import com.benromberg.cordonbleu.main.resource.user.LoginRequest;
import com.benromberg.cordonbleu.main.resource.user.RegisterRequest;
import com.benromberg.cordonbleu.main.resource.user.UserRequest;

public class UserResourceTest implements UserFixture {
    private static final String USER_NAME = "userName";
    private static final String NEW_EMAIL_ALIAS = "new@alias.com";
    private static final String NEW_EMAIL = "new@email.com";
    private static final String NEW_NAME = "newName";
    private static final String USER_PASSWORD = "user password";
    private static final String USER_EMAIL = "user@email.com";

    @Rule
    @ClassRule
    public static final CordonBleuTestRule RULE = new CordonBleuTestRule().withTeam();

    private final UserService userService = RULE.getInstance(UserService.class);

    @Test
    public void getUser_WithoutCookie_YieldsUnauthorized() throws Exception {
        Response response = RULE.get("/api/user");
        assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void getUser_WithValidSessionCookie_ReturnsUser() throws Exception {
        User user = RULE.getAuthenticatedUser();
        Response response = RULE.withAuthenticatedUser().get("/api/user");
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        ReadOwnUserResponse userResponse = response.readEntity(ReadOwnUserResponse.class);
        assertThat(userResponse.getId()).isEqualTo(user.getId());
        assertThat(userResponse.getEmail()).isEqualTo(CordonBleuTestRule.USER_EMAIL);
        assertThat(userResponse.getGlobalPermissions()).isEmpty();
    }

    @Test
    public void getUser_WithTeam_ReturnsUserWithTeam() throws Exception {
        Response response = RULE.withTeamUser().get("/api/user");
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        ReadOwnUserResponse userResponse = response.readEntity(ReadOwnUserResponse.class);
        assertThat(userResponse.getTeams()).extracting(ReadUserTeamResponse::getId, ReadUserTeamResponse::getName,
                ReadUserTeamResponse::getPermissions).containsExactly(
                tuple(TEAM_ID, TEAM_NAME,
                        new HashSet<>(asList(TeamPermission.COMMENT, TeamPermission.APPROVE, TeamPermission.VIEW))));
    }

    @Test
    public void getUser_WithInvalidSessionCookie_YieldsUnauthorized() throws Exception {
        Response response = RULE.request().header("Cookie", CordonBleuTestRule.SESSION_COOKIE_NAME + "=fakesessionid")
                .get("/api/user");
        assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void saveUser_WithoutCookie_YieldsUnauthorized() throws Exception {
        Response response = RULE.post("/api/user", new UserRequest(NEW_NAME, NEW_EMAIL, asList(NEW_EMAIL_ALIAS)));
        assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void saveUser_WithEmptyEmail_YieldsBadRequest() throws Exception {
        Response response = RULE.withAuthenticatedUser().post("/api/user",
                new UserRequest(NEW_NAME, "", asList(NEW_EMAIL_ALIAS)));
        assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void saveUser_WithDuplicateEmail_YieldsConflict() throws Exception {
        userService.registerUser(NEW_EMAIL, USER_NAME, USER_PASSWORD);
        Response response = RULE.withAuthenticatedUser().post("/api/user",
                new UserRequest(NEW_NAME, NEW_EMAIL, asList(NEW_EMAIL_ALIAS)));
        assertThat(response.getStatus()).isEqualTo(Status.CONFLICT.getStatusCode());
    }

    @Test
    public void saveUser_WithEmptyStringName_YieldsBadRequest() throws Exception {
        Response response = RULE.withAuthenticatedUser().post("/api/user",
                new UserRequest("", NEW_EMAIL, asList(NEW_EMAIL_ALIAS)));
        assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void saveUser_WithEmptyEmailAlias_YieldsBadRequest() throws Exception {
        Response response = RULE.withAuthenticatedUser().post("/api/user",
                new UserRequest(NEW_NAME, NEW_EMAIL, asList("")));
        assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void saveUser_WithValidSessionCookie_ReturnsUpdatedUser() throws Exception {
        User user = RULE.getAuthenticatedUser();
        Response response = RULE.withAuthenticatedUser().post("/api/user",
                new UserRequest(NEW_NAME, NEW_EMAIL, asList(NEW_EMAIL_ALIAS)));
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        ReadOwnUserResponse userResponse = response.readEntity(ReadOwnUserResponse.class);
        assertThat(userResponse.getId()).isEqualTo(user.getId());
        assertThat(userResponse.getName()).isEqualTo(NEW_NAME);
        assertThat(userResponse.getEmail()).isEqualTo(NEW_EMAIL);
        assertThat(userResponse.getEmailAliases()).containsExactly(NEW_EMAIL_ALIAS);
    }

    @Test
    public void login_WithNonExistingUser_YieldsNotFound() throws Exception {
        LoginRequest loginRequest = createLoginRequest();
        Response response = RULE.post("/api/user/login", loginRequest);
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void login_WithWrongPassword_YieldsUnauthorized() throws Exception {
        userService.registerUser(USER_EMAIL, USER_NAME, USER_PASSWORD);
        Response response = RULE.post("/api/user/login", new LoginRequest(USER_EMAIL, "wrong password"));
        assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void login_WithInactiveUser_YieldsForbidden() throws Exception {
        User user = userService.registerUser(USER_EMAIL, USER_NAME, USER_PASSWORD);
        userService.updateFlag(user.getId(), UserFlag.INACTIVE, true);
        Response response = RULE.post("/api/user/login", createLoginRequest());
        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void login_WithInactiveAdminUser_Succeeds() throws Exception {
        User user = userService.registerUser(USER_EMAIL, USER_NAME, USER_PASSWORD);
        userService.updateFlag(user.getId(), UserFlag.INACTIVE, true);
        userService.updateFlag(user.getId(), UserFlag.ADMIN, true);
        Response response = RULE.post("/api/user/login", createLoginRequest());
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    }

    @Test
    public void login_WithCorrectPassword_ReturnsUser() throws Exception {
        User user = userService.registerUser(USER_EMAIL, USER_NAME, USER_PASSWORD);
        Response response = RULE.post("/api/user/login", createLoginRequest());
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        ReadOwnUserResponse userResponse = response.readEntity(ReadOwnUserResponse.class);
        assertThat(userResponse.getId()).isEqualTo(user.getId());
        assertThat(userResponse.getEmail()).isEqualTo(USER_EMAIL);
        assertThat(userResponse.getName()).isEqualTo(USER_NAME);
    }

    @Test
    public void login_WithCorrectPassword_SetsCookie() throws Exception {
        userService.registerUser(USER_EMAIL, USER_NAME, USER_PASSWORD);
        Response response = RULE.post("/api/user/login", createLoginRequest());
        assertThat(response.getCookies().get(CordonBleuTestRule.SESSION_COOKIE_NAME).getValue()).isNotEmpty();
    }

    @Test
    public void register_WithExistingEmail_YieldsConflict() throws Exception {
        userService.registerUser(USER_EMAIL, USER_NAME, USER_PASSWORD);
        Response response = RULE.post("/api/user/register", createRegisterRequest());
        assertThat(response.getStatus()).isEqualTo(Status.CONFLICT.getStatusCode());
    }

    @Test
    public void register_WithNonExistingUser_ReturnsUser() throws Exception {
        Response response = RULE.post("/api/user/register", createRegisterRequest());
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        ReadOwnUserResponse userResponse = response.readEntity(ReadOwnUserResponse.class);
        assertThat(userResponse.getId()).isNotEmpty();
        assertThat(userResponse.getEmail()).isEqualTo(USER_EMAIL);
        assertThat(userResponse.getName()).isEqualTo(USER_NAME);
        assertThat(userResponse.getEmailAliases()).isEmpty();
    }

    @Test
    public void register_WithNonExistingUser_SetsCookie() throws Exception {
        Response response = RULE.post("/api/user/register", createRegisterRequest());
        assertThat(response.getCookies().get(CordonBleuTestRule.SESSION_COOKIE_NAME).getValue()).isNotEmpty();
    }

    @Test
    public void register_WithEmptyEmail_YieldsBadRequest() throws Exception {
        Response response = RULE.post("/api/user/register", new RegisterRequest("", USER_NAME, USER_PASSWORD));
        assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void register_WithShortPassword_YieldsBadRequest() throws Exception {
        Response response = RULE.post("/api/user/register", new RegisterRequest(USER_EMAIL, USER_NAME, "short"));
        assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void autocomplete_WithNoUser_ReturnsEmptyList() throws Exception {
        Response response = RULE.request().param("prefix", "u").get("/api/user/autocomplete");
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        List<ReadUserResponse> users = response.readEntity(new GenericType<List<ReadUserResponse>>() {
        });
        assertThat(users).isEmpty();
    }

    @Test
    public void autocomplete_WithUser_ReturnsUser() throws Exception {
        userService.registerUser(USER_EMAIL, USER_NAME, USER_PASSWORD);
        Response response = RULE.request().param("prefix", "u").get("/api/user/autocomplete");
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        List<ReadUserResponse> users = response.readEntity(new GenericType<List<ReadUserResponse>>() {
        });
        assertThat(users).extracting(ReadUserResponse::getName).containsExactly(USER_NAME);
    }

    @Test
    public void autocomplete_WithUser_ButWrongPrefix_ReturnsEmptyList() throws Exception {
        userService.registerUser(USER_EMAIL, USER_NAME, USER_PASSWORD);
        Response response = RULE.request().param("prefix", "x").get("/api/user/autocomplete");
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        List<ReadUserResponse> users = response.readEntity(new GenericType<List<ReadUserResponse>>() {
        });
        assertThat(users).isEmpty();
    }

    private LoginRequest createLoginRequest() {
        return new LoginRequest(USER_EMAIL, USER_PASSWORD);
    }

    private RegisterRequest createRegisterRequest() {
        return new RegisterRequest(USER_EMAIL, USER_NAME, USER_PASSWORD);
    }
}
