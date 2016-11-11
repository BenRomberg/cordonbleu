package com.benromberg.cordonbleu.service.comment;

import com.benromberg.cordonbleu.data.model.Comment;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitFilePath;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

import org.stringtemplate.v4.ST;

import com.benromberg.cordonbleu.service.diff.DiffStatus;
import com.benromberg.cordonbleu.service.diff.DiffViewCodeLine;
import com.benromberg.cordonbleu.service.email.EmailTemplate;

public class CommentEmailTemplate extends EmailTemplate {
    private final Commit commit;
    private final Comment comment;
    private final List<String> plainLinesBefore;
    private final List<String> plainLinesAfter;
    private final List<DiffViewCodeLine> htmlLinesBefore;
    private final List<DiffViewCodeLine> htmlLinesAfter;
    private final List<EmailComment> comments;
    private final String commitPath;

    public CommentEmailTemplate(Commit commit, Comment comment, List<EmailComment> comments,
            List<String> plainLinesBefore, List<String> plainLinesAfter, List<DiffViewCodeLine> htmlLinesBefore,
            List<DiffViewCodeLine> htmlLinesAfter, String commitPath) {
        super("comment.stg");
        this.commit = commit;
        this.comment = comment;
        this.plainLinesBefore = plainLinesBefore;
        this.plainLinesAfter = plainLinesAfter;
        this.htmlLinesBefore = htmlLinesBefore;
        this.htmlLinesAfter = htmlLinesAfter;
        this.comments = comments;
        this.commitPath = commitPath;
        registerRenderer(DiffStatus.class,
                (object, format, locale) -> ((DiffStatus) object).toString().toLowerCase(locale));
    }

    @Override
    protected ST getSubjectTemplate() {
        return injectCommonAttributes(super.getSubjectTemplate()).add("comment", comment).add("commitMessageFirstLine",
                getFirstLine(commit.getMessage()));
    }

    private String getFirstLine(String message) {
        return new BufferedReader(new StringReader(message)).lines().findFirst().orElse("");
    }

    @Override
    protected ST getPlainBodyTemplate() {
        return injectCommonAttributes(super.getPlainBodyTemplate()).add("path", getPlainPath())
                .add("linesBefore", plainLinesBefore).add("linesAfter", plainLinesAfter);
    }

    private String getPlainPath() {
        CommitFilePath commitFilePath = comment.getCommitFilePath();
        if (!commitFilePath.getBeforePath().isPresent()) {
            return "+ " + commitFilePath.getAfterPath().get();
        }
        if (!commitFilePath.getAfterPath().isPresent()) {
            return "- " + commitFilePath.getBeforePath().get();
        }
        if (commitFilePath.getBeforePath().equals(commitFilePath.getAfterPath())) {
            return commitFilePath.getBeforePath().get();
        }
        return commitFilePath.getBeforePath().get() + " -> " + commitFilePath.getAfterPath().get();
    }

    @Override
    protected ST getHtmlBodyTemplate() {
        return injectCommonAttributes(super.getHtmlBodyTemplate()).add("path", getPlainPath())
                .add("linesBefore", htmlLinesBefore).add("linesAfter", htmlLinesAfter);
    }

    private ST injectCommonAttributes(ST template) {
        return template.add("commit", commit).add("comments", comments).add("commitPath", commitPath);
    }

}
