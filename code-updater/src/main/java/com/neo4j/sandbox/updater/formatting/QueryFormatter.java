package com.neo4j.sandbox.updater.formatting;

public interface QueryFormatter {

    String format(String initialCode, String newQuery);
}
