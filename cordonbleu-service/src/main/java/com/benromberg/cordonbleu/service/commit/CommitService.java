package com.benromberg.cordonbleu.service.commit;

import static java.util.stream.Collectors.toList;
import com.benromberg.cordonbleu.data.dao.CommitDao;
import com.benromberg.cordonbleu.data.dao.TeamDao;
import com.benromberg.cordonbleu.data.model.Comment;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitApproval;
import com.benromberg.cordonbleu.data.model.CommitId;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.util.ClockService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

public class CommitService {
    private final CommitDao commitDao;
    private final int notificationConsiderationAmount;
    private final TeamDao teamDao;

    @Inject
    public CommitService(CommitDao commitDao, TeamDao teamDao,
            CommitNotificationConsiderationAmount notificationConsiderationAmount) {
        this.commitDao = commitDao;
        this.teamDao = teamDao;
        this.notificationConsiderationAmount = notificationConsiderationAmount
                .getCommitNotificationConsiderationAmount();
    }

    public Optional<CommitApproval> approve(CommitId commitId, User user) {
        CommitApproval approval = new CommitApproval(user, ClockService.now());
        return updateApproval(commitId, Optional.of(approval)).map(commit -> approval);
    }

    public boolean revertApproval(CommitId commitId) {
        return updateApproval(commitId, Optional.empty()).isPresent();
    }
    
    public void proposeToCollectiveReview(CommitId commitId, boolean value) {
    	commitDao.updateProposetoCollectiveReview(commitId, value);
    }

    public Optional<Commit> findById(RawCommitId rawCommitId) {
        CommitId commitId = rawCommitId.toCommitId(teamDao);
        return commitDao.findById(commitId);
    }

    private Optional<Commit> updateApproval(CommitId commitId, Optional<CommitApproval> approval) {
        return commitDao.updateApproval(commitId, approval);
    }

    public CommitNotifications findNotifications(User user, int limit) {
        int consideredLimit = Math.max(notificationConsiderationAmount, limit);
        List<Commit> notificationCommits = commitDao.findNotifications(user, consideredLimit);
        List<CommitNotification> notifications = notificationCommits.stream()
                .map(commit -> mapCommitToNotification(commit, user)).collect(toList());
        int totalAmount = (int) notifications.stream().filter(CommitNotification::isPrompt).count();
        return new CommitNotifications(totalAmount, notifications.stream().limit(limit).collect(toList()));
    }

    private CommitNotification mapCommitToNotification(Commit commit, User user) {
        List<String> userEmails = Stream.concat(Stream.of(user.getEmail()), user.getEmailAliases().stream())
                .map(email -> email.toLowerCase()).collect(toList());
        boolean isOwnCommit = userEmails.contains(commit.getAuthor().getEmail().toLowerCase());
        Comment lastComment = commit.getComments().get(commit.getComments().size() - 1);
        boolean prompt = !commit.getApproval().isPresent() && (isOwnCommit || !lastComment.getUser().equals(user));
        CommitNotificationAction lastAction = commit.getApproval().map(approval -> createActionForApproval(approval))
                .orElse(createActionForComment(lastComment));
        return new CommitNotification(commit, prompt, lastAction);
    }

    private CommitNotificationAction createActionForComment(Comment lastComment) {
        return new CommitNotificationAction(lastComment.getUser(), CommitNotificationActionType.COMMENT,
                lastComment.getCreated());
    }

    private CommitNotificationAction createActionForApproval(CommitApproval approval) {
        return new CommitNotificationAction(approval.getApprover(), CommitNotificationActionType.APPROVE,
                approval.getTime());
    }
}
