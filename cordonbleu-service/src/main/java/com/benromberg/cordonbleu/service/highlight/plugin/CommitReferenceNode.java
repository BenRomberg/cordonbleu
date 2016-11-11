package com.benromberg.cordonbleu.service.highlight.plugin;

import java.util.List;

import org.pegdown.ast.AbstractNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.Visitor;

public class CommitReferenceNode extends AbstractNode {
    private String commitHash;

    public CommitReferenceNode(String commitHash) {
        this.commitHash = commitHash;
    }

    public String getCommitHash() {
        return commitHash;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public List<Node> getChildren() {
        return null;
    }

}
