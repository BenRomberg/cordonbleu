package com.benromberg.cordonbleu.main.resource.commit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadCommitNotificationResponse {
    @JsonProperty
    private final ReadCommitNotificationCommitResponse commit;

    @JsonProperty
    private final boolean prompt;

    @JsonProperty
    private final ReadCommitNotificationActionResponse lastAction;

    @JsonCreator
    public ReadCommitNotificationResponse(ReadCommitNotificationCommitResponse commit, boolean prompt,
            ReadCommitNotificationActionResponse lastAction) {
        this.commit = commit;
        this.prompt = prompt;
        this.lastAction = lastAction;
    }

    public ReadCommitNotificationCommitResponse getCommit() {
        return commit;
    }

    public boolean isPrompt() {
        return prompt;
    }

    public ReadCommitNotificationActionResponse getLastAction() {
        return lastAction;
    }

}
