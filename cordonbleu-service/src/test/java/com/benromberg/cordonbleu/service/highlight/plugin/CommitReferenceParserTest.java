package com.benromberg.cordonbleu.service.highlight.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import com.benromberg.cordonbleu.service.highlight.plugin.CommitReferenceNode;
import com.benromberg.cordonbleu.service.highlight.plugin.CommitReferenceParser;

public class CommitReferenceParserTest {
    private static final String COMMIT_HASH = "29e927a1ae3cd89cb957c8d0408e5506d6760fc9";

    @Test
    public void matchesCommitHash() throws Exception {
        CommitReferenceParser parser = Parboiled.createParser(CommitReferenceParser.class);
        ParsingResult<?> result = new ReportingParseRunner<>(parser.commitReference()).run(COMMIT_HASH);
        assertThat(result.matched).isTrue();
        CommitReferenceNode resultNode = (CommitReferenceNode) result.resultValue;
        assertThat(resultNode.getCommitHash()).isEqualTo(COMMIT_HASH);
    }
}
