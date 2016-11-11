package com.benromberg.cordonbleu.service.coderepository;

import com.benromberg.cordonbleu.data.model.CommitFixture;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import org.assertj.core.api.Condition;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class GitTestRepository implements TestRule, CommitFixture {
    public static final String COMMITTED_FILE_CONTENT = "committed file content";
    public static final String COMMITTED_FILE_NAME = "test";

    private final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Git origin;
    private LocalDateTime lastCommitTime;
    private File repositoryFolder;
    private Charset charset = StandardCharsets.UTF_8;

    @Override
    public Statement apply(Statement base, Description description) {
        return temporaryFolder.apply(applySelf(base), description);
    }

    private Statement applySelf(Statement base) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                setUpRule();
                base.evaluate();
            }
        };
    }

    private void setUpRule() throws Exception {
        repositoryFolder = temporaryFolder.getRoot();
        initAndCloneRepository();
    }

    public void changeRepositoryFolder(String folderName) throws Exception {
        repositoryFolder = temporaryFolder.newFolder(folderName);
        initAndCloneRepository();
    }

    public String createAndCheckout(String branchName) throws GitAPIException {
        return origin.checkout().setCreateBranch(true).setName(branchName).call().getName();
    }

    public void deleteBranch(String branchName) throws GitAPIException {
        origin.branchDelete().setBranchNames(branchName).setForce(true).call();
    }

    public void checkout(String branchReference) throws GitAPIException {
        origin.checkout().setName(branchReference).call();
    }

    public void merge(String otherBranchReference) throws GitAPIException, IOException {
        origin.merge().include(origin.getRepository().exactRef(otherBranchReference)).call();
    }

    private void initAndCloneRepository() throws Exception {
        origin = createTestRepository(repositoryFolder);
    }

    public String getUrl() {
        return repositoryFolder.toURI().toString();
    }

    public String getFolderName() {
        return repositoryFolder.getName();
    }

    public void addSubModule() throws Exception {
        File subModuleFolder = temporaryFolder.newFolder();
        createTestRepository(subModuleFolder);
        origin.submoduleAdd().setURI(subModuleFolder.toURI().toString()).setPath("submodule").call();
    }

    private Git createTestRepository(File repositoryDirectory) throws Exception {
        return Git.init().setDirectory(repositoryDirectory).call();
    }

    public RevCommit createFileAndCommit() throws Exception {
        lastCommitTime = LocalDateTime.now(ZoneOffset.UTC);
        return createFileAndCommit(COMMITTED_FILE_NAME, COMMITTED_FILE_CONTENT, COMMIT_MESSAGE, COMMIT_AUTHOR_NAME);
    }

    public RevCommit createFileAndCommit(String fileName, String fileContent) throws Exception {
        lastCommitTime = LocalDateTime.now(ZoneOffset.UTC);
        return createFileAndCommit(fileName, fileContent, COMMIT_MESSAGE, COMMIT_AUTHOR_NAME);
    }

    private RevCommit createFileAndCommit(String fileName, String fileContent, String message, String authorName)
            throws Exception {
        createFile(fileName, fileContent);
        return createCommitAndAdd(fileName, message, authorName);
    }

    public void createCommitAndAdd(String fileName) throws Exception {
        createCommitAndAdd(fileName, COMMIT_MESSAGE, COMMIT_AUTHOR_NAME);
    }

    public void createCommit() throws GitAPIException {
        createCommit(COMMIT_MESSAGE, COMMIT_AUTHOR_NAME);
    }

    public void removeFromIndex(String path) throws GitAPIException {
        origin.rm().addFilepattern(path).call();
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    private RevCommit createCommitAndAdd(String path, String message, String authorName) throws GitAPIException {
        origin.add().addFilepattern(path).call();
        return createCommit(message, authorName);
    }

    private RevCommit createCommit(String message, String authorName) throws GitAPIException {
        return origin.commit().setMessage(message).setAuthor(authorName, COMMIT_AUTHOR_EMAIL).call();
    }

    private void createFile(String fileName, String fileContent) throws Exception {
        File testFile = new File(repositoryFolder, fileName);
        try (PrintWriter writer = new PrintWriter(testFile, charset.toString())) {
            writer.write(fileContent);
        }
    }

    public void moveFile(String oldFileName, String newFileName) throws IOException {
        Files.move(new File(repositoryFolder, oldFileName).toPath(), new File(repositoryFolder, newFileName).toPath());
    }

    public LocalDateTime getLastCommitTime() {
        return lastCommitTime;
    }

    public static Condition<LocalDateTime> withinMarginOfError(LocalDateTime commitTime) {
        return new Condition<>(item -> ChronoUnit.SECONDS.between(commitTime, item) <= 1, "within one second of %s",
                commitTime);
    }

    public void resetToCommit(String ref) throws GitAPIException {
        origin.reset().setMode(ResetType.HARD).setRef(ref).call();
    }
}
