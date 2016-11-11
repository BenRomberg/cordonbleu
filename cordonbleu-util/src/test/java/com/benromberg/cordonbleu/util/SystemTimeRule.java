package com.benromberg.cordonbleu.util;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.benromberg.cordonbleu.util.ClockService;

public class SystemTimeRule implements TestRule {
    private static final ZoneOffset ZONE = ZoneOffset.UTC;

    private Clock clock;

    public SystemTimeRule() {
        clock = Clock.fixed(Clock.systemUTC().instant(), ZONE);
    }

    public SystemTimeRule withTime(LocalDateTime time) {
        clock = Clock.fixed(time.toInstant(ZONE), ZONE);
        return this;
    }

    public void advanceBySeconds(int seconds) {
        clock = Clock.fixed(clock.instant().plusSeconds(seconds), ZONE);
    }

    public void advanceByDuration(Duration duration) {
        clock = Clock.fixed(clock.instant().plus(duration), ZONE);
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Clock oldClock = ClockService.getActive();
                ClockService.setActiveSupplier(() -> clock);
                base.evaluate();
                ClockService.setActiveSupplier(() -> oldClock);
            }
        };
    }

    public LocalDateTime getDateTime() {
        return LocalDateTime.now(clock);
    }
}
