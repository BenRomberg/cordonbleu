package com.benromberg.cordonbleu.service.comment;

import com.benromberg.cordonbleu.data.model.Comment;
import com.benromberg.cordonbleu.data.model.User;

import java.util.List;

public class EmailComment {
    private boolean highlighted;
    private Comment comment;
    private String htmlText;
    private List<User> referencedUsers;

    public EmailComment(Comment comment, String htmlText, boolean highlighted, List<User> referencedUsers) {
        this.htmlText = htmlText;
        this.highlighted = highlighted;
        this.comment = comment;
        this.referencedUsers = referencedUsers;
    }

    public List<User> getReferencedUsers() {
        return referencedUsers;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public String getText() {
        return comment.getText();
    }

    public String getHtmlText() {
        return htmlText;
    }

    public User getUser() {
        return comment.getUser();
    }
}
