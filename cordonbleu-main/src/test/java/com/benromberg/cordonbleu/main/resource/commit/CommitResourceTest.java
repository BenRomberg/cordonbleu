package com.benromberg.cordonbleu.main.resource.commit;

import static com.benromberg.cordonbleu.service.coderepository.CodeRepositoryMock.COMMIT_PATH_AFTER;
import static com.benromberg.cordonbleu.service.coderepository.CodeRepositoryMock.COMMIT_PATH_BEFORE;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import com.benromberg.cordonbleu.data.dao.CommitDao;
import com.benromberg.cordonbleu.data.dao.TeamDao;
import com.benromberg.cordonbleu.data.model.Comment;
import com.benromberg.cordonbleu.data.model.CommitApproval;
import com.benromberg.cordonbleu.data.model.CommitFilePath;
import com.benromberg.cordonbleu.data.model.CommitFixture;
import com.benromberg.cordonbleu.data.model.CommitLineNumber;
import com.benromberg.cordonbleu.data.model.TeamFlag;
import com.benromberg.cordonbleu.main.CordonBleuTestRule;
import com.benromberg.cordonbleu.main.RequestBuilder;
import com.benromberg.cordonbleu.service.coderepository.CodeRepositoryMock;
import com.benromberg.cordonbleu.service.commit.CommitNotificationActionType;
import com.benromberg.cordonbleu.util.ClockService;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.main.resource.commit.ApprovalRequest;
import com.benromberg.cordonbleu.main.resource.commit.CommitAuthorRequest;
import com.benromberg.cordonbleu.main.resource.commit.CommitListRequest;

public class CommitResourceTest implements CommitFixture {
    private static final CommitLineNumber COMMIT_LINE_NUMBER = new CommitLineNumber(Optional.of(1), Optional.of(2));
    private static final CommitFilePath COMMIT_FILE_PATH = new CommitFilePath(Optional.of("before-path"),
            Optional.of("after-path"));
    private static final int LIMIT = 2;

    @Rule
    @ClassRule
    public static final CordonBleuTestRule RULE = new CordonBleuTestRule().withRepository();

    private final CommitDao commitDao = RULE.getInstance(CommitDao.class);
    private final TeamDao teamDao = RULE.getInstance(TeamDao.class);

