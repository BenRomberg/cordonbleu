package com.benromberg.cordonbleu.service.highlight.plugin;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import com.benromberg.cordonbleu.data.model.User;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.parboiled.Rule;
import org.pegdown.Parser;
import org.pegdown.plugins.InlinePluginParser;

public class UserReferenceParser extends Parser implements InlinePluginParser {
    // fields need to be protected because Parboiled creates a subclass at runtime that needs access
    protected Object[] users;
    protected Map<String, User> userMap;

    public UserReferenceParser(List<User> users) {
        super(ALL, 1000L, DefaultParseRunnerProvider);
        this.users = getUserNamesWithLongestFirst(users).toArray();
        this.userMap = users.stream().collect(toMap(User::getName, user -> user));
    }

    private List<String> getUserNamesWithLongestFirst(List<User> users) {
        List<String> userNameList = users.stream().map(User::getName).collect(toList());
        Collections.sort(userNameList, (name1, name2) -> name2.length() - name1.length());
        return userNameList;
    }

    @Override
    public Rule[] inlinePluginRules() {
        if (users.length == 0) {
            return new Rule[] {};
        }
        return new Rule[] { userReference() };
    }

    public Rule userReference() {
        return NodeSequence(Sequence("@", FirstOf(users), push(new UserReferenceNode(userMap.get(match())))));
    }

}
