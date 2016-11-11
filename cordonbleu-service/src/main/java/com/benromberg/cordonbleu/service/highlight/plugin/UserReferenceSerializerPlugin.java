package com.benromberg.cordonbleu.service.highlight.plugin;

import com.benromberg.cordonbleu.data.model.User;

import java.util.ArrayList;
import java.util.List;

import org.pegdown.Printer;
import org.pegdown.ast.Node;
import org.pegdown.ast.Visitor;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

public class UserReferenceSerializerPlugin implements ToHtmlSerializerPlugin {
    private List<User> users = new ArrayList<>();

    @Override
    public boolean visit(Node node, Visitor visitor, Printer printer) {
        if (!(node instanceof UserReferenceNode)) {
            return false;
        }
        User user = ((UserReferenceNode) node).getUser();
        users.add(user);
        printer.print("<strong>@" + user.getName() + "</strong>");
        return true;
    }

    public List<User> getUsers() {
        return users;
    }
}
