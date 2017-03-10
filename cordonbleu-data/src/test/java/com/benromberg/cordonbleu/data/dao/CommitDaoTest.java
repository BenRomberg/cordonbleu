package com.benromberg.cordonbleu.data.dao;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import com.benromberg.cordonbleu.data.model.CommentFixture;
import com.benromberg.cordonbleu.data.model.CommitFixture;
import com.benromberg.cordonbleu.util.SystemTimeRule;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.benromberg.cordonbleu.data.dao.CodeRepositoryMetadataDao;
import com.benromberg.cordonbleu.data.dao.CommitDao;
import com.benromberg.cordonbleu.data.dao.CommitFilter;
import com.benromberg.cordonbleu.data.dao.TeamDao;
import com.benromberg.cordonbleu.data.dao.UserDao;
import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;
import com.benromberg.cordonbleu.data.model.Comment;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitApproval;
import com.benromberg.cordonbleu.data.model.CommitAuthor;
import com.benromberg.cordonbleu.data.model.CommitId;
import com.benromberg.cordonbleu.data.model.CommitRepository;
import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.model.User;
import com.mongodb.DBObject;

public class CommitDaoTest implements CommitFixture, CommentFixture {
    private static final Team OTHER_TEAM = new TeamBuilder().name("other-team").build();
    private static final String OTHER_REPOSITORY_NAME = "other-repository";
    private static final String COMMIT_APPROVER_NAME = "approver";
    private static final String COMMIT_APPROVER_EMAIL = "approver@email.com";
    private static final User COMMIT_USER = new User(COMMIT_AUTHOR_EMAIL, COMMIT_AUTHOR_NAME, "author password");
    private static final String FIRST_AUTHOR_NAME = "aaaauthor";
    private static final String UPPERCASE_AUTHOR = "Uppercase Author";
    private static final String OTHER_TEXT = "other text";
    private static final LocalDateTime COMMIT_APPROVAL_TIME = LocalDateTime.now();
    private static final String OTHER_COMMIT_HASH = "other commit hash";
    private static final List<String> OTHER_COMMIT_BRANCHES = asList("other commit branch");
    private static final CodeRepositoryMetadata OTHER_REPOSITORY = new RepositoryBuilder().name(OTHER_REPOSITORY_NAME)
            .build();
    private static final User COMMIT_APPROVER = new User(COMMIT_APPROVER_EMAIL, COMMIT_APPROVER_NAME,
            "approver password");

    @Rule
    public SystemTimeRule systemTimeRule = new SystemTimeRule();

    @Rule
    public DaoRule databaseRule = new DaoRule().withRepository().withCommentUser();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private final UserDao userDao = databaseRule.createUserDao();
    private final TeamDao teamDao = databaseRule.createTeamDao();
    private final CommitDao dao = databaseRule.createCommitDao();
    private final CodeRepositoryMetadataDao repositoryDao = databaseRule.createRepositoryDao();

    @Before
    public void setUp() {
        userDao.insert(COMMIT_APPROVER);
    }

    @Test
    public void insertedElement_CanBeFoundById() throws Exception {
        Commit dummyElement = COMMIT;
        dao.insert(dummyElement);
        Commit foundElement = dao.findById(dummyElement.getId()).get();
        assertCommit(foundElement);
    }

    @Test
    public void insertedElement_HasCreatedAsIsoDate() throws Exception {
        Commit dummyElement = COMMIT;
        dao.insert(dummyElement);
        DBObject foundElement = DaoRule.getDB().getCollection(CommitDao.COLLECTION_NAME).findOne();
        assertThat(foundElement.get("created")).isInstanceOf(Date.class);
    }

    @Test
    public void insertedElement_HasRepositoryAsReference() throws Exception {
        Commit dummyElement = COMMIT;
        dao.insert(dummyElement);
        DBObject foundElement = DaoRule.getDB().getCollection(CommitDao.COLLECTION_NAME).findOne();
        @SuppressWarnings("unchecked")
        List<Object> repositories = (List<Object>) foundElement.get("repositories");
        assertThat(((DBObject) repositories.get(0)).get("repository")).isInstanceOf(String.class);
    }

