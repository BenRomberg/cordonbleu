package com.benromberg.cordonbleu.main.resource.usermanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.data.model.UserFlag;
import com.benromberg.cordonbleu.main.CordonBleuTestRule;
import com.benromberg.cordonbleu.main.resource.team.ReadUserResponse;

import java.util.List;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.main.resource.usermanagement.UpdateFlagRequest;

public class UserManagementResourceTest {
    @Rule
    @ClassRule
    public static final CordonBleuTestRule RULE = new CordonBleuTestRule();

    @Test
    public void getUsers_WithoutBeingLoggedIn_YieldsUnauthorized() throws Exception {
        Response response = RULE.get("/api/admin/user");
        assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void getUsers_WithoutAdminPermission_YieldsForbidden() throws Exception {
        Response response = RULE.withAuthenticatedUser().get("/api/admin/user");
        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void getUsers_WithNoOtherUsers_ReturnsAdminUser() throws Exception {
        Response response = RULE.withAdminUser().get("/api/admin/user");
        assertThat(getUsers(response)).extracting(ReadUserResponse::getEmail).containsExactly(
                CordonBleuTestRule.USER_EMAIL);
    }

    @Test
    public void updateFlag_WithoutBeingLoggedIn_YieldsUnauthorized() throws Exception {
        Response response = RULE.post("/api/admin/user/updateFlag", new UpdateFlagRequest("user-id", UserFlag.ADMIN,
                true));
        assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void updateFlag_WithoutAdminPermission_YieldsForbidden() throws Exception {
        Response response = RULE.withAuthenticatedUser().post("/api/admin/user/updateFlag",
                new UpdateFlagRequest("user-id", UserFlag.ADMIN, true));
        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void updateFlag_OnUnknownUser_ReturnsUnchangedUserList() throws Exception {
        Response response = RULE.withAdminUser().post("/api/admin/user/updateFlag",
                new UpdateFlagRequest("unknown-user-id", UserFlag.INACTIVE, true));
        assertThat(getUsers(response)).extracting(ReadUserResponse::getEmail, ReadUserResponse::isInactive)
                .containsExactly(tuple(CordonBleuTestRule.USER_EMAIL, false));
    }

    @Test
    public void updateFlag_OnAdminUser_ReturnsUpdatedUser() throws Exception {
        User user = RULE.getAuthenticatedUser();
        Response response = RULE.withAdminUser().post("/api/admin/user/updateFlag",
                new UpdateFlagRequest(user.getId(), UserFlag.INACTIVE, true));
        assertThat(getUsers(response)).extracting(ReadUserResponse::getId, ReadUserResponse::isInactive)
                .containsExactly(tuple(user.getId(), true));
    }

    private List<ReadUserResponse> getUsers(Response response) {
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        return response.readEntity(new GenericType<List<ReadUserResponse>>() {
        });
    }
}
