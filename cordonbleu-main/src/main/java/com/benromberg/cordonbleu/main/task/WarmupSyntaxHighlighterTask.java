package com.benromberg.cordonbleu.main.task;

import javax.inject.Inject;

import com.benromberg.cordonbleu.service.highlight.SyntaxHighlighter;

public class WarmupSyntaxHighlighterTask extends AbstractTask {
    private SyntaxHighlighter syntaxHighlighter;

    @Inject
    public WarmupSyntaxHighlighterTask(SyntaxHighlighter syntaxHighlighter) {
        this.syntaxHighlighter = syntaxHighlighter;
    }

    @Override
    public void runTask() {
        syntaxHighlighter.warmup();
    }
}
