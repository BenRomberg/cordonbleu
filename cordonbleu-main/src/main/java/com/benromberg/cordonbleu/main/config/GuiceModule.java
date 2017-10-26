package com.benromberg.cordonbleu.main.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import com.benromberg.cordonbleu.data.model.NamedEntity;
import com.benromberg.cordonbleu.data.util.KeyPairGenerator;
import com.benromberg.cordonbleu.data.validation.NameValidation;
import com.benromberg.cordonbleu.data.validation.UserValidation;
import com.benromberg.cordonbleu.service.coderepository.CodeRepositoryFactory;
import com.benromberg.cordonbleu.service.coderepository.CodeRepositoryService.CodeRepositoryFolderProvider;
import com.benromberg.cordonbleu.service.coderepository.RepositoryFactory;
import com.benromberg.cordonbleu.service.coderepository.keypair.SshKeyPairGenerator;
import com.benromberg.cordonbleu.service.coderepository.keypair.SshPrivateKeyPasswordProvider;
import com.benromberg.cordonbleu.service.coderepository.svncredential.SvnCredentialProvider;
import com.benromberg.cordonbleu.service.commit.CommitNotificationConsiderationAmount;
import com.benromberg.cordonbleu.service.email.EmailConfiguration;
import com.benromberg.cordonbleu.service.highlight.HighlightingTimeout;
import com.benromberg.cordonbleu.service.user.PasswordValidation;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.mongodb.DB;

public class GuiceModule extends AbstractModule {
    private static final int THREAD_KEEP_ALIVE_SECONDS = 60;
    private static final int MAX_THREAD_POOL = 10;
    private static final int MIN_THREAD_POOL = 2;

    @SuppressWarnings("deprecation")
    @Provides
    public DB getDatabase(ManagedMongo mongo) {
        // TODO: MongoDatabase not supported by MongoJack 2.3.0, update once compatible version is out
        return mongo.getMongoClient().getDB("cordonbleu");
    }

    @Provides
    public EmailConfiguration getEmailConfiguration(CordonBleuConfiguration configuration) {
        return configuration.getEmailConfiguration();
    }

    @Provides
    @Singleton
    public ExecutorService getExecutorService() {
        return new ThreadPoolExecutor(MIN_THREAD_POOL, MAX_THREAD_POOL, THREAD_KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
    }

    @Override
    protected void configure() {
        bind(HighlightingTimeout.class).to(CordonBleuConfiguration.class);
        bind(CodeRepositoryFolderProvider.class).to(CordonBleuConfiguration.class);
        bind(CommitNotificationConsiderationAmount.class).to(CordonBleuConfiguration.class);
        bind(SshPrivateKeyPasswordProvider.class).to(CordonBleuConfiguration.class);
        bind(SvnCredentialProvider.class).to(CordonBleuConfiguration.class);
        bind(CodeRepositoryFactory.class).to(RepositoryFactory.class);
        bind(KeyPairGenerator.class).to(SshKeyPairGenerator.class);
        bind(UserValidation.class).toInstance(SharedConfig.sharedConfig());
        bind(new TypeLiteral<NameValidation<NamedEntity>>() {
        }).toInstance(SharedConfig.sharedConfig().getNameValidation());
        bind(PasswordValidation.class).toInstance(SharedConfig.sharedConfig());
    }
}
