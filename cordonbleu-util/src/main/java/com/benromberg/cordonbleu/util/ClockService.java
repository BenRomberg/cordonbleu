package com.benromberg.cordonbleu.util;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.function.Supplier;

public class ClockService {
    private static Supplier<Clock> activeSupplier = () -> Clock.systemUTC();

    public static Clock getActive() {
        return activeSupplier.get();
    }

    static void setActiveSupplier(Supplier<Clock> clock) {
        activeSupplier = clock;
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(getActive());
    }
}
