package com.benromberg.cordonbleu.service.commit;

import com.benromberg.cordonbleu.data.model.CommitHighlightCacheText;

public class HighlightedCommitMessage {
    private final String message;
    private final CommitHighlightCacheText highlightedMessage;

    public HighlightedCommitMessage(String message, CommitHighlightCacheText highlightedMessage) {
        this.message = message;
        this.highlightedMessage = highlightedMessage;
    }

    public String getMessage() {
        return message;
    }

    public String getHighlightedMessage() {
        return highlightedMessage.getText();
    }
}
