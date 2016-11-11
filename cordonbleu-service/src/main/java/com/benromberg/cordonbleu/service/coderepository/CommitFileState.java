package com.benromberg.cordonbleu.service.coderepository;

public class CommitFileState {
    private final String path;
    private final boolean binary;
    private final String content;

    public CommitFileState(String path, CommitFileContent commitFileContent) {
        this.path = path;
        this.binary = commitFileContent.isBinary();
        this.content = commitFileContent.getContent();
    }

    public String getPath() {
        return path;
    }

    public boolean isBinary() {
        return binary;
    }

    public String getContent() {
        return content;
    }

}
