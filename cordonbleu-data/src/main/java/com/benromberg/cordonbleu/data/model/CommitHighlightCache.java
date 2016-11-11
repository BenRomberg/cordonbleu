package com.benromberg.cordonbleu.data.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitHighlightCache extends Entity<CommitId> {
    public static final String COMMENTS_PROPERTY = "comments";

    @JsonProperty
    private final int version;

    @JsonProperty
    private final List<CommitHighlightCacheFile> files;

    @JsonProperty
    private final CommitHighlightCacheText message;

    @JsonProperty(COMMENTS_PROPERTY)
    private final Map<String, CommitHighlightCacheText> comments;

    @JsonCreator
    public CommitHighlightCache(CommitId id, int version, List<CommitHighlightCacheFile> files,
            CommitHighlightCacheText message, Map<String, CommitHighlightCacheText> comments) {
        super(id);
        this.version = version;
        this.files = files;
        this.message = message;
        this.comments = comments;
    }

    public int getVersion() {
        return version;
    }

    public List<CommitHighlightCacheFile> getFiles() {
        return files;
    }

    public CommitHighlightCacheText getMessage() {
        return message;
    }

    public Map<String, CommitHighlightCacheText> getComments() {
        return comments;
    }

}
