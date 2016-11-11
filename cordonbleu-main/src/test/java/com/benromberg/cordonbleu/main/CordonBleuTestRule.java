package com.benromberg.cordonbleu.main;

import com.benromberg.cordonbleu.data.dao.CodeRepositoryMetadataDao;
import com.benromberg.cordonbleu.data.dao.CommitDao;
import com.benromberg.cordonbleu.data.dao.TeamDao;
import com.benromberg.cordonbleu.data.dao.UserDao;
import com.benromberg.cordonbleu.data.model.CommentFixture;
import com.benromberg.cordonbleu.data.model.CommitFixture;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.data.model.UserFlag;
import com.benromberg.cordonbleu.data.model.UserTeamFlag;
import com.benromberg.cordonbleu.data.testutil.DatabaseRule;
import com.benromberg.cordonbleu.service.user.UserService;
import com.benromberg.cordonbleu.util.OpenPortProvider;
import com.benromberg.cordonbleu.util.SystemTimeRule;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.junit.DropwizardAppRule;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.benromberg.cordonbleu.main.config.CordonBleuConfiguration;
import com.benromberg.cordonbleu.main.config.WebsiteVersionFeature;
import com.squarespace.jersey2.guice.BootstrapUtils;

public class CordonBleuTestRule implements TestRule, CommitFixture, CommentFixture {
    public static final String USER_NAME = "authenticatedUserName";
    public static final String USER_PASSWORD = "authenticated user password";
    public static final String USER_EMAIL = "authenticated_user@email.com";
    public static final String SESSION_COOKIE_NAME = "session";

    private final int applicationPort = OpenPortProvider.getOpenPort();
    private final DatabaseRule databaseRule = new DatabaseRule();
    private final SystemTimeRule systemTimeRule = new SystemTimeRule();
    private final ConfigOverride[] additionalConfigOverrides;
    private final CordonBleuTestApplication testApplication;
    private final String configurationPath;
    private final DropwizardAppRule<CordonBleuConfiguration> dropwizardRule;
    private final Client client = ClientBuilder.newClient().register(ObjectMapperContextResolver.class);
    private final List<Runnable> beforeEachCallbacks = new ArrayList<>();

    private User authenticatedUser;
    private UserService userService;

    private class AdjustedTestSupport extends DropwizardTestSupport<CordonBleuConfiguration> {
        public AdjustedTestSupport() {
            super(testApplication.getClass(), configurationPath, getConfigOverrides());
        }

        @Override
        public io.dropwizard.Application<CordonBleuConfiguration> newApplication() {
            return testApplication;
        }
    }

    public CordonBleuTestRule(ConfigOverride... additionalOverrides) {
        this.testApplication = new CordonBleuTestApplication();
        this.additionalConfigOverrides = additionalOverrides;
        this.configurationPath = "src/test/resources/config-test.json";
        dropwizardRule = new DropwizardAppRule<>(new AdjustedTestSupport());
    }

    private void beforeEach() {
        userService = getInstance(UserService.class);
        authenticatedUser = userService.registerUser(USER_EMAIL, USER_NAME, USER_PASSWORD);
        beforeEachCallbacks.forEach(Runnable::run);
    }

    public CordonBleuTestRule withTeam() {
        beforeEachCallbacks.add(() -> getInstance(TeamDao.class).insert(TEAM));
        return this;
    }

    public CordonBleuTestRule withRepository() {
        withTeam();
        beforeEachCallbacks.add(() -> getInstance(CodeRepositoryMetadataDao.class).insert(REPOSITORY));
        return this;
    }

    public CordonBleuTestRule withCommit() {
        withRepository();
        beforeEachCallbacks.add(() -> getInstance(CommitDao.class).insert(COMMIT));
        return this;
    }

    public CordonBleuTestRule withCommentUser() {
        beforeEachCallbacks.add(() -> getInstance(UserDao.class).insert(COMMENT_USER));
        return this;
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }

    private ConfigOverride[] getConfigOverrides() {
        return Stream.concat(getDefaultConfigOverrides(), Arrays.stream(additionalConfigOverrides)).toArray(
                ConfigOverride[]::new);
    }

    private Stream<ConfigOverride> getDefaultConfigOverrides() {
        return Stream.of(ConfigOverride.config("server.connector.port", Integer.toString(applicationPort)));
    }

    private URI getUri() {
        return URI.create(String.format("http://localhost:%d", applicationPort));
    }

    public Response post(String path, Object entity) {
        return request().post(path, entity);
    }

    public Response get(String path) {
        return request().get(path);
    }

    public RequestBuilder request() {
        return new RequestBuilder(client.target(getUri())).header(WebsiteVersionFeature.WEBSITE_VERSION_HEADER,
                WebsiteVersionFeature.WEBSITE_VERSION);
    }

    public RequestBuilder withAuthenticatedUser() {
        String sessionId = userService.startSession(authenticatedUser, USER_PASSWORD).get();
        return request().header("Cookie", SESSION_COOKIE_NAME + "=" + sessionId);
    }

    public RequestBuilder withTeamUser() {
        userService.joinTeam(authenticatedUser.getId(), TEAM_ID);
        return withAuthenticatedUser();
    }

    public RequestBuilder withTeamOwnerUser() {
        userService.joinTeam(authenticatedUser.getId(), TEAM_ID);
        userService.updateTeamFlag(authenticatedUser.getId(), TEAM_ID, UserTeamFlag.OWNER, true);
        return withAuthenticatedUser();
    }

    public RequestBuilder withAdminUser() {
        userService.updateFlag(authenticatedUser.getId(), UserFlag.ADMIN, true);
        return withAuthenticatedUser();
    }

    public <T> T getInstance(Class<T> clazz) {
        return testApplication.getInjector().getInstance(clazz);
    }

    public LocalDateTime getDateTime() {
        return systemTimeRule.getDateTime();
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        if (description.isTest()) {
            return systemTimeRule.apply(databaseRule.apply(new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    beforeEach();
                    base.evaluate();
                }
            }, description), description);
        }
        return dropwizardRule.apply(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    base.evaluate();
                } finally {
                    // dropwizard-guice overrides some singleton instances and never resets them in a test environment
                    // So we need to do it ourselves...
                    // See also https://github.com/HubSpot/dropwizard-guice/issues/50
                    BootstrapUtils.reset();
                }
            }

        }, description);
    }
}
