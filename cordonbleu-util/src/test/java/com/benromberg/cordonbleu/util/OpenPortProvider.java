package com.benromberg.cordonbleu.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;

public class OpenPortProvider {
    public static int getOpenPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static URI getUri() {
        return URI.create(String.format("http://localhost:%d", getOpenPort()));
    }
}