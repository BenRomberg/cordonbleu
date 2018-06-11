package com.benromberg.cordonbleu.service.coderepository;

import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitFixture;
import com.benromberg.cordonbleu.data.model.CommitId;
import com.benromberg.cordonbleu.data.model.CommitRepository;
import com.benromberg.cordonbleu.data.model.TeamKeyPair;
import com.benromberg.cordonbleu.util.ClasspathUtil;
import com.benromberg.cordonbleu.util.SystemTimeRule;

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.benromberg.cordonbleu.service.coderepository.GitTestRepository.COMMITTED_FILE_CONTENT;
import static com.benromberg.cordonbleu.service.coderepository.GitTestRepository.COMMITTED_FILE_NAME;
import static com.benromberg.cordonbleu.service.coderepository.GitTestRepository.withinMarginOfError;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.eclipse.jgit.lib.Constants.MASTER;
import static org.eclipse.jgit.lib.Constants.OBJECT_ID_STRING_LENGTH;

public class GitRepositoryTest implements CommitFixture {
    private static final String BINARY_FILENAME = "file.bin";
    private static final String NEW_FILE_CONTENT = "new file content";
    private static final String NEW_FILE_NAME = "new-file-name";
    private static final String NEW_BRANCH = "new-branch";
    private static final String WHITESPACE_CONTENT = "\t ";
    private static final String BINARY_CONTENT = new String(Base64.getDecoder().decode("000102030405060708090a0b0c0d0e0f"));
    private static final String ISO_8859_1_CONTENT = "äußerst übermäßige Verwendung von Sonderzeichen";
    private static final String PRIVATE_KEY = ClasspathUtil.readFileFromClasspath("keys/private.key");
    private static final String PRIVATE_KEY_PASSWORD = "private-key-password";

    @Rule
    public GitTestRepository git = new GitTestRepository();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public SystemTimeRule systemTimeRule = new SystemTimeRule();

    private GitRepository repository;
    private File repositoryFolder;
    private CodeRepositoryMetadata repositoryMetadata;

    @Before
    public void setUp() throws Exception {
        repositoryFolder = new File(temporaryFolder.getRoot(), "repository");
        repositoryMetadata = repository().sourceUrl(git.getUrl()).build();
        repository = new GitRepository(repositoryMetadata, repositoryFolder, () -> PRIVATE_KEY_PASSWORD);
    }

    @Test
    public void emptyRepository_HasNoBranch() throws Exception {
        List<String> branches = repository.getBranches();
        assertThat(branches).isEmpty();
    }

    @Test
    public void repository_WithCommit_HasMasterBranch() throws Exception {
        git.createFileAndCommit();
        repository.pull(asList());
        List<String> branches = repository.getBranches();
        assertThat(branches).containsExactly(MASTER);
    }

    @Test
    public void repository_HasNewBranch_AfterPulling() throws Exception {
        git.createFileAndCommit();
        // first commit needed as creating a new branch would fail otherwise
        // see info-box on http://www.codeaffine.com/2015/05/06/jgit-initialize-repository/
        git.createAndCheckout(NEW_BRANCH);
        repository.pull(asList());
        List<String> branches = repository.getBranches();
        assertThat(branches).containsExactly(MASTER, NEW_BRANCH);
    }

