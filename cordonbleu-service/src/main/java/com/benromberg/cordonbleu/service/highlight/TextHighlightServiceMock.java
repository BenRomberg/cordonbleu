package com.benromberg.cordonbleu.service.highlight;

import java.util.function.Function;

public class TextHighlightServiceMock extends TextHighlightService {
    private final TextHighlightResult result;

    public TextHighlightServiceMock(TextHighlightResult result) {
        super(null, null);
        this.result = result;
    }

    @Override
    public TextHighlightResult markdownToHtml(String markdown) {
        return result;
    }

    @Override
    public TextHighlightResult markdownToHtml(String markdown, Function<String, String> commitPathResolver) {
        return result;
    }

    @Override
    public TextHighlightResult textToHtml(String text) {
        return result;
    }
}