    @Test
    public void listPrivateTeam_WithoutBeingLoggedIn_YieldsNotFound() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.PRIVATE, true);
        Response response = RULE.post("/api/commit/list", createListRequest(REPOSITORY_ID));
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void listPrivateTeam_WithLoggedInUser_YieldsNotFound() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.PRIVATE, true);
        Response response = RULE.withAuthenticatedUser().post("/api/commit/list", createListRequest(REPOSITORY_ID));
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void listPrivateTeam_WithTeamMember_ReturnsList() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.PRIVATE, true);
        Response response = RULE.withTeamUser().post("/api/commit/list", createListRequest(REPOSITORY_ID));
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    }

    @Test
    public void listCommits_WithoutCommits_ReturnsEmptyList() throws Exception {
        Response response = RULE.post("/api/commit/list", createListRequest(REPOSITORY_ID));
        List<ReadCommitListItemResponse> commits = getCommitsFromResponse(response);
        assertThat(commits).isEmpty();
    }

    @Test
    public void listCommits_WithSingleMatchingCommit_ReturnsSingletonList() throws Exception {
        commitDao.insert(COMMIT);
        Response response = RULE.post("/api/commit/list", createListRequest(REPOSITORY_ID));
        List<ReadCommitListItemResponse> commits = getCommitsFromResponse(response);
        assertThat(commits).extracting(ReadCommitListItemResponse::getHash).containsExactly(COMMIT_HASH);
    }

    @Test
    public void listCommits_WithSingleNonMatchingCommit_ReturnsEmptyList() throws Exception {
        commitDao.insert(COMMIT);
        Response response = RULE.post("/api/commit/list", createListRequest("non-existing-repo-id"));
        List<ReadCommitListItemResponse> commits = getCommitsFromResponse(response);
        assertThat(commits).isEmpty();
    }

    @Test
    public void commitDetailPrivateTeam_WithLoggedInUser_YieldsNotFound() throws Exception {
        commitDao.insert(COMMIT);
        teamDao.updateFlag(TEAM_ID, TeamFlag.PRIVATE, true);
        Response response = RULE.withAuthenticatedUser().param("hash", COMMIT_HASH).param("teamId", TEAM_ID)
                .get("/api/commit/detail");
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void commitDetailPrivateTeam_WithTeamUser_ReturnsCommit() throws Exception {
        commitDao.insert(COMMIT);
        teamDao.updateFlag(TEAM_ID, TeamFlag.PRIVATE, true);
        Response response = RULE.withTeamUser().param("hash", COMMIT_HASH).param("teamId", TEAM_ID)
                .get("/api/commit/detail");
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    }

    @Test
    public void commitDetail_WithoutCommit_YieldsNotFound() throws Exception {
        Response response = RULE.request().param("hash", COMMIT_HASH).param("teamId", TEAM_ID)
                .get("/api/commit/detail");
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void commitDetail_WithExistingCommit_ReturnsDetails() throws Exception {
        commitDao.insert(COMMIT);
        Response response = RULE.request().param("hash", COMMIT_HASH).param("teamId", TEAM_ID)
                .get("/api/commit/detail");
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        ReadCommitDetailResponse commit = response.readEntity(new GenericType<ReadCommitDetailResponse>() {
        });
        assertThat(commit.getHash()).isEqualTo(COMMIT_HASH);
    }

    @Test
    public void commitDetail_WithRemovedCommit_YieldsGone() throws Exception {
        commitDao.insert(COMMIT);
        commitDao.updateAsRemoved(COMMIT_ID);
        Response response = RULE.request().param("hash", COMMIT_HASH).param("teamId", TEAM_ID)
                .get("/api/commit/detail");
        assertThat(response.getStatus()).isEqualTo(Status.GONE.getStatusCode());
    }

    @Test
    public void commitDetailSpacerLinesPrivateTeam_WithLoggedInUser_YieldsNotFound() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.PRIVATE, true);
        Response response = requestSpacerLines(RULE.request());
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void commitDetailSpacerLinesPrivateTeam_WithTeamUser_ReturnsCommit() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.PRIVATE, true);
        Response response = requestSpacerLines(RULE.withTeamUser());
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    }

    @Test
    public void commitDetailSpacerLines_WithoutCommit_YieldsNotFound() throws Exception {
        Response response = RULE.request().param("hash", COMMIT_HASH).param("teamId", TEAM_ID)
                .param("beforePath", "before.path").param("afterPath", "after.path").param("beginIndex", 1)
                .param("endIndex", 2).get("/api/commit/detail/spacerLines");
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void commitDetailSpacerLines_WithExistingCommit_ReturnsLines() throws Exception {
        Response response = requestSpacerLines(RULE.request());
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        List<ReadLineResponse> lines = response.readEntity(new GenericType<List<ReadLineResponse>>() {
        });
        assertThat(lines).extracting(line -> line.getLine().get().getHighlightedCode()).containsExactly(
                CodeRepositoryMock.COMMIT_CONTENT_BEFORE.getContent(),
                CodeRepositoryMock.COMMIT_CONTENT_AFTER.getContent());
    }

    @Test
    public void commitDetailSpacerLines_WithWrongFile_YieldsNotFound() throws Exception {
        commitDao.insert(COMMIT);
        Response response = RULE.request().param("hash", COMMIT_HASH).param("teamId", TEAM_ID)
                .param("beforePath", "wrongPath").param("afterPath", "wrongPath").param("beginIndex", 0)
                .param("endIndex", 1).get("/api/commit/detail/spacerLines");
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void approveInPrivateTeam_WithLoggedInUser_YieldsNotFound() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.PRIVATE, true);
        commitDao.insert(COMMIT);
        Response response = RULE.withAuthenticatedUser().post("/api/commit/approve",
                new ApprovalRequest(COMMIT_HASH, TEAM_ID));
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void approveInPrivateTeam_WithTeamUser_ReturnsApproval() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.PRIVATE, true);
        commitDao.insert(COMMIT);
        Response response = RULE.withTeamUser().post("/api/commit/approve", new ApprovalRequest(COMMIT_HASH, TEAM_ID));
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    }

    @Test
    public void approveInRestrictedApprovalTeam_WithAuthenticatedUser_YieldsForbidden() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.APPROVE_MEMBER_ONLY, true);
        commitDao.insert(COMMIT);
        Response response = RULE.withAuthenticatedUser().post("/api/commit/approve",
                new ApprovalRequest(COMMIT_HASH, TEAM_ID));
        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void approveInRestrictedApprovalTeam_WithTeamUser_ReturnsApproval() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.APPROVE_MEMBER_ONLY, true);
        commitDao.insert(COMMIT);
        Response response = RULE.withTeamUser().post("/api/commit/approve", new ApprovalRequest(COMMIT_HASH, TEAM_ID));
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    }

    @Test
    public void approve_WithoutAuthenticatedUser_YieldsUnauthorized() throws Exception {
        Response response = RULE.post("/api/commit/approve", new ApprovalRequest(COMMIT_HASH, TEAM_ID));
        assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void approve_WithoutCommit_YieldsNotFound() throws Exception {
        Response response = RULE.withAuthenticatedUser().post("/api/commit/approve",
                new ApprovalRequest(COMMIT_HASH, TEAM_ID));
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void approve_WithExistingCommit_ReturnsApproval() throws Exception {
        commitDao.insert(COMMIT);
        Response response = RULE.withAuthenticatedUser().post("/api/commit/approve",
                new ApprovalRequest(COMMIT_HASH, TEAM_ID));
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        ReadCommitApprovalResponse approvalResponse = response
                .readEntity(new GenericType<ReadCommitApprovalResponse>() {
                });
        assertThat(approvalResponse.getApprover().getEmail()).isEqualTo(CordonBleuTestRule.USER_EMAIL);
        assertThat(approvalResponse.getTime()).isEqualTo(RULE.getDateTime());
    }

    @Test
    public void revertApprovalInPrivateTeam_WithLoggedInUser_YieldsNotFound() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.PRIVATE, true);
        commitDao.insert(COMMIT);
        Response response = RULE.withAuthenticatedUser().post("/api/commit/revertApproval",
                new ApprovalRequest(COMMIT_HASH, TEAM_ID));
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void revertApprovalInPrivateTeam_WithTeamUser_ReturnsApproval() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.PRIVATE, true);
        commitDao.insert(COMMIT);
        Response response = RULE.withTeamUser().post("/api/commit/revertApproval",
                new ApprovalRequest(COMMIT_HASH, TEAM_ID));
        assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    public void revertApprovalInRestrictedApprovalTeam_WithAuthenticatedUser_YieldsForbidden() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.APPROVE_MEMBER_ONLY, true);
        commitDao.insert(COMMIT);
        Response response = RULE.withAuthenticatedUser().post("/api/commit/revertApproval",
                new ApprovalRequest(COMMIT_HASH, TEAM_ID));
        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void revertApprovalInRestrictedApprovalTeam_WithTeamUser_ReturnsApproval() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.APPROVE_MEMBER_ONLY, true);
        commitDao.insert(COMMIT);
        Response response = RULE.withTeamUser().post("/api/commit/revertApproval",
                new ApprovalRequest(COMMIT_HASH, TEAM_ID));
        assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    public void revertApproval_WithoutAuthenticatedUser_YieldsUnauthorized() throws Exception {
        Response response = RULE.post("/api/commit/revertApproval", new ApprovalRequest(COMMIT_HASH, TEAM_ID));
        assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void revertApproval_WithoutCommit_YieldsNotFound() throws Exception {
        Response response = RULE.withAuthenticatedUser().post("/api/commit/revertApproval",
                new ApprovalRequest(COMMIT_HASH, TEAM_ID));
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void revertApproval_WithExistingCommit_ReturnsApproval() throws Exception {
        commitDao.insert(COMMIT);
        commitDao.updateApproval(COMMIT_ID,
                Optional.of(new CommitApproval(RULE.getAuthenticatedUser(), ClockService.now())));
        Response response = RULE.withAuthenticatedUser().post("/api/commit/revertApproval",
                new ApprovalRequest(COMMIT_HASH, TEAM_ID));
        assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    public void getNotifications_WithOtherCommit_AndOwnCommentWithoutResponse_ReturnsNotificationWithoutPrompt()
            throws Exception {
        commitDao.insert(COMMIT);
        commitDao.addComment(COMMIT_ID, new Comment(RULE.getAuthenticatedUser(), "comment", COMMIT_FILE_PATH,
                COMMIT_LINE_NUMBER));
        Response response = RULE.withTeamUser().param("limit", 20).get("/api/commit/notifications");
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        ReadCommitNotificationsResponse commitNotifications = response
                .readEntity(ReadCommitNotificationsResponse.class);
        assertThat(commitNotifications.getTotalPrompts()).isEqualTo(0);
        assertThat(commitNotifications.getNotifications()).hasSize(1);
        ReadCommitNotificationResponse notification = commitNotifications.getNotifications().get(0);
        assertThat(notification.isPrompt()).isFalse();
        assertThat(notification.getCommit().getHash()).isEqualTo(COMMIT_HASH);
        assertThat(notification.getCommit().getTeamName()).isEqualTo(TEAM_NAME);
        assertThat(notification.getLastAction().getType()).isEqualTo(CommitNotificationActionType.COMMENT);
        assertThat(notification.getLastAction().getUser().getEmail()).isEqualTo(CordonBleuTestRule.USER_EMAIL);
        assertThat(notification.getLastAction().getTime()).isEqualTo(RULE.getDateTime());
    }

    private Response requestSpacerLines(RequestBuilder requestBuilder) {
        commitDao.insert(COMMIT);
        return requestBuilder.param("hash", COMMIT_HASH).param("teamId", TEAM_ID)
                .param("beforePath", COMMIT_PATH_BEFORE).param("afterPath", COMMIT_PATH_AFTER).param("beginIndex", 0)
                .param("endIndex", 1).get("/api/commit/detail/spacerLines");
    }

    private CommitListRequest createListRequest(String repositoryId) {
        return new CommitListRequest(asList(repositoryId), asList(new CommitAuthorRequest(COMMIT_AUTHOR_NAME,
                COMMIT_AUTHOR_EMAIL)), asList(), true, Optional.empty(), LIMIT, false);
    }

    private List<ReadCommitListItemResponse> getCommitsFromResponse(Response response) {
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        List<ReadCommitListItemResponse> commits = response
                .readEntity(new GenericType<List<ReadCommitListItemResponse>>() {
                });
        return commits;
    }
}
