package com.ocsin.jaya.app;

import com.ocsin.jaya.runtime.JayaRuntime;

public final class JayaHandle implements AutoCloseable {

    private final JayaHttpServer server;
    private final JayaRuntime runtime;

    JayaHandle(JayaHttpServer server, JayaRuntime runtime) {
        this.server = server;
        this.runtime = runtime;
    }

    @Override
    public void close() {
        try {
            server.close();
        } finally {
            runtime.close();
        }
    }
}
