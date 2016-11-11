package com.benromberg.cordonbleu.service.highlight;

import com.benromberg.cordonbleu.data.model.User;

import java.util.List;

public class TextHighlightResult {
    private String text;
    private List<User> referencedUsers;

    public TextHighlightResult(String text, List<User> referencedUsers) {
        this.text = text;
        this.referencedUsers = referencedUsers;
    }

    public String getText() {
        return text;
    }

    public List<User> getReferencedUsers() {
        return referencedUsers;
    }

}