    @Test
    public void insertedElement_CanBeFoundByRepository() throws Exception {
        Commit dummyElement = COMMIT;
        dao.insert(dummyElement);
        List<Commit> foundCommits = dao.findByRepositories(asList(REPOSITORY));
        assertThat(foundCommits).extracting(Commit::getId).containsExactly(COMMIT_ID);
    }

    @Test
    public void insertedElement_CanBeFoundByRepository_OrderedByTimeDesc() throws Exception {
        Commit firstElement = COMMIT;
        Commit secondElement = commit().id(OTHER_COMMIT_HASH).created(COMMIT_CREATED.minusMinutes(1)).build();
        dao.insert(secondElement);
        dao.insert(firstElement);
        List<Commit> foundCommits = dao.findByRepositories(asList(REPOSITORY));
        assertThat(foundCommits).containsExactly(firstElement, secondElement);
    }

    @Test
    public void insertedElement_CanNotBeFoundByOtherRepository() throws Exception {
        dao.insert(COMMIT);
        List<Commit> foundCommits = dao.findByRepositories(asList(OTHER_REPOSITORY));
        assertThat(foundCommits).isEmpty();
    }

    @Test
    public void insertOrUpdateRepository_OnInsert_CanBeFoundById() throws Exception {
        Commit dummyElement = COMMIT;
        Commit commit = dao.insertOrUpdateRepository(dummyElement, COMMIT_REPOSITORY);
        assertCommit(commit);
    }

    @Test
    public void insertOrUpdateRepository_OnExistingCommit_UpdatesRepository() throws Exception {
        Commit dummyElement = COMMIT;
        dao.insert(dummyElement);
        repositoryDao.insert(OTHER_REPOSITORY);
        CommitRepository commitRepository = new CommitRepository(OTHER_REPOSITORY, OTHER_COMMIT_BRANCHES);
        Commit commit = dao.insertOrUpdateRepository(commit().repositories(commitRepository).build(), commitRepository);
        assertThat(commit.getRepositories()).extracting(repository -> repository.getRepository().getName(),
                CommitRepository::getBranches).containsExactly(tuple(REPOSITORY_NAME, COMMIT_BRANCHES),
                tuple(OTHER_REPOSITORY_NAME, OTHER_COMMIT_BRANCHES));
    }

    @Test
    @Ignore("would only fail with real mongo")
    public void insertOrUpdateRepository_WithManyBranches_DoesntExceedIndexLimit() throws Exception {
        Commit dummyElement = COMMIT;
        List<String> manyBranches = Collections.nCopies(1024, "branch");
        Commit commit = dao.insertOrUpdateRepository(dummyElement, new CommitRepository(REPOSITORY, manyBranches));
        assertThat(commit.getRepositories()).extracting(entry -> entry.getRepository().getName(),
                CommitRepository::getBranches).containsExactly(tuple(REPOSITORY_NAME, manyBranches));
    }

    @Test
    public void findByFilter_CanBeFoundByRepositoryAndAuthor() throws Exception {
        Commit dummyElement = COMMIT;
        dao.insert(dummyElement);
        List<Commit> foundCommits = dao.findByFilter(createFilter(REPOSITORY, dummyElement.getAuthor(), true));
        assertThat(foundCommits).extracting(Commit::getId).containsExactly(COMMIT_ID);
    }

    @Test
    public void findByFilter_CanBeFoundByRepositoryAndAuthorEmail_CaseInsensitive() throws Exception {
        Commit dummyElement = COMMIT;
        dao.insert(dummyElement);
        List<Commit> foundCommits = dao.findByFilter(createFilter(REPOSITORY, new CommitAuthor(FIRST_AUTHOR_NAME,
                COMMIT_AUTHOR_EMAIL.toUpperCase()), true));
        assertThat(foundCommits).extracting(Commit::getId).containsExactly(COMMIT_ID);
    }

