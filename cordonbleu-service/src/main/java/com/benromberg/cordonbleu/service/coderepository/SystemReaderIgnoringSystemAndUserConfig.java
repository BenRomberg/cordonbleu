package com.benromberg.cordonbleu.service.coderepository;

import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.SystemReader;

public class SystemReaderIgnoringSystemAndUserConfig extends SystemReader {
    private static final FileBasedConfig NULL_CONFIG = new FileBasedConfig(null, null) {
        public void load() {
        }

        public boolean isOutdated() {
            return false;
        }
    };

    private final SystemReader delegate;

    public SystemReaderIgnoringSystemAndUserConfig() {
        delegate = SystemReader.getInstance();
    }

    @Override
    public FileBasedConfig openUserConfig(Config parent, FS fs) {
        return NULL_CONFIG;
    }

    @Override
    public FileBasedConfig openSystemConfig(Config parent, FS fs) {
        return NULL_CONFIG;
    }
    @Override
    public String getHostname() {
        return delegate.getHostname();
    }

    @Override
    public String getenv(String variable) {
        return delegate.getenv(variable);
    }

    @Override
    public String getProperty(String key) {
        return delegate.getProperty(key);
    }


    @Override
    public long getCurrentTime() {
        return delegate.getCurrentTime();
    }

    @Override
    public int getTimezone(long when) {
        return delegate.getTimezone(when);
    }
}
