package com.neo4j.sandbox;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.Executor;

public class TestHttpServer implements BeforeEachCallback, AfterEachCallback {

    private HttpServer server;

    private TestHttpServer(InetSocketAddress inetSocketAddress) throws IOException {
        server = HttpServer.create(inetSocketAddress, 10);
    }

    public static TestHttpServer withRandomPort() {
        return withPort(0);
    }

    public static TestHttpServer withPort(int port) {
        try {
            return new TestHttpServer(new InetSocketAddress(port));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public TestHttpServer withHandler(ContextHttpHandler handler, Filter... filters) {
        return withHandler(handler.getContext(), handler, filters);
    }

    public TestHttpServer withHandler(ContextHttpHandler handler, Authenticator authenticator, Filter... filters) {
        return withHandler(handler.getContext(), handler, authenticator, filters);
    }

    public TestHttpServer withHandler(String context, HttpHandler handler, Filter... filters) {
        this.registerHandler(context, handler, null, filters);
        return this;
    }

    public TestHttpServer withHandler(String context, HttpHandler handler, Authenticator authenticator, Filter... filters) {
        this.registerHandler(context, handler, authenticator, filters);
        return this;
    }

    public TestHttpServer withExecutor(Executor executor) {
        this.server.setExecutor(executor);
        return this;
    }

    public String getAddress() {
        return String.format("http://localhost:%d", server.getAddress().getPort());
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        start();
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        stop();
    }

    private void start() {
        server.start();
    }

    private void stop() {
        server.stop(2);
    }

    private void registerHandler(String path, HttpHandler handler, Authenticator authenticator, Filter... filters) {
        HttpContext context = server.createContext(path, handler);
        if (authenticator != null) {
            context.setAuthenticator(authenticator);
        }
        if (filters.length > 0) {
            context.getFilters().addAll(Arrays.asList(filters));
        }
    }
}

