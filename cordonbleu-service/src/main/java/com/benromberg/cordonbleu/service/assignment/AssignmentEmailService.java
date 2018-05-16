package com.benromberg.cordonbleu.service.assignment;

import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitId;
import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.service.email.EmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.function.Function;

import javax.inject.Inject;

public class AssignmentEmailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AssignmentEmailService.class);
    private final EmailService emailService;

    @Inject
    public AssignmentEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void sendSingleAssignmentEmail(Commit commit, User assignedTo, User assignedBy) {
        String receiver = assignedTo.getEmail();
        LOGGER.info("Queuing email for assigning commit {} to {}.", commit.getId().getHash(), receiver);

        emailService.queueEmail(Collections.singleton(receiver), assignedBy,
                new SingleAssignmentEmailTemplate(commit, assignedBy, getCommitPath(commit.getId())));
    }

    public void sendBatchAssignmentEmail(CommitBatchAssignment batchAssignment, User assignedBy) {
        String receiver = batchAssignment.getAssignee().getEmail();
        LOGGER.info("Queuing email for assigning commit of {} to {}.", batchAssignment.getCommitAuthor().getEmail(), receiver);

        emailService.queueEmail(Collections.singleton(receiver), assignedBy,
                new BatchAssignmentEmailTemplate(batchAssignment, this::getCommitPath, assignedBy));
    }

    private String getCommitPath(CommitId commitId) {
        return getCommitPathResolver(commitId.getTeam()).apply(commitId.getHash());
    }

    private Function<String, String> getCommitPathResolver(Team team) {
        return commitHash -> emailService.getRootPath() + "/team/" + team.getName() + "/commit/" + commitHash;
    }
}
