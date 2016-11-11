package com.benromberg.cordonbleu.main.resource.team;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import com.benromberg.cordonbleu.data.dao.CodeRepositoryMetadataDao;
import com.benromberg.cordonbleu.data.dao.CommitDao;
import com.benromberg.cordonbleu.data.dao.UserDao;
import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitAuthor;
import com.benromberg.cordonbleu.data.model.CommitFixture;
import com.benromberg.cordonbleu.data.model.CommitRepository;
import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.data.model.UserFlag;
import com.benromberg.cordonbleu.main.CordonBleuTestRule;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class TeamResourceFilterTest implements CommitFixture {
    private static final Team OTHER_TEAM = new TeamBuilder().name("other-team").build();
    private static final CodeRepositoryMetadata OTHER_TEAM_REPOSITORY = new RepositoryBuilder()
            .name("other-repository").team(OTHER_TEAM).build();
    private static final CommitAuthor OTHER_TEAM_COMMIT_AUTHOR = new CommitAuthor("other-team-author",
            "other-team@author.com");
    private static final Commit OTHER_TEAM_COMMIT = new CommitBuilder()
            .repositories(new CommitRepository(OTHER_TEAM_REPOSITORY, emptyList())).author(OTHER_TEAM_COMMIT_AUTHOR)
            .id("other-team-commit").build();

    @Rule
    @ClassRule
    public static final CordonBleuTestRule RULE = new CordonBleuTestRule().withRepository();

    private final UserDao userDao = RULE.getInstance(UserDao.class);
    private final CodeRepositoryMetadataDao repositoryDao = RULE.getInstance(CodeRepositoryMetadataDao.class);
    private final CommitDao commitDao = RULE.getInstance(CommitDao.class);

    @Test
    public void getTeamFilter_IncludesRepository() throws Exception {
        ReadFilterResponse filterResponse = requestTeamFilter();
        assertThat(filterResponse.getRepositories()).extracting(ReadRepositoryFilterResponse::getId,
                ReadRepositoryFilterResponse::getName).containsExactly(tuple(REPOSITORY_ID, REPOSITORY_NAME));
    }

    @Test
    public void getTeamFilter_SkipsRepositoryNotInTeam() throws Exception {
        repositoryDao.insert(OTHER_TEAM_REPOSITORY);
        ReadFilterResponse filterResponse = requestTeamFilter();
        assertThat(filterResponse.getRepositories()).extracting(ReadRepositoryFilterResponse::getId,
                ReadRepositoryFilterResponse::getName).containsExactly(tuple(REPOSITORY_ID, REPOSITORY_NAME));
    }

    @Test
    @Ignore("Aggregation queries not fully supported by Fongo: https://github.com/fakemongo/fongo/issues/8")
    public void getTeamFilter_IncludesAuthor() throws Exception {
        commitDao.insert(COMMIT);
        ReadFilterResponse filterResponse = requestTeamFilter();
        assertThat(filterResponse.getAuthors()).extracting(ReadCommitAuthorResponse::getName,
                ReadCommitAuthorResponse::getEmail).containsExactly(tuple(COMMIT_AUTHOR_NAME, COMMIT_AUTHOR_EMAIL));
    }

    @Test
    @Ignore("Aggregation queries not fully supported by Fongo: https://github.com/fakemongo/fongo/issues/8")
    public void getTeamFilter_SkipsAuthorNotInRepositoryWithinTeam() throws Exception {
        repositoryDao.insert(OTHER_TEAM_REPOSITORY);
        commitDao.insert(OTHER_TEAM_COMMIT);
        ReadFilterResponse filterResponse = requestTeamFilter();
        assertThat(filterResponse.getAuthors()).extracting(ReadCommitAuthorResponse::getName,
                ReadCommitAuthorResponse::getEmail).isEmpty();
    }

    @Test
    public void getTeamFilter_IncludesTeamUser() throws Exception {
        RULE.withTeamUser();
        ReadFilterResponse filterResponse = requestTeamFilter();
        User user = RULE.getAuthenticatedUser();
        assertThat(filterResponse.getUsers()).extracting(ReadUserFilterResponse::getId,
                ReadUserFilterResponse::getName, ReadUserFilterResponse::getEmail).containsExactly(
                tuple(user.getId(), user.getName(), user.getEmail()));
    }

    @Test
    public void getTeamFilter_DoesntIncludeInactiveUser() throws Exception {
        RULE.withTeamUser();
        User user = RULE.getAuthenticatedUser();
        userDao.updateFlag(user.getId(), UserFlag.INACTIVE, true);
        ReadFilterResponse filterResponse = requestTeamFilter();
        assertThat(filterResponse.getUsers()).isEmpty();
    }

    @Test
    public void getTeamFilter_DoesntIncludeUserNotOnTheTeam() throws Exception {
        ReadFilterResponse filterResponse = requestTeamFilter();
        assertThat(filterResponse.getUsers()).isEmpty();
    }

    private ReadFilterResponse requestTeamFilter() {
        Response response = RULE.request().param("name", TEAM_NAME).get("/api/team");
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        return response.readEntity(ReadActiveTeamResponse.class).getFilters();
    }
}
