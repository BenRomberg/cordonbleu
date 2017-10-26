package com.benromberg.cordonbleu.main;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.java8.Java8Bundle;
import io.dropwizard.lifecycle.ExecutorServiceManager;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.benromberg.cordonbleu.data.util.jackson.JsonMapper;
import com.benromberg.cordonbleu.main.auth.CookieAuthFactory;
import com.benromberg.cordonbleu.main.auth.UserAuthenticator;
import com.benromberg.cordonbleu.main.config.CordonBleuConfiguration;
import com.benromberg.cordonbleu.main.config.GuiceModule;
import com.benromberg.cordonbleu.main.task.SendEmailTask;
import com.benromberg.cordonbleu.main.task.UpdateCodeRepositoryTask;
import com.benromberg.cordonbleu.main.task.WarmupSyntaxHighlighterTask;
import com.benromberg.cordonbleu.main.util.jackson.CustomModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.hubspot.dropwizard.guice.GuiceBundle;

public class CordonBleuApplication extends Application<CordonBleuConfiguration> {
    private static final int EXECUTER_SHUTDOWN_SECONDS = 5;
    private static final String EXECUTER_SERVICE_NAME = "cordonbleu-executor";
    private static final String SCHEDULED_EXECUTER_SERVICE_NAME = "cordonbleu-scheduled";

    private Injector injector;
    private ScheduledExecutorService scheduledExecutorService;
    private final Module module;

    public static void main(String[] args) throws Exception {
        for(String bla : args)
            System.out.println(bla);
        /*String[] arg = new String[1];
        arg[0]="C:\\Users\\ndeblock\\dev\\tmp\\qualitydashboard\\conf\\configuration.json";*/
        new CordonBleuApplication(new GuiceModule()).run(args);
    }

    public CordonBleuApplication(Module module) {
        this.module = module;
    }

    @Override
    public void initialize(Bootstrap<CordonBleuConfiguration> bootstrap) {
        GuiceBundle<CordonBleuConfiguration> guiceBundle = GuiceBundle.<CordonBleuConfiguration> newBuilder()
                .addModule(module).enableAutoConfig(getClass().getPackage().getName())
                .setConfigClass(CordonBleuConfiguration.class).build(Stage.DEVELOPMENT);
        bootstrap.addBundle(guiceBundle);
        bootstrap.addBundle(new Java8Bundle());
        bootstrap.addBundle(new AssetsBundle("/static"));
        configureJsonMapper(bootstrap.getObjectMapper());
        injector = guiceBundle.getInjector();
    }

    protected Injector getInjector() {
        return injector;
    }

    private void configureJsonMapper(ObjectMapper mapper) {
        JsonMapper.wrapInstance(mapper);
        CustomModule.enhanceMapper(mapper);
    }

    @Override
    public void run(CordonBleuConfiguration configuration, Environment environment) throws Exception {
        environment.getApplicationContext().addServlet(CatchAllServlet.class, "/*");
        environment.jersey().packages(getClass().getPackage().getName());
        environment.jersey().setUrlPattern("/api/*");
        environment.jersey().register(
                AuthFactory.binder(new CookieAuthFactory(injector.getInstance(UserAuthenticator.class), "session")));
        environment.lifecycle().manage(createExecutorServiceManager());
        scheduledExecutorService = environment.lifecycle().scheduledExecutorService(SCHEDULED_EXECUTER_SERVICE_NAME)
                .build();
        scheduleTasks();
    }

    private ExecutorServiceManager createExecutorServiceManager() {
        return new ExecutorServiceManager(injector.getInstance(ExecutorService.class),
                io.dropwizard.util.Duration.seconds(EXECUTER_SHUTDOWN_SECONDS), EXECUTER_SERVICE_NAME);
    }

    protected void scheduleTasks() {
        scheduleOnceAtStartup(WarmupSyntaxHighlighterTask.class);
        scheduleAtFixedRate(UpdateCodeRepositoryTask.class, Duration.ofMinutes(1));
        scheduleAtFixedRate(SendEmailTask.class, Duration.ofMinutes(1));
    }

    private void scheduleAtFixedRate(Class<? extends Runnable> taskClass, Duration rate) {
        scheduledExecutorService.scheduleAtFixedRate(injector.getInstance(taskClass), 0, rate.toMillis(),
                TimeUnit.MILLISECONDS);
    }

    private void scheduleOnceAtStartup(Class<? extends Runnable> taskClass) {
        scheduledExecutorService.schedule(injector.getInstance(taskClass), 0, TimeUnit.MILLISECONDS);
    }
}
