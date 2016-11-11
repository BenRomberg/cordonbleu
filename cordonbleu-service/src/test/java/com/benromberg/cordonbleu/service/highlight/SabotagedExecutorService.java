package com.benromberg.cordonbleu.service.highlight;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

public class SabotagedExecutorService extends AbstractExecutorService {
    private Runnable otherCommand;

    public SabotagedExecutorService(Runnable otherCommand) {
        this.otherCommand = otherCommand;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public List<Runnable> shutdownNow() {
        return null;
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void execute(Runnable command) {
        otherCommand.run();
        command.run();
    }
}