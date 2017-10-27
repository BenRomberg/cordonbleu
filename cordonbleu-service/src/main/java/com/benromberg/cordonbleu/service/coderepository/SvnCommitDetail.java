package com.benromberg.cordonbleu.service.coderepository;

import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;
import com.benromberg.cordonbleu.data.model.Commit;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.wc2.ng.SvnDiffGenerator;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc2.SvnDiff;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

public class SvnCommitDetail {
    private static final int BINARY_FILE_CONTROL_CHARACTER_FACTOR = 100;
    private static final String BINARY_FILE_REPLACEMENT = "Binary file content with MD5 checksum %s";
    private static final int BINARY_ANALYSIS_LIMIT = 100000;

    private final SVNRepository repository;
    private final CodeRepositoryMetadata repositoryMetadata;

    public SvnCommitDetail(CodeRepositoryMetadata repositoryMetadata, SVNRepository repository) {
        this.repositoryMetadata = repositoryMetadata;
        this.repository = repository;
    }

    public CommitDetail getCommitDetail(Commit commit) {
        try {
            String[] paths = {""};
            long revision = getRevisionFromCommit(commit);

            Collection<SVNLogEntry> logEntries = repository.log(paths, null, revision, revision, true, true);

            return new CommitDetail(commit, getCommitFiles(commit, logEntries));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private long getRevisionFromCommit(Commit commit) {
        return Long.parseLong(commit.getId().getHash().split("-")[0]);
    }

    private List<CommitFile> getCommitFiles(Commit commit, Collection<SVNLogEntry> logEntries) throws IOException, SVNException {
        List<CommitFile> files = new ArrayList<>();
        long revision = getRevisionFromCommit(commit);

        for (final SVNLogEntry logEntry : logEntries) {
                Set changedPathsSet = logEntry.getChangedPaths().keySet();
                for (Iterator changedPaths = changedPathsSet.iterator(); changedPaths.hasNext(); ) {
                    SVNLogEntryPath entryPath = logEntry.getChangedPaths().get(changedPaths.next());

                    getCommitFileFromDiff(entryPath, revision).ifPresent(files::add);
                }
            }

        return files;
    }


    private Optional<CommitFileContent> getFileContent(String path, long revision) throws IOException, SVNException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        repository.getFile(path, revision, null, os);

        CommitFileContent filecontent;

        if (isBinary(os.toByteArray())) {
            filecontent = CommitFileContent.ofBinary(createBinaryContent(os.toString()));
        } else {
            filecontent = CommitFileContent.ofSource(os.toString());
        }

        return  Optional.of(filecontent);
    }

    private SvnDiff getDiff(SVNLogEntryPath entryPath, long revision) throws SVNException  {
        // diff part
        final SvnOperationFactory svnOperationFactory = new SvnOperationFactory();

        final SVNRevision startRevision = SVNRevision.create(10581);
        final SVNRevision endRevision = SVNRevision.create(revision);

        final SVNURL fileUrl = SVNURL.parseURIEncoded(repository.getRepositoryRoot(true) + entryPath.getPath());

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final SvnDiffGenerator diffGenerator = new SvnDiffGenerator();
        diffGenerator.setBasePath(new File(""));

        final SvnDiff diff = svnOperationFactory.createDiff();
        diff.setSource(SvnTarget.fromURL(fileUrl, startRevision), startRevision, endRevision);
        diff.setOutput(byteArrayOutputStream);
        diff.setDiffGenerator(diffGenerator);
        diff.run();

        final String actualDiffOutput = new String(byteArrayOutputStream.toByteArray()).replace(System.getProperty("line.separator"), "\n");

        return diff;
    }

    private Optional<CommitFile> getCommitFileFromDiff(SVNLogEntryPath entryPath, long revision) throws IOException, SVNException {
        if (entryPath.getType() == SVNLogEntryPath.TYPE_ADDED) {
            return getFileContent(entryPath.getPath(), revision).map(
                content -> CommitFile.added(entryPath.getPath(), content));
        } else if (entryPath.getType() == SVNLogEntryPath.TYPE_DELETED) {
            return getFileContent(entryPath.getPath(), revision-1).map(
                content -> CommitFile.removed(entryPath.getPath(), content));
        }


        Optional<CommitFileContent> contentBefore = getFileContent(entryPath.getPath(), revision-1);
        Optional<CommitFileContent> contentAfter = getFileContent(entryPath.getPath(), revision);
        if (!contentBefore.isPresent() || !contentAfter.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(CommitFile.changed(entryPath.getPath(), entryPath.getPath(), contentBefore.get(),
                contentAfter.get()));
    }

    private String createBinaryContent(String binaryContent) throws IOException {
        return String.format(BINARY_FILE_REPLACEMENT, ChecksumUtil.stringToMd5HexChecksum(binaryContent));
    }

    private boolean isBinary(byte[] analysisBytes) {
        long binaryChars = IntStream.range(0, analysisBytes.length).map(i -> analysisBytes[i])
                .filter(sourceChar -> Character.isISOControl(sourceChar) && !Character.isWhitespace(sourceChar))
                .count();
        long totalChars = Math.min(analysisBytes.length, BINARY_ANALYSIS_LIMIT);
        return binaryChars * BINARY_FILE_CONTROL_CHARACTER_FACTOR > totalChars;
    }
}
