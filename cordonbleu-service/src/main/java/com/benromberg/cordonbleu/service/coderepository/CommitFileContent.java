package com.benromberg.cordonbleu.service.coderepository;

public class CommitFileContent {
    private final boolean binary;
    private final String content;

    private CommitFileContent(boolean binary, String content) {
        this.binary = binary;
        this.content = content;
    }

    public static CommitFileContent ofBinary(String checksum) {
        return new CommitFileContent(true, checksum);
    }

    public static CommitFileContent ofSource(String source) {
        return new CommitFileContent(false, source);
    }

    public boolean isBinary() {
        return binary;
    }

    public String getContent() {
        return content;
    }

}
