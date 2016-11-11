package com.benromberg.cordonbleu.main.resource.comment;

import static java.util.stream.Collectors.toList;
import com.benromberg.cordonbleu.data.model.CommitFilePath;
import com.benromberg.cordonbleu.data.model.CommitLineNumber;
import com.benromberg.cordonbleu.service.commit.HighlightedComment;
import com.benromberg.cordonbleu.service.commit.HighlightedCommit;

import java.util.List;

public class CommentEnhancer {
    public static List<CommentResponse> extractCommentsForLine(List<CommentResponse> comments, CommitFilePath path,
            CommitLineNumber lineNumber) {
        return comments
                .stream()
                .filter(comment -> comment.getComment().getCommitFilePath().equals(path)
                        && comment.getComment().getCommitLineNumber().equals(lineNumber)).collect(toList());
    }

    public List<CommentResponse> convertComments(HighlightedCommit commit) {
        return commit.getComments().stream().map(comment -> toCommentResponse(comment)).collect(toList());
    }

    private CommentResponse toCommentResponse(HighlightedComment comment) {
        return new CommentResponse(comment);
    }
}