    @Test
    public void findByFilter_CanBeFoundByRepositoryAndUser() throws Exception {
        userDao.insert(COMMIT_USER);
        Commit dummyElement = COMMIT;
        dao.insert(dummyElement);
        List<Commit> foundCommits = dao.findByFilter(createFilter(REPOSITORY, COMMIT_USER, true));
        assertThat(foundCommits).extracting(Commit::getId).containsExactly(COMMIT_ID);
    }

    @Test
    public void findByFilter_CanBeFoundByRepositoryAndUser_WithEmailHavingDifferentCase() throws Exception {
        User uppercaseEmailUser = new User(COMMIT_AUTHOR_EMAIL.toUpperCase(), COMMIT_AUTHOR_NAME, "author password");
        userDao.insert(uppercaseEmailUser);
        Commit dummyElement = COMMIT;
        dao.insert(dummyElement);
        List<Commit> foundCommits = dao.findByFilter(createFilter(REPOSITORY, uppercaseEmailUser, true));
        assertThat(foundCommits).extracting(Commit::getId).containsExactly(COMMIT_ID);
    }

    @Test
    public void findByFilter_CanBeFoundByRepositoryAndUsersEmailAlias() throws Exception {
        User aliasUser = userDao.update(COMMIT_APPROVER, COMMIT_APPROVER_NAME, COMMIT_APPROVER_EMAIL,
                asList(COMMIT_AUTHOR_EMAIL)).get();
        Commit dummyElement = COMMIT;
        dao.insert(dummyElement);
        List<Commit> foundCommits = dao.findByFilter(createFilter(REPOSITORY, aliasUser, true));
        assertThat(foundCommits).extracting(Commit::getId).containsExactly(COMMIT_ID);
    }

    @Test
    public void findByFilter_CanNotBeFoundByOtherRepositoryAndSameAuthor() throws Exception {
        Commit dummyElement = COMMIT;
        dao.insert(dummyElement);
        List<Commit> foundCommits = dao.findByFilter(createFilter(OTHER_REPOSITORY, dummyElement.getAuthor(), true));
        assertThat(foundCommits).isEmpty();
    }

    @Test
    public void findByFilter_CanNotBeFoundBySameRepositoryAndOtherAuthorEmail() throws Exception {
        Commit dummyElement = COMMIT;
        dao.insert(dummyElement);
        List<Commit> foundCommits = dao.findByFilter(createFilter(REPOSITORY, new CommitAuthor(COMMIT_AUTHOR_NAME,
                "other@email.com"), true));
        assertThat(foundCommits).isEmpty();
    }

    @Test
    public void findByFilter_CanNotBeFoundByUnapproved() throws Exception {
        Commit dummyElement = COMMIT;
        dao.insert(dummyElement);
        dao.updateApproval(dummyElement.getId(), Optional.of(new CommitApproval(COMMIT_APPROVER, COMMIT_APPROVAL_TIME)));
        List<Commit> foundCommits = dao.findByFilter(createFilter(REPOSITORY, dummyElement.getAuthor(), false));
        assertThat(foundCommits).isEmpty();
    }

    @Test
    public void findByFilter_StartsAfterLastCommitHash() throws Exception {
        Commit firstElement = COMMIT;
        Commit secondElement = commit().id(OTHER_COMMIT_HASH).created(COMMIT_CREATED.minusMinutes(1)).build();
        dao.insert(secondElement);
        dao.insert(firstElement);
        List<Commit> foundCommits = dao.findByFilter(createFilter(Optional.of(firstElement.getId().getHash()), 100));
        assertThat(foundCommits).containsExactly(secondElement);
    }

    @Test
    public void findByFilter_StartsAfterLastCommitHash_WithCommitAtSameTime() throws Exception {
        Commit firstElement = COMMIT;
        Commit secondElement = commit().id(OTHER_COMMIT_HASH).build();
        dao.insert(secondElement);
        dao.insert(firstElement);
        List<Commit> foundCommits = dao.findByFilter(createFilter(Optional.of(firstElement.getId().getHash()), 100));
        assertThat(foundCommits).containsExactly(secondElement);
    }

