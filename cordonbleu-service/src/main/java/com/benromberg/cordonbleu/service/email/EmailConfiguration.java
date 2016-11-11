package com.benromberg.cordonbleu.service.email;

public class EmailConfiguration {
    private String host;
    private int port;
    private String username;
    private String password;
    private String rootPath;
    private String sharedCss;

    public EmailConfiguration(String host, int port, String username, String password, String rootPath, String sharedCss) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.rootPath = rootPath;
        this.sharedCss = sharedCss;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRootPath() {
        return rootPath;
    }

    public String getSharedCss() {
        return sharedCss;
    }
}
