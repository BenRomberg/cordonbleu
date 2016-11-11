package com.benromberg.cordonbleu.util;

public class ExceptionUtil {

    public static <T> T convertException(ExceptionalSupplier<T> callback) {
        try {
            return callback.get();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void convertException(ExceptionalCallback callback) {
        try {
            callback.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public interface ExceptionalSupplier<X> {
        X get() throws Exception;
    }

    public interface ExceptionalCallback {
        void call() throws Exception;
    }
}
