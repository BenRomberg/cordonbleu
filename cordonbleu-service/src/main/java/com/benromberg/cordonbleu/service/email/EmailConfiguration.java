package com.benromberg.cordonbleu.service.email;

import org.simplejavamail.api.mailer.config.TransportStrategy;

import java.util.Optional;

public class EmailConfiguration {
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String fromAddress;
    private final String rootPath;
    private final String sharedCss;
    private final Optional<TransportStrategy> transportStrategy;

    public EmailConfiguration(String host, int port, String username, String password, String fromAddress, String rootPath,
            String sharedCss, Optional<TransportStrategy> transportStrategy) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.fromAddress = fromAddress;
        this.rootPath = rootPath;
        this.sharedCss = sharedCss;
        this.transportStrategy = transportStrategy;
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

    public String getFromAddress() {
        return fromAddress;
    }

    public String getRootPath() {
        return rootPath;
    }

    public String getSharedCss() {
        return sharedCss;
    }

    public TransportStrategy getTransportStrategy() {
        return transportStrategy.orElse(TransportStrategy.SMTP_TLS);
    }
}
