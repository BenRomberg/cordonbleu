package com.benromberg.cordonbleu.main.config;

import com.benromberg.cordonbleu.service.email.EmailConfiguration;
import com.benromberg.cordonbleu.util.ClasspathUtil;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EmailServer {
    @JsonProperty
    private String host;

    @JsonProperty
    private int port;

    @JsonProperty
    private String username;

    @JsonProperty
    private String password;

    @JsonProperty
    private String rootPath;

    @JsonCreator
    public EmailServer(String host, int port, String username, String password, String rootPath) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.rootPath = rootPath;
    }

    public EmailConfiguration toConfiguration() {
        String sharedCss = ClasspathUtil.readFileFromClasspath("webpack/sharedWithEmail.css");
        return new EmailConfiguration(host, port, username, password, rootPath, sharedCss);
    }
}
