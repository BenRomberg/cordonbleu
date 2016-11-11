package com.benromberg.cordonbleu.service.comment;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import com.benromberg.cordonbleu.data.model.Comment;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitId;
import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.model.User;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;

import javax.inject.Inject;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.benromberg.cordonbleu.service.commit.CommitHighlightService;
import com.benromberg.cordonbleu.service.commit.HighlightedCommit;
import com.benromberg.cordonbleu.service.commit.HighlightedCommitFile;
import com.benromberg.cordonbleu.service.diff.DiffViewCodeLine;
import com.benromberg.cordonbleu.service.diff.DiffViewService;
import com.benromberg.cordonbleu.service.email.EmailService;
import com.benromberg.cordonbleu.service.highlight.TextHighlightResult;
import com.benromberg.cordonbleu.service.highlight.TextHighlightService;
import com.benromberg.cordonbleu.service.user.UserService;

public class CommentEmailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentEmailService.class);
    private static final int DEFAULT_EMAIL_NOTIFICATION_CONTEXT_LINES = 4;

    private final EmailService emailService;
    private final UserService userService;
    private final DiffViewService diffViewService;
    private final int emailNotificationContextLines;
    private final CommitHighlightService commitHighlightService;
    private final TextHighlightService commentHighlightService;

    @Inject
    public CommentEmailService(EmailService emailService, UserService userService, DiffViewService diffViewService,
            TextHighlightService commentHighlightService, CommitHighlightService commitHighlightService) {
        this(emailService, userService, diffViewService, commentHighlightService, commitHighlightService,
                DEFAULT_EMAIL_NOTIFICATION_CONTEXT_LINES);
    }

    public CommentEmailService(EmailService emailService, UserService userService, DiffViewService diffViewService,
            TextHighlightService commentHighlightService, CommitHighlightService commitHighlightService,
            int emailNotificationContextLines) {
        this.emailService = emailService;
        this.userService = userService;
        this.diffViewService = diffViewService;
        this.commentHighlightService = commentHighlightService;
        this.commitHighlightService = commitHighlightService;
        this.emailNotificationContextLines = emailNotificationContextLines;
    }

    public void sendNotificationEmail(Commit commit, Comment comment) {
        List<EmailComment> emailComments = extractEmailComments(commit, comment);
        Set<String> receivers = getEmailReceivers(commit, emailComments);
        LOGGER.info("Queuing email for commit {} and comment from {} to {}.", commit.getId().getHash(), comment
                .getUser().getEmail(), receivers);
        List<DiffViewCodeLine> codeLines = getCodeLines(commit, comment);
        int commentLine = getCommentLine(codeLines, comment);
        emailService.queueEmail(receivers, comment.getUser(), new CommentEmailTemplate(commit, comment, emailComments,
                getPlainLinesBefore(codeLines, commentLine), getPlainLinesAfter(codeLines, commentLine),
                getHtmlLinesBefore(codeLines, commentLine), getHtmlLinesAfter(codeLines, commentLine),
                getCommitPath(commit.getId())));
    }

    private String getCommitPath(CommitId commitId) {
        return getCommitPathResolver(commitId.getTeam()).apply(commitId.getHash());
    }

    private Function<String, String> getCommitPathResolver(Team team) {
        return commitHash -> emailService.getRootPath() + "/team/" + team.getName() + "/commit/" + commitHash;
    }

    private List<EmailComment> extractEmailComments(Commit commit, Comment comment) {
        List<EmailComment> emailComments = commit
                .getComments()
                .stream()
                .filter(otherComment -> otherComment.getCommitFilePath().equals(comment.getCommitFilePath())
                        && otherComment.getCommitLineNumber().equals(comment.getCommitLineNumber()))
                .map(otherComment -> createEmailComment(commit, otherComment, false)).collect(toList());
        emailComments.add(createEmailComment(commit, comment, true));
        return emailComments;
    }

    private EmailComment createEmailComment(Commit commit, Comment comment, boolean highlighted) {
        TextHighlightResult highlightResult = commentHighlightService.markdownToHtml(comment.getText(),
                getCommitPathResolver(commit.getId().getTeam()));
        return new EmailComment(comment, highlightResult.getText(), highlighted, highlightResult.getReferencedUsers());
    }

    private List<DiffViewCodeLine> getHtmlLinesAfter(List<DiffViewCodeLine> codeLines, int commentLine) {
        return getHtmlLines(codeLines, commentLine + 1, commentLine + emailNotificationContextLines);
    }

    private List<DiffViewCodeLine> getHtmlLinesBefore(List<DiffViewCodeLine> codeLines, int commentLine) {
        return getHtmlLines(codeLines, commentLine - emailNotificationContextLines + 1, commentLine);
    }

    private List<DiffViewCodeLine> getHtmlLines(List<DiffViewCodeLine> codeLines, int startLine, int endLine) {
        Set<Integer> relevantLines = IntStream.rangeClosed(startLine, endLine).boxed().collect(toSet());
        return IntStream.range(0, codeLines.size()).filter(index -> relevantLines.contains(index))
                .mapToObj(index -> codeLines.get(index)).collect(toList());
    }

    private int getCommentLine(List<DiffViewCodeLine> codeLines, Comment comment) {
        int commentLine = IntStream.range(0, codeLines.size())
                .filter(index -> codeLines.get(index).getCommitLineNumber().equals(comment.getCommitLineNumber()))
                .findFirst().getAsInt();
        return commentLine;
    }

    private List<DiffViewCodeLine> getCodeLines(Commit commit, Comment comment) {
        HighlightedCommit commitDetail = commitHighlightService.highlight(commit);
        HighlightedCommitFile commitFile = commitDetail.getFiles().stream()
                .filter(file -> file.getPath().equals(comment.getCommitFilePath())).findFirst().get();
        List<DiffViewCodeLine> codeLines = diffViewService.diffCodeLines(commitFile);
        return codeLines;
    }

    private List<String> getPlainLinesAfter(List<DiffViewCodeLine> codeLines, int commentLine) {
        return stripHtml(getHtmlLinesAfter(codeLines, commentLine));
    }

    private List<String> stripHtml(List<DiffViewCodeLine> htmlLines) {
        return htmlLines
                .stream()
                .map(htmlLine -> StringEscapeUtils.unescapeHtml4(htmlLine.getHighlightedCode().replaceAll("<.*?>", "")))
                .collect(toList());
    }

    private List<String> getPlainLinesBefore(List<DiffViewCodeLine> codeLines, int commentLine) {
        return stripHtml(getHtmlLinesBefore(codeLines, commentLine));
    }

    private Set<String> getEmailReceivers(Commit commit, List<EmailComment> emailComments) {
        Set<String> receivers = commit.getComments().stream().map(otherComment -> otherComment.getUser().getEmail())
                .collect(toSet());
        List<User> usersWithCommitAuthorEmail = userService.findUserByEmailOrAlias(commit.getAuthor().getEmail());
        receivers.addAll(usersWithCommitAuthorEmail.stream().map(user -> user.getEmail()).collect(toList()));
        emailComments.stream().filter(EmailComment::isHighlighted).forEach(emailComment -> {
            receivers.addAll(emailComment.getReferencedUsers().stream().map(User::getEmail).collect(toList()));
            receivers.remove(emailComment.getUser().getEmail());
        });
        return receivers;
    }
}
