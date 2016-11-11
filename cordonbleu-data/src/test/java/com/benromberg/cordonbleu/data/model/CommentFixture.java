package com.benromberg.cordonbleu.data.model;

import java.util.Optional;

import com.benromberg.cordonbleu.data.model.Comment;
import com.benromberg.cordonbleu.data.model.CommitFilePath;
import com.benromberg.cordonbleu.data.model.CommitLineNumber;
import com.benromberg.cordonbleu.data.model.User;

public interface CommentFixture {
    CommitLineNumber COMMENT_LINE_NUMBER = new CommitLineNumber(Optional.of(1), Optional.of(1));
    CommitFilePath COMMENT_FILE_PATH = new CommitFilePath(Optional.of("before.path"), Optional.of("after.path"));
    String COMMENT_TEXT = "comment text";
    String COMMENT_USER_PASSWORD = "comment-user-password";
    String COMMENT_USER_NAME = "comment-user";
    String COMMENT_USER_EMAIL = "comment@email.com";
    User COMMENT_USER = new User(COMMENT_USER_EMAIL, COMMENT_USER_NAME, COMMENT_USER_PASSWORD);
    Comment COMMENT = new CommentBuilder().build();
    String COMMENT_ID = COMMENT.getId();

    default CommentBuilder comment() {
        return new CommentBuilder();
    }

    class CommentBuilder {
        private User user = COMMENT_USER;
        private String text = COMMENT_TEXT;
        private CommitFilePath commitFilePath = COMMENT_FILE_PATH;
        private CommitLineNumber commitLineNumber = COMMENT_LINE_NUMBER;

        public CommentBuilder user(User user) {
            this.user = user;
            return this;
        }

        public CommentBuilder text(String text) {
            this.text = text;
            return this;
        }

        public CommentBuilder commitFilePath(CommitFilePath commitFilePath) {
            this.commitFilePath = commitFilePath;
            return this;
        }

        public CommentBuilder commitLineNumber(CommitLineNumber commitLineNumber) {
            this.commitLineNumber = commitLineNumber;
            return this;
        }

        public Comment build() {
            return new Comment(this.user, this.text, this.commitFilePath, this.commitLineNumber);
        }
    }
}
