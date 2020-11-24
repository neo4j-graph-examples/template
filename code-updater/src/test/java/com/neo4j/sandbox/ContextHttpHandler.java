package com.neo4j.sandbox;

import com.sun.net.httpserver.HttpHandler;

public interface ContextHttpHandler extends HttpHandler {
    String getContext();
}