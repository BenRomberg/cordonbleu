package com.benromberg.cordonbleu.service.highlight.plugin;

import java.util.function.Function;

import org.pegdown.Printer;
import org.pegdown.ast.Node;
import org.pegdown.ast.Visitor;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

public class CommitReferenceSerializerPlugin implements ToHtmlSerializerPlugin {
    private final Function<String, String> commitUrlResolver;

    public CommitReferenceSerializerPlugin(Function<String, String> commitUrlResolver) {
        this.commitUrlResolver = commitUrlResolver;
    }

    @Override
    public boolean visit(Node node, Visitor visitor, Printer printer) {
        if (!(node instanceof CommitReferenceNode)) {
            return false;
        }
        String commitHash = ((CommitReferenceNode) node).getCommitHash();
        printer.print("<a href=\"" + commitUrlResolver.apply(commitHash) + "\" class=\"vue-enhance\">"
                + commitHash.substring(0, 6) + "</a>");
        return true;
    }

}
