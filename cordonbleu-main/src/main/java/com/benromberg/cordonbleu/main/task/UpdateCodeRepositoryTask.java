package com.benromberg.cordonbleu.main.task;

import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.service.coderepository.CodeRepositoryService;
import com.benromberg.cordonbleu.service.commit.CommitHighlightService;
import com.benromberg.cordonbleu.util.ClockService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateCodeRepositoryTask extends AbstractTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCodeRepositoryTask.class);
    private static final int COMMIT_AGE_TO_HIGHLIGHT_IN_DAYS = 7;

    private final CodeRepositoryService service;
    private final CommitHighlightService commitHighlightService;

    @Inject
    public UpdateCodeRepositoryTask(CodeRepositoryService service, CommitHighlightService commitHighlightService) {
        this.service = service;
        this.commitHighlightService = commitHighlightService;
    }

    @Override
    public void runTask() {
        List<Commit> commitsToHighlight = new ArrayList<>();
        service.updateRepositories(commit -> {
            if (Duration.between(commit.getCreated(), ClockService.now()).toDays() <= COMMIT_AGE_TO_HIGHLIGHT_IN_DAYS) {
                commitsToHighlight.add(commit);
            }
        });
        highlightCommits(commitsToHighlight);
    }

    private void highlightCommits(List<Commit> commitsToHighlight) {
        if (!commitsToHighlight.isEmpty()) {
            LOGGER.info("Highlighting {} commits within the last {} days.", commitsToHighlight.size(),
                    COMMIT_AGE_TO_HIGHLIGHT_IN_DAYS);
            commitsToHighlight.forEach(commit -> commitHighlightService.highlight(commit));
        }
    }
}