    @Test
    public void findByFilter_WithUnknownLastCommitHash_ThrowsNoSuchElementException() throws Exception {
        expectedException.expect(NoSuchElementException.class);
        dao.findByFilter(createFilter(Optional.of("unknown-last-commit-hash"), 100));
    }

    @Test
    public void findByFilter_ObeysLimit() throws Exception {
        Commit firstElement = COMMIT;
        Commit secondElement = commit().id(OTHER_COMMIT_HASH).created(COMMIT_CREATED.minusMinutes(1)).build();
        dao.insert(secondElement);
        dao.insert(firstElement);
        List<Commit> foundCommits = dao.findByFilter(createFilter(Optional.empty(), 1));
        assertThat(foundCommits).containsExactly(firstElement);
    }

    @Test
    public void findByFilter_WithMultipleCommitsHavingSameTimestamp_DoesntReturnSmallerCommitHash() throws Exception {
        Commit firstElement = COMMIT;
        Commit secondElement = commit().id(OTHER_COMMIT_HASH).build();
        dao.insert(firstElement);
        dao.insert(secondElement);
        List<Commit> foundCommits = dao.findByFilter(createFilter(Optional.of(OTHER_COMMIT_HASH), 1));
        assertThat(foundCommits).isEmpty();
    }

    @Test
    public void findByFilter_WithCommitHavingSmallerTimestampAndHash_ReturnsCommit() throws Exception {
        Commit firstElement = COMMIT;
        Commit secondElement = commit().id(OTHER_COMMIT_HASH).created(COMMIT_CREATED.plusMinutes(1)).build();
        dao.insert(firstElement);
        dao.insert(secondElement);
        List<Commit> foundCommits = dao.findByFilter(createFilter(Optional.of(OTHER_COMMIT_HASH), 1));
        assertThat(foundCommits).containsExactly(firstElement);
    }

    @Test
    public void updateAsRemoved_WithoutCommit_ReturnsEmpty() throws Exception {
        Optional<Commit> commit = dao.updateAsRemoved(new CommitId("non-existing-hash", TEAM));
        assertThat(commit).isEmpty();
    }

    @Test
    public void updateAsRemoved_WithCommit_ReturnsCommitAsRemoved() throws Exception {
        dao.insert(COMMIT);
        Commit commit = dao.updateAsRemoved(COMMIT_ID).get();
        assertThat(commit.isRemoved()).isTrue();
    }

    @Test
    public void updatedApproval_CanBeFoundInCommit() throws Exception {
        Commit dummyElement = COMMIT;
        dao.insert(dummyElement);
        dao.updateApproval(dummyElement.getId(), Optional.of(new CommitApproval(COMMIT_APPROVER, COMMIT_APPROVAL_TIME)));
        Commit commit = dao.findById(dummyElement.getId()).get();
        CommitApproval approval = commit.getApproval().get();
        assertThat(approval.getApprover()).isEqualTo(COMMIT_APPROVER);
        assertThat(approval.getTime()).isEqualTo(COMMIT_APPROVAL_TIME);
    }

    @Test
    public void updateApproval_DoesNotDestroyCommit() throws Exception {
        Commit dummyElement = COMMIT;
        dao.insert(dummyElement);
        dao.updateApproval(dummyElement.getId(), Optional.of(new CommitApproval(COMMIT_APPROVER, COMMIT_APPROVAL_TIME)));
        Commit commit = dao.findById(dummyElement.getId()).get();
        assertThat(commit.getAuthor().getName()).isEqualTo(COMMIT_AUTHOR_NAME);
    }

