package com.benromberg.cordonbleu.service.commit;

import com.benromberg.cordonbleu.data.model.Commit;

public class CommitNotification {
    private final Commit commit;
    private final boolean prompt;
    private final CommitNotificationAction lastAction;

    public CommitNotification(Commit commit, boolean prompt, CommitNotificationAction lastAction) {
        this.commit = commit;
        this.prompt = prompt;
        this.lastAction = lastAction;
    }

    public Commit getCommit() {
        return commit;
    }

    public boolean isPrompt() {
        return prompt;
    }

    public CommitNotificationAction getLastAction() {
        return lastAction;
    }

}
