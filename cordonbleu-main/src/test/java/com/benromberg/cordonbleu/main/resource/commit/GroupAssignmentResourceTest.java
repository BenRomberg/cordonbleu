package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.data.dao.CommitDao;
import com.benromberg.cordonbleu.data.dao.UserDao;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitAuthor;
import com.benromberg.cordonbleu.data.model.CommitFixture;
import com.benromberg.cordonbleu.data.model.UserFixture;
import com.benromberg.cordonbleu.main.CordonBleuTestRule;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class GroupAssignmentResourceTest implements CommitFixture, UserFixture {

    @Rule
    @ClassRule
    public static final CordonBleuTestRule RULE = new CordonBleuTestRule().withRepository().withCommentUser();

    private final UserDao userDao = RULE.getInstance(UserDao.class);
    private final CommitDao commitDao = RULE.getInstance(CommitDao.class);

    private static final GroupAssignmentRequest EMPTY_GROUP_ASSIGNMENT = new GroupAssignmentRequest(TEAM_ID, Collections.emptyList());
    private static final GroupAssignmentRequest ASSIGNMENT_WITH_ONE_USER = new GroupAssignmentRequest(TEAM_ID,
            Collections.singletonList(USER.getId()));

    private static final CommitAuthor OTHER_COMMIT_AUTHOR = new CommitAuthor("other-name", "other@mail.com");

    private static final Commit OTHER_COMMIT_1 = new CommitBuilder().id("other-commit-1")
            .author(OTHER_COMMIT_AUTHOR)
            .created(LocalDateTime.now())
            .build();
    private static final Commit OTHER_COMMIT_2 = new CommitBuilder().id("other-commit-2")
            .author(OTHER_COMMIT_AUTHOR)
            .created(LocalDateTime.now())
            .build();
    private static final Commit COMMIT_TO_OLD = new CommitBuilder().id("old-commit").created(LocalDateTime.now().minusDays(16)).build();

    @Test
    public void groupAssignment_WithNoAuthenticatedUser_IsUnauthorized() {
        Response response = RULE.post("/api/groupAssignment", EMPTY_GROUP_ASSIGNMENT);
        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void groupAssignment_WithNonTeamUser_IsForbidden() {
        Response response = RULE.withAuthenticatedUser().post("/api/groupAssignment", EMPTY_GROUP_ASSIGNMENT);
        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void groupAssignment_WithNonExistingUserId_ReturnsEmptyList() {
        Response response = RULE.withTeamUser().post("/api/groupAssignment", ASSIGNMENT_WITH_ONE_USER);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(readResponse(response)).isEmpty();
    }

    @Test
    public void groupAssignment_WithEmptyUserList_ReturnsEmptyList() {
        Response response = RULE.withTeamUser().post("/api/groupAssignment", EMPTY_GROUP_ASSIGNMENT);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(readResponse(response)).isEmpty();
    }

    @Test
    public void groupAssignment_WithOneUserButNotCommit_ReturnsEmptyList() {
        userDao.insert(USER);
        Response response = RULE.withTeamUser().post("/api/groupAssignment", ASSIGNMENT_WITH_ONE_USER);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(readResponse(response)).isEmpty();
    }

    @Test
    public void groupAssignment_WithOneUserAndSomeCommits_ReturnsAssignments() {
        userDao.insert(USER);
        Stream.of(COMMIT, OTHER_COMMIT_1, OTHER_COMMIT_2, COMMIT_TO_OLD).forEach(commitDao::insert);

        Response response = RULE.withTeamUser().post("/api/groupAssignment", ASSIGNMENT_WITH_ONE_USER);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        List<ReadGroupAssignmentResponse> assignments = readResponse(response);
        assertThat(assignments).hasSize(2);
        assertThat(assignments).anySatisfy(assignment -> {
            assertThat(assignment.getAssignee()).isEqualToComparingFieldByField(USER);
            assertThat(assignment.getCommitAuthor()).isEqualToComparingFieldByField(COMMIT_AUTHOR);
            assertThat(assignment.getCommits()).extracting(Commit::getId).containsExactly(COMMIT_ID);
        });
        assertThat(assignments).anySatisfy(assignment -> {
            assertThat(assignment.getAssignee()).isEqualToComparingFieldByField(USER);
            assertThat(assignment.getCommitAuthor()).isEqualToComparingFieldByField(OTHER_COMMIT_AUTHOR);
            assertThat(assignment.getCommits()).extracting(Commit::getId).containsExactly(OTHER_COMMIT_1.getId(), OTHER_COMMIT_2.getId());
        });
    }

    private List<ReadGroupAssignmentResponse> readResponse(Response response) {
        return response.readEntity(new GenericType<List<ReadGroupAssignmentResponse>>() {
        });
    }
}