    @Test
    public void repository_WithCommit_HasSingleCommit() throws Exception {
        git.createFileAndCommit();
        Collection<Commit> commits = pull();
        assertThat(commits).hasSize(1);
        Commit commit = commits.iterator().next();
        assertThat(commit.getId().getHash().length()).isEqualTo(OBJECT_ID_STRING_LENGTH);
        assertThat(commit.getId().getTeam()).isEqualTo(TEAM);
        assertThat(commit.getAuthor().getName()).isEqualTo(COMMIT_AUTHOR_NAME);
        assertThat(commit.getAuthor().getEmail()).isEqualTo(COMMIT_AUTHOR_EMAIL);
        assertThat(commit.getMessage()).isEqualTo(COMMIT_MESSAGE);
        assertThat(commit.getCreated()).is(withinMarginOfError(git.getLastCommitTime()));
        assertThat(commit.getRepositories()).extracting(repository -> repository.getRepository().getName(), CommitRepository::getBranches)
                .containsExactly(tuple(REPOSITORY_NAME, asList(MASTER)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void repository_WithCommitMergedToMultipleBranches_ShowsAllRelevantBranches() throws Exception {
        git.createFileAndCommit();
        String newBranchName = git.createAndCheckout(NEW_BRANCH);
        git.createFileAndCommit();
        git.checkout(MASTER);
        git.merge(newBranchName);
        Collection<Commit> commits = pull();
        assertThat(commits).extracting(commit -> commit.getRepositories().get(0).getBranches())
                .containsExactly(asList(MASTER, NEW_BRANCH), asList(MASTER, NEW_BRANCH));
    }

    @Test
    public void pull_WithLockedRepository_WontPullImmediately() throws Exception {
        git.createFileAndCommit();
        createIndexLockFile();
        Collection<Commit> commits = pull();
        assertThat(commits).isEmpty();
    }

    @Test
    public void pull_WithLockedRepository_WillPullAfterLockTimeout() throws Exception {
        git.createFileAndCommit();
        createIndexLockFile();
        systemTimeRule.advanceByDuration(Duration.ofMinutes(6));
        Collection<Commit> commits = pull();
        assertThat(commits).hasSize(1);
    }

    @Test
    public void pull_WithNewBranch_IncludesOnlyNewCommits() throws Exception {
        git.createFileAndCommit();
        Commit parentCommit = pull().get(0);
        git.createAndCheckout(NEW_BRANCH);
        RevCommit newCommit = git.createFileAndCommit();
        Collection<Commit> commits = pull(parentCommit);
        assertThat(commits).extracting(commit -> commit.getId().getHash()).containsExactly(newCommit.name());
    }

    @Test
    public void pull_WithRewrittenBranch_RemovesCommit() throws Exception {
        RevCommit firstCommit = git.createFileAndCommit();
        RevCommit removedCommit = git.createFileAndCommit(NEW_FILE_NAME, NEW_FILE_CONTENT);
        List<Commit> initialCommits = pull();
        git.resetToCommit(firstCommit.getName());
        PullResult pullResult = pullResult(initialCommits);
        assertThat(pullResult.getRemovedCommitIds()).extracting(CommitId::getHash).containsExactly(removedCommit.getName());
    }

    @Test
    public void pull_WithDeletedBranch_RemovesCommits() throws Exception {
        git.createFileAndCommit();
        git.createAndCheckout(NEW_BRANCH);
        RevCommit removedCommit = git.createFileAndCommit(NEW_FILE_NAME, NEW_FILE_CONTENT);
        List<Commit> initialCommits = pull();
        git.checkout(MASTER);
        git.deleteBranch(NEW_BRANCH);
        PullResult pullResult = pullResult(initialCommits);
        assertThat(pullResult.getRemovedCommitIds()).extracting(CommitId::getHash).containsExactly(removedCommit.getName());
    }

    @Test
    public void pullRepeatedly_WithRewrittenBranch_DoesntThrowMergeConflict() throws Exception {
        RevCommit firstCommit = git.createFileAndCommit();
        git.createFileAndCommit(NEW_FILE_NAME, NEW_FILE_CONTENT);
        pull();
        git.resetToCommit(firstCommit.getName());
        git.createFileAndCommit(NEW_FILE_NAME, "other-content");
        pull();
        pull();
    }

    @Test
    public void creatingRepositoryTwice_ReusesFirstRepository_AndWorks() throws Exception {
        repository = new GitRepository(repository().sourceUrl(git.getUrl()).build(), repositoryFolder, () -> PRIVATE_KEY_PASSWORD);
        git.createFileAndCommit();
        repository.pull(asList());
        List<String> branches = repository.getBranches();
        assertThat(branches).containsExactly(MASTER);
    }

    @Test
    public void creatingExistingRepositoryIntoNewFolder_FindsInterimCommit() throws Exception {
        git.createFileAndCommit();
        Commit firstCommit = pull().get(0);
        git.createFileAndCommit();
        repository = new GitRepository(repositoryMetadata, new File(temporaryFolder.getRoot(), "newFolder"), () -> PRIVATE_KEY_PASSWORD);
        git.createFileAndCommit();
        List<Commit> secondCommit = pull(firstCommit);
        assertThat(secondCommit).hasSize(2);
    }

    @Test
    public void repository_WithCommitOnOtherBranch_HasAllCommits() throws Exception {
        git.createFileAndCommit();
        git.createAndCheckout(NEW_BRANCH);
        git.createFileAndCommit();
        Collection<Commit> commits = pull();
        assertThat(commits).hasSize(2);
    }

    @Test
    public void commitWithoutParent_HasEmptyContentBefore() throws Exception {
        git.createFileAndCommit();
        pullAndAssertCommitDetail("", "", COMMITTED_FILE_NAME, COMMITTED_FILE_CONTENT);
    }

    @Test
    public void commitWithBinaryContent_CanBePulled() throws Exception {
        git.createFileAndCommit(BINARY_FILENAME, BINARY_CONTENT);
        List<Commit> commits = pull();
        assertThat(commits).hasSize(1);
    }

    @Test
    public void commitWithOtherEncoding_IsInterpretedCorrectly() throws Exception {
        git.setCharset(StandardCharsets.ISO_8859_1);
        git.createFileAndCommit(NEW_FILE_NAME, ISO_8859_1_CONTENT);
        CommitDetail commitDetail = repository.getCommitDetail(pull().get(0));
        assertThat(commitDetail.getFiles().get(0).getStateAfter().get().getContent()).isEqualTo(ISO_8859_1_CONTENT);
    }

    @Test
    public void commitWithBinaryContent_HasChecksumDescriptionAsBinaryContent() throws Exception {
        git.createFileAndCommit(BINARY_FILENAME, BINARY_CONTENT);
        CommitDetail commitDetail = repository.getCommitDetail(pull().get(0));
        assertThat(commitDetail.getFiles()).hasSize(1);
        CommitFile file = commitDetail.getFiles().get(0);
        assertBinaryState(file.getStateBefore(), "", "");
        assertBinaryState(file.getStateAfter(), BINARY_FILENAME, "Binary file content with MD5 checksum 75dae7103cb68ad5bfbf30b1a25565c7");
    }

    @Test
    public void commitWithWhitespaceContent_HasRegularWhitespaceContent() throws Exception {
        git.createFileAndCommit(BINARY_FILENAME, WHITESPACE_CONTENT);
        pullAndAssertCommitDetail("", "", BINARY_FILENAME, WHITESPACE_CONTENT);
    }

    @Test
    public void commitWithParent_WithSameFile_HasParentContentBefore() throws Exception {
        Commit firstCommit = toCommit(git.createFileAndCommit());
        git.createFileAndCommit(COMMITTED_FILE_NAME, NEW_FILE_CONTENT);
        pullAndAssertCommitDetail(firstCommit, COMMITTED_FILE_NAME, COMMITTED_FILE_CONTENT, COMMITTED_FILE_NAME, NEW_FILE_CONTENT);
    }

    @Test
    public void commitWithParent_WithSameFileMoved_HasParentContentBefore() throws Exception {
        Commit firstCommit = toCommit(git.createFileAndCommit());
        git.createFileAndCommit(COMMITTED_FILE_NAME, NEW_FILE_CONTENT);
        pullAndAssertCommitDetail(firstCommit, COMMITTED_FILE_NAME, COMMITTED_FILE_CONTENT, COMMITTED_FILE_NAME, NEW_FILE_CONTENT);
    }

    @Test
    public void commitWithParent_WithNewFile_HasEmptyContentBefore() throws Exception {
        Commit parentCommit = toCommit(git.createFileAndCommit());
        git.moveFile(COMMITTED_FILE_NAME, NEW_FILE_NAME);
        git.removeFromIndex(COMMITTED_FILE_NAME);
        git.createCommitAndAdd(NEW_FILE_NAME);
        pullAndAssertCommitDetail(parentCommit, COMMITTED_FILE_NAME, COMMITTED_FILE_CONTENT, NEW_FILE_NAME, COMMITTED_FILE_CONTENT);
    }

    @Test
    public void commitWithParent_WithDeletedFile_HasEmptyContentAfter() throws Exception {
        Commit firstCommit = toCommit(git.createFileAndCommit());
        git.removeFromIndex(COMMITTED_FILE_NAME);
        git.createCommit();
        pullAndAssertCommitDetail(firstCommit, COMMITTED_FILE_NAME, COMMITTED_FILE_CONTENT, "", "");
    }

    @Test
    public void commitWithSubModuleChange_OnFirstCommit_DoesNotShowSubModuleChanges() throws Exception {
        git.addSubModule();
        git.createFileAndCommit();
        Commit commit = pull().get(0);
        repository.getCommitDetail(commit);
    }

    @Test
    public void commitWithSubModuleChange_OnSubsequentCommit_DoesNotShowSubModuleChanges() throws Exception {
        git.createFileAndCommit();
        List<Commit> firstCommits = pull();
        git.addSubModule();
        git.createFileAndCommit();
        Commit commit = pull(firstCommits).get(0);
        repository.getCommitDetail(commit);
    }

    @Test
    public void remove_DeletesRepositoryFolder() throws Exception {
        git.createFileAndCommit();
        repository.remove();
        assertThat(repositoryFolder.exists()).isFalse();
    }

    @Test
    public void newlyCreatedRepository_FirstPull_HasCommitsFromAllBranches() throws Exception {
        git.createFileAndCommit();
        git.createAndCheckout(NEW_BRANCH);
        git.createFileAndCommit();
        repository = new GitRepository(repository().sourceUrl(git.getUrl()).build(), new File(temporaryFolder.getRoot(), "otherRepository"),
                () -> PRIVATE_KEY_PASSWORD);
        Collection<Commit> newCommits = pull();
        assertThat(newCommits).hasSize(2);
    }

    @Test
    public void remoteRepository_WithPublicKey_CanBeConnected() throws Exception {
        repositoryMetadata = repository().sourceUrl("git@bitbucket.org:benromberg/integration-test.git")
                .team(team().keyPair(new TeamKeyPair(PRIVATE_KEY, "public-key")).build())
                .build();
        repository = new GitRepository(repositoryMetadata, new File(temporaryFolder.getRoot(), "newFolder"), () -> PRIVATE_KEY_PASSWORD);
    }

    @Test
    public void remoteRepository_WithPublicKey_CanBePulledFrom() throws Exception {
        repositoryMetadata = repository().sourceUrl("git@bitbucket.org:benromberg/integration-test.git")
                .team(team().keyPair(new TeamKeyPair(PRIVATE_KEY, "public-key")).build())
                .build();
        repository = new GitRepository(repositoryMetadata, new File(temporaryFolder.getRoot(), "newFolder"), () -> PRIVATE_KEY_PASSWORD);
        repository.pull(emptyList());
    }

    private void createIndexLockFile() throws IOException {
        new File(new File(repositoryFolder, ".git"), "index.lock").createNewFile();
    }

    private List<Commit> pull(Commit... existingCommits) {
        return pull(asList(existingCommits));
    }

    private List<Commit> pull(List<Commit> existingCommits) {
        return repository.pull(existingCommits).getNewCommits().stream().map(CommitWithRepository::getCommit).collect(toList());
    }

    private PullResult pullResult(List<Commit> existingCommits) {
        return repository.pull(existingCommits);
    }

    private Commit toCommit(RevCommit commit) {
        return commit().id(commit.name()).build();
    }

    private void pullAndAssertCommitDetail(Commit parentCommit, String pathBefore, String contentBefore, String pathAfter,
            String contentAfter) {
        Collection<Commit> commits = pull(parentCommit);
        assertCommitDetail(commits.iterator().next(), pathBefore, contentBefore, pathAfter, contentAfter);
    }

    private void pullAndAssertCommitDetail(String pathBefore, String contentBefore, String pathAfter, String contentAfter) {
        Collection<Commit> commits = pull();
        assertThat(commits).hasSize(1);
        assertCommitDetail(commits.iterator().next(), pathBefore, contentBefore, pathAfter, contentAfter);
    }

    private void assertCommitDetail(Commit commit, String pathBefore, String contentBefore, String pathAfter, String contentAfter) {
        CommitDetail commitDetail = repository.getCommitDetail(commit);
        assertThat(commitDetail.getCommit().getMessage()).isEqualTo(COMMIT_MESSAGE);
        assertThat(commitDetail.getFiles()).hasSize(1);
        assertCommitFile(commitDetail.getFiles().get(0), pathBefore, contentBefore, pathAfter, contentAfter);
    }

    private void assertCommitFile(CommitFile commitFile, String pathBefore, String contentBefore, String pathAfter, String contentAfter) {
        assertState(commitFile.getStateBefore(), pathBefore, contentBefore);
        assertState(commitFile.getStateAfter(), pathAfter, contentAfter);
    }

    private void assertState(Optional<CommitFileState> state, String path, String content) {
        if (path.isEmpty()) {
            assertThat(state).isEmpty();
            return;
        }
        assertThat(state.get().isBinary()).isFalse();
        assertThat(state.get().getPath()).isEqualTo(path);
        assertThat(state.get().getContent()).isEqualTo(content);
    }

    private void assertBinaryState(Optional<CommitFileState> state, String path, String content) {
        if (path.isEmpty()) {
            assertThat(state).isEmpty();
            return;
        }
        assertThat(state.get().isBinary()).isTrue();
        assertThat(state.get().getPath()).isEqualTo(path);
        assertThat(state.get().getContent()).isEqualTo(content);
    }
}
