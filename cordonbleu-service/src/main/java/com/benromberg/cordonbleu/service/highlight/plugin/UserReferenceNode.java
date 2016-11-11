package com.benromberg.cordonbleu.service.highlight.plugin;

import com.benromberg.cordonbleu.data.model.User;

import java.util.List;

import org.pegdown.ast.AbstractNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.Visitor;

public class UserReferenceNode extends AbstractNode {
    private User user;

    public UserReferenceNode(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
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
