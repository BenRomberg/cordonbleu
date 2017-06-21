package com.benromberg.cordonbleu.main.config;

import com.benromberg.cordonbleu.service.email.EmailConfiguration;
import com.benromberg.cordonbleu.util.ClasspathUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EmailServer {
    @JsonProperty
    private final String host;

    @JsonProperty
    private final int port;

    @JsonProperty
    private final String username;

    @JsonProperty
    private final String password;

    @JsonProperty
    private final String fromAddress;

    @JsonProperty
    private final String rootPath;

    @JsonCreator
    public EmailServer(String host, int port, String username, String password, String fromAddress, String rootPath) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.fromAddress = fromAddress;
        this.rootPath = rootPath;
    }

    public EmailConfiguration toConfiguration() {
        String sharedCss = ClasspathUtil.readFileFromClasspath("webpack/sharedWithEmail.css");
        return new EmailConfiguration(host, port, username, password, fromAddress, rootPath, sharedCss);
    }
}
