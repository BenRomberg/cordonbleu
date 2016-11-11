package com.benromberg.cordonbleu.main.task;

import io.dropwizard.servlets.tasks.Task;

import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMultimap;

public abstract class AbstractTask extends Task implements Runnable {
    private static Logger LOGGER = LoggerFactory.getLogger(AbstractTask.class);

    public AbstractTask() {
        super(null);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
        run();
    }

    @Override
    public void run() {
        LOGGER.info("task {} started", getName());
        try {
            runTask();
            LOGGER.info("task {} finished", getName());
        } catch (Exception e) {
            LOGGER.error("task {} aborted", getName(), e);
        }
    }

    public abstract void runTask();
}
