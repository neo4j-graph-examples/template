package com.neo4j.sandbox.updater.formatting;

public class DefaultQueryFormatter implements QueryFormatter {

    private final IndentDetector indentDetector;

    public DefaultQueryFormatter(IndentDetector indentDetector) {
        this.indentDetector = indentDetector;
    }

    @Override
    public String format(String initialCode, String newQuery) {
        return indent(initialCode, newQuery);
    }

    private String indent(String code, String rawQuery) {
        return new IndentDetector()
                .detect("MATCH (m:Movie", code)
                .map(indentation -> indentation.indent(rawQuery))
                .orElse(rawQuery);
    }
}
