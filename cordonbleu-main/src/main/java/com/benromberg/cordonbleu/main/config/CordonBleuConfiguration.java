package com.benromberg.cordonbleu.main.config;

import com.benromberg.cordonbleu.data.util.jackson.IgnoreSuperClassFields;
import com.benromberg.cordonbleu.service.coderepository.CodeRepositoryService.CodeRepositoryFolderProvider;
import com.benromberg.cordonbleu.service.coderepository.keypair.SshPrivateKeyPasswordProvider;
import com.benromberg.cordonbleu.service.commit.CommitNotificationConsiderationAmount;
import com.benromberg.cordonbleu.service.email.EmailConfiguration;
import com.benromberg.cordonbleu.service.highlight.HighlightingTimeout;
import io.dropwizard.Configuration;

import java.io.File;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CordonBleuConfiguration extends Configuration implements CodeRepositoryFolderProvider, HighlightingTimeout,
        CommitNotificationConsiderationAmount, SshPrivateKeyPasswordProvider {
    @JsonProperty
    private final File codeRepositoryFolder;

    @JsonProperty
    private final String mongoUrl;

    @JsonProperty
    private final EmailServer emailServer;

    @JsonProperty
    private final long highlightingTimeoutInMilliseconds;

    @JsonProperty
    private final int commitNotificationConsiderationAmount;

    @JsonProperty
    private final Optional<Credentials> globalCredentials;

    @JsonProperty
    private final String sshPrivateKeyPassword;

    @JsonCreator
    @IgnoreSuperClassFields
    private CordonBleuConfiguration(File codeRepositoryFolder, String mongoUrl, EmailServer emailServer,
            long highlightingTimeoutInMilliseconds, int commitNotificationConsiderationAmount,
            Optional<Credentials> globalCredentials, String sshPrivateKeyPassword) {
        this.codeRepositoryFolder = codeRepositoryFolder;
        this.mongoUrl = mongoUrl;
        this.emailServer = emailServer;
        this.highlightingTimeoutInMilliseconds = highlightingTimeoutInMilliseconds;
        this.commitNotificationConsiderationAmount = commitNotificationConsiderationAmount;
        this.globalCredentials = globalCredentials;
        this.sshPrivateKeyPassword = sshPrivateKeyPassword;
    }

    public String getMongoUrl() {
        return mongoUrl;
    }

    public Optional<Credentials> getGlobalCredentials() {
        return globalCredentials;
    }

    @Override
    public File getCodeRepositoryFolder() {
        codeRepositoryFolder.mkdirs();
        return codeRepositoryFolder;
    }

    public EmailConfiguration getEmailConfiguration() {
        return emailServer.toConfiguration();
    }

    @Override
    public long getHighlightingTimeoutInMillis() {
        return highlightingTimeoutInMilliseconds;
    }

    @Override
    public int getCommitNotificationConsiderationAmount() {
        return commitNotificationConsiderationAmount;
    }

    @Override
    public String getSshPrivateKeyPassword() {
        return sshPrivateKeyPassword;
    }
}