    @Test
    public void revertedApproval_CanBeFoundInCommit() throws Exception {
        Commit dummyElement = COMMIT;
        dao.insert(dummyElement);
        dao.updateApproval(dummyElement.getId(), Optional.of(new CommitApproval(COMMIT_APPROVER, COMMIT_APPROVAL_TIME)));
        dao.updateApproval(dummyElement.getId(), Optional.empty());
        Optional<CommitApproval> approval = dao.findById(dummyElement.getId()).get().getApproval();
        assertThat(approval).isEqualTo(Optional.empty());
    }

    @Test
    public void updateApproval_ReturnsUpdatedCommit() throws Exception {
        Commit dummyElement = COMMIT;
        dao.insert(dummyElement);
        Commit returnedCommit = dao.updateApproval(dummyElement.getId(),
                Optional.of(new CommitApproval(COMMIT_APPROVER, COMMIT_APPROVAL_TIME))).get();
        assertThat(returnedCommit.getApproval().get().getApprover()).isEqualTo(COMMIT_APPROVER);
        assertThat(returnedCommit.getApproval().get().getTime()).isEqualTo(COMMIT_APPROVAL_TIME);
    }

    @Test
    public void addedComment_CanBeFoundInCommit() throws Exception {
        insertWithComment(COMMIT);
        List<Comment> foundComments = dao.findById(COMMIT.getId()).get().getComments();
        assertThat(foundComments).extracting(Comment::getText).containsExactly(COMMENT_TEXT);
    }

    @Test
    public void addComment_ReturnsUpdatedCommit() throws Exception {
        Commit dummyElement = COMMIT;
        Commit returnedCommit = insertWithComment(dummyElement);
        assertThat(returnedCommit.getComments()).extracting(Comment::getText).containsExactly(COMMENT_TEXT);
    }

    @Test
    public void updatedComment_CanBeFoundInCommit() throws Exception {
        Commit commit = insertWithComment(COMMIT);
        dao.updateComment(commit.getId(), commit.getComments().get(0).getId(), OTHER_TEXT);
        List<Comment> foundComments = dao.findById(COMMIT.getId()).get().getComments();
        assertThat(foundComments).extracting(Comment::getText).containsExactly(OTHER_TEXT);
    }

    @Test
    public void updateComment_ReturnsUpdatedCommit() throws Exception {
        Commit commit = insertWithComment(COMMIT);
        Commit returnedCommit = dao.updateComment(commit.getId(), commit.getComments().get(0).getId(), OTHER_TEXT)
                .get();
        assertThat(returnedCommit.getComments()).extracting(Comment::getText).containsExactly(OTHER_TEXT);
    }

    @Test
    public void removedComment_CanNotBeFoundInCommit() throws Exception {
        Commit commit = insertWithComment(COMMIT);
        dao.removeComment(commit.getId(), commit.getComments().get(0).getId());
        List<Comment> foundComments = dao.findById(COMMIT.getId()).get().getComments();
        assertThat(foundComments).isEmpty();
    }

    @Test
    public void removeComment_ReturnsUpdatedCommit() throws Exception {
        Commit commit = insertWithComment(COMMIT);
        Commit returnedCommit = dao.removeComment(commit.getId(), commit.getComments().get(0).getId()).get();
        assertThat(returnedCommit.getComments()).isEmpty();
    }

    @Test
    public void addedComments_AreOrderedByCreated() throws Exception {
        Commit commit = insertWithComment(COMMIT);
        systemTimeRule.advanceBySeconds(-1);
        dao.addComment(commit.getId(), comment().text(OTHER_TEXT).build());
        List<Comment> foundComments = dao.findById(commit.getId()).get().getComments();
        assertThat(foundComments).extracting(Comment::getText).containsExactly(OTHER_TEXT, COMMENT_TEXT);
    }

    @Test
    @Ignore("Aggregation queries not fully supported by Fongo: https://github.com/fakemongo/fongo/issues/8")
    public void findAuthors_IncludesAddedCommitAuthorEmail() throws Exception {
        dao.insert(COMMIT);
        List<CommitAuthor> authors = dao.findAuthors(TEAM);
        assertThat(authors).extracting(CommitAuthor::getName, CommitAuthor::getEmail).containsExactly(
                tuple(COMMIT_AUTHOR_NAME, COMMIT_AUTHOR_EMAIL));
    }

