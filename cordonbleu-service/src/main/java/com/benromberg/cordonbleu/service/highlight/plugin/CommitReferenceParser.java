package com.benromberg.cordonbleu.service.highlight.plugin;

import org.eclipse.jgit.lib.Constants;
import org.parboiled.Rule;
import org.pegdown.Parser;
import org.pegdown.plugins.InlinePluginParser;

public class CommitReferenceParser extends Parser implements InlinePluginParser {
    public CommitReferenceParser() {
        super(ALL, 1000L, DefaultParseRunnerProvider);
    }

    @Override
    public Rule[] inlinePluginRules() {
        return new Rule[] { commitReference() };
    }

    public Rule commitReference() {
        Rule hexCharacter = FirstOf(CharRange('0', '9'), CharRange('a', 'f'), CharRange('A', 'F'));
        return NodeSequence(NTimes(Constants.OBJECT_ID_STRING_LENGTH, hexCharacter).suppressSubnodes(),
                push(new CommitReferenceNode(match())));
    }

}
