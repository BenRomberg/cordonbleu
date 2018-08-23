package com.benromberg.cordonbleu.service.coderepository;

import static com.benromberg.cordonbleu.util.CollectionUtil.toStream;
import static com.benromberg.cordonbleu.util.ExceptionUtil.convertException;
import static java.util.stream.Collectors.toList;
import com.benromberg.cordonbleu.data.model.Commit;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import com.ibm.icu.text.CharsetDetector;

public class GitCommitDetail {
    private static final int BINARY_FILE_CONTROL_CHARACTER_FACTOR = 100;
    private static final String BINARY_FILE_REPLACEMENT = "Binary file content with MD5 checksum %s";
    private static final int BINARY_ANALYSIS_LIMIT = 100_000_000;

    private final Repository repository;

    public GitCommitDetail(Repository repository) {
        this.repository = repository;
    }

    public CommitDetail getCommitDetail(Commit commit) {
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit revCommit = convertException(() -> walk.parseCommit(repository.resolve(commit.getId().getHash())));
            return new CommitDetail(commit, getCommitFiles(walk, revCommit));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<CommitFile> getCommitFiles(RevWalk walk, RevCommit revCommit) throws IOException {
        if (revCommit.getParentCount() == 0) {
            return getFilesFromRootCommit(revCommit);
        }
        return getFilesDifferentFromParentCommit(walk, revCommit);
    }

    private List<CommitFile> getFilesDifferentFromParentCommit(RevWalk walk, RevCommit revCommit) throws IOException {
        RevCommit parentCommit = walk.parseCommit(revCommit.getParent(0).getId());
        List<DiffEntry> diffs = getCommitDiffs(revCommit, parentCommit);
        return diffs.stream().flatMap(diff -> toStream(getCommitFileFromDiff(diff))).collect(toList());
    }

    private Optional<CommitFile> getCommitFileFromDiff(DiffEntry diff) {
        if (diff.getChangeType() == ChangeType.ADD) {
            return getFileContent(diff.getNewId().toObjectId()).map(
                    content -> CommitFile.added(diff.getNewPath(), content));
        }
        if (diff.getChangeType() == ChangeType.DELETE) {
            return getFileContent(diff.getOldId().toObjectId()).map(
                    content -> CommitFile.removed(diff.getOldPath(), content));
        }
        Optional<CommitFileContent> contentBefore = getFileContent(diff.getOldId().toObjectId());
        Optional<CommitFileContent> contentAfter = getFileContent(diff.getNewId().toObjectId());
        if (!contentBefore.isPresent() || !contentAfter.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(CommitFile.changed(diff.getOldPath(), diff.getNewPath(), contentBefore.get(),
                contentAfter.get()));
    }

    private Optional<CommitFileContent> getFileContent(ObjectId objectId) {
        if (!repository.hasObject(objectId)) {
            return Optional.empty();
        }
        return Optional.of(convertException(() -> createFileContent(objectId)));
    }

    private CommitFileContent createFileContent(ObjectId objectId) throws IOException {
        ObjectLoader objectLoader = repository.open(objectId);
        if (isBinary(objectLoader.getCachedBytes(BINARY_ANALYSIS_LIMIT))) {
            return CommitFileContent.ofBinary(createBinaryContent(objectLoader));
        }
        CharsetDetector charsetDetector = new CharsetDetector();
        return CommitFileContent.ofSource(charsetDetector.getString(objectLoader.getCachedBytes(), null));
    }

    private String createBinaryContent(ObjectLoader objectLoader) throws IOException {
        try (InputStream inputStream = objectLoader.openStream()) {
            return String.format(BINARY_FILE_REPLACEMENT, ChecksumUtil.inputStreamToMd5HexChecksum(inputStream));
        }
    }

    private boolean isBinary(byte[] analysisBytes) {
        long binaryChars = IntStream.range(0, analysisBytes.length).map(i -> analysisBytes[i])
                .filter(sourceChar -> Character.isISOControl(sourceChar) && !Character.isWhitespace(sourceChar))
                .count();
        long totalChars = Math.min(analysisBytes.length, BINARY_ANALYSIS_LIMIT);
        return binaryChars * BINARY_FILE_CONTROL_CHARACTER_FACTOR > totalChars;
    }

    private List<DiffEntry> getCommitDiffs(RevCommit revCommit, RevCommit parent) throws IOException {
        try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
            diffFormatter.setRepository(repository);
            diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
            diffFormatter.setDetectRenames(true);
            // not necessary, although JGit implementation might make this relevant
            diffFormatter.setAbbreviationLength(Constants.OBJECT_ID_STRING_LENGTH);
            return diffFormatter.scan(parent.getTree(), revCommit.getTree());
        }
    }

    private List<CommitFile> getFilesFromRootCommit(RevCommit revCommit) throws IOException {
        List<CommitFile> files = new ArrayList<>();
        try (TreeWalk treeWalk = new TreeWalk(repository)) {
            treeWalk.setRecursive(true);
            treeWalk.addTree(revCommit.getTree());
            while (treeWalk.next()) {
                ObjectId objectId = treeWalk.getObjectId(0);
                getFileContent(objectId).ifPresent(
                        content -> files.add(CommitFile.added(treeWalk.getPathString(), content)));
            }
        }
        return files;
    }
}