    @Test
    @Ignore("Aggregation queries not fully supported by Fongo: https://github.com/fakemongo/fongo/issues/8")
    public void findAuthors_SkipsAuthorsOutsideTheTeam() throws Exception {
        dao.insert(COMMIT);
        teamDao.insert(OTHER_TEAM);
        List<CommitAuthor> authors = dao.findAuthors(OTHER_TEAM);
        assertThat(authors).isEmpty();
    }

    @Test
    @Ignore("Aggregation queries not fully supported by Fongo: https://github.com/fakemongo/fongo/issues/8")
    public void foundAuthors_AreSortedByName() throws Exception {
        Commit commit = COMMIT;
        dao.insert(commit);
        dao.insert(commit().id(OTHER_COMMIT_HASH).author(new CommitAuthor(FIRST_AUTHOR_NAME, COMMIT_AUTHOR_EMAIL))
                .build());
        List<CommitAuthor> authors = dao.findAuthors(TEAM);
        assertThat(authors).extracting(CommitAuthor::getName).containsExactly(FIRST_AUTHOR_NAME, COMMIT_AUTHOR_NAME);
    }

    @Test
    @Ignore("Aggregation queries not fully supported by Fongo: https://github.com/fakemongo/fongo/issues/8")
    public void foundAuthors_AreSortedByName_IgnoreCase() throws Exception {
        Commit commit = COMMIT;
        dao.insert(commit);
        dao.insert(commit().id(OTHER_COMMIT_HASH).author(new CommitAuthor(UPPERCASE_AUTHOR, COMMIT_AUTHOR_EMAIL))
                .build());
        List<CommitAuthor> authors = dao.findAuthors(TEAM);
        assertThat(authors).extracting(CommitAuthor::getName).containsExactly(COMMIT_AUTHOR_NAME, UPPERCASE_AUTHOR);
    }

    @Test
    public void removeOrphaned_RemovesCommitNoLongerAttachedToGivenRepositories() throws Exception {
        Commit dummyElement = COMMIT;
        dao.insert(dummyElement);
        dao.removeOrphaned(asList());
        assertThat(dao.findById(dummyElement.getId())).isEmpty();
    }

    @Test
    public void removeOrphaned_KeepsCommitWithExistingRepository() throws Exception {
        Commit dummyElement = COMMIT;
        dao.insert(dummyElement);
        dao.removeOrphaned(asList(REPOSITORY));
        assertThat(dao.findById(dummyElement.getId())).isPresent();
    }

    @Test
    public void removeOrphaned_FromCommitWithTwoRepositories_KeepsOnlyOneExistingRepository() throws Exception {
        CommitRepository noLongerExistingRepository = new CommitRepository(repository().name(OTHER_REPOSITORY_NAME)
                .build(), COMMIT_BRANCHES);
        Commit dummyElement = commit().repositories(COMMIT_REPOSITORY, noLongerExistingRepository).build();
        dao.insert(dummyElement);
        dao.removeOrphaned(asList(REPOSITORY));
        assertThat(dao.findById(dummyElement.getId()).get().getRepositories()).hasSize(1);
    }

    @Test
    public void findNotifications_WithoutCommits_ReturnsEmptyList() throws Exception {
        List<Commit> notifications = insertUserWithTeamAndFindNotifications(COMMIT_APPROVER, 100);
        assertThat(notifications).isEmpty();
    }

    @Test
    public void findNotifications_WithUncommentedCommit_ReturnsEmptyList() throws Exception {
        dao.insert(COMMIT);
        List<Commit> notifications = insertUserWithTeamAndFindNotifications(COMMIT_USER, 100);
        assertThat(notifications).isEmpty();
    }

    @Test
    public void findNotifications_WithOwnCommitCommented_ButInAnotherTeam_ReturnsEmptyList() throws Exception {
        insertWithComment(COMMIT);
        userDao.insert(COMMIT_USER);
        teamDao.insert(OTHER_TEAM);
        userDao.addTeam(COMMIT_USER.getId(), OTHER_TEAM);
        List<Commit> notifications = dao.findNotifications(COMMIT_USER, 100);
        assertThat(notifications).isEmpty();
    }

    @Test
    public void findNotifications_WithOwnCommitCommented_ReturnsCommit() throws Exception {
        Commit commit = insertWithComment(COMMIT);
        List<Commit> notifications = insertUserWithTeamAndFindNotifications(COMMIT_USER, 100);
        assertThat(notifications).extracting(Commit::getId).containsExactly(commit.getId());
    }

    @Test
    public void findNotifications_WithCommitCommentedByUser_ReturnsCommit() throws Exception {
        Commit commit = insertWithComment(COMMIT);
        List<Commit> notifications = insertUserWithTeamAndFindNotifications(COMMENT_USER, 100);
        assertThat(notifications).extracting(Commit::getId).containsExactly(commit.getId());
    }

    @Test
    public void findNotifications_SortedByLastCommentDateDesc() throws Exception {
        Commit commit = insertWithComment(COMMIT);
        systemTimeRule.advanceBySeconds(1);
        Commit otherCommit = insertWithComment(commit().id(OTHER_COMMIT_HASH).build());
        List<Commit> notifications = insertUserWithTeamAndFindNotifications(COMMENT_USER, 100);
        assertThat(notifications).extracting(Commit::getId).containsExactly(otherCommit.getId(), commit.getId());
    }

    @Test
    public void findNotifications_ObeysLimit() throws Exception {
        insertWithComment(COMMIT);
        systemTimeRule.advanceBySeconds(1);
        Commit otherCommit = insertWithComment(commit().id(OTHER_COMMIT_HASH).build());
        List<Commit> notifications = insertUserWithTeamAndFindNotifications(COMMENT_USER, 1);
        assertThat(notifications).extracting(Commit::getId).containsExactly(otherCommit.getId());
    }

    private List<Commit> insertUserWithTeamAndFindNotifications(User user, int limit) {
        userDao.insertIfNotExists(user);
        User userWithTeam = userDao.addTeam(user.getId(), TEAM).get();
        return dao.findNotifications(userWithTeam, limit);
    }

    private void assertCommit(Commit foundElement) {
        assertThat(foundElement.getAuthor().getName()).isEqualTo(COMMIT_AUTHOR_NAME);
        assertThat(foundElement.getAuthor().getEmail()).isEqualTo(COMMIT_AUTHOR_EMAIL);
        assertThat(foundElement.getCreated()).isEqualTo(COMMIT_CREATED);
        assertThat(foundElement.getMessage()).isEqualTo(COMMIT_MESSAGE);
        assertThat(foundElement.getRepositories()).extracting(entry -> entry.getRepository().getName(),
                CommitRepository::getBranches).containsExactly(tuple(REPOSITORY_NAME, COMMIT_BRANCHES));
        assertThat(foundElement.getApproval()).isEqualTo(Optional.empty());
    }

    private CommitFilter createFilter(CodeRepositoryMetadata repository, CommitAuthor author, boolean approved) {
        return new CommitFilter(TEAM, asList(repository), asList(author), asList(), approved, Optional.empty(), 100, false);
    }

    private CommitFilter createFilter(CodeRepositoryMetadata repository, User user, boolean approved) {
        return new CommitFilter(TEAM, asList(repository), asList(), asList(user), approved, Optional.empty(), 100, false);
    }

    private CommitFilter createFilter(Optional<String> lastCommitId, int limit) {
        return new CommitFilter(TEAM, asList(REPOSITORY), asList(COMMIT_AUTHOR), asList(), true, lastCommitId, limit, false);
    }

    private Commit insertWithComment(Commit dummyElement) {
        dao.insert(dummyElement);
        return dao.addComment(dummyElement.getId(), comment().build()).get();
    }

}
