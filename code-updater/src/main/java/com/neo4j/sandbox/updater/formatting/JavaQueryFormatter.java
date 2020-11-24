package com.neo4j.sandbox.updater.formatting;

public class JavaQueryFormatter implements QueryFormatter {

    private final IndentDetector indentDetector;

    public JavaQueryFormatter(IndentDetector indentDetector) {
        this.indentDetector = indentDetector;
    }

    @Override
    public String format(String initialCode, String newQuery) {
        String quotedString = quote(newQuery);
        return indent(initialCode, quotedString);
    }

    private String quote(String indentedQuery) {
        String prefix = "\"";
        String intermediateLineSuffix = "\" +";
        String finalSuffix = "\";";
        String[] lines = indentedQuery.split("\n");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i != 0) {
                result.append(prefix);
            }
            result.append(lines[i]);
            if (i != lines.length - 1) {
                result.append(intermediateLineSuffix);
                result.append("\n");
            } else {
                result.append(finalSuffix);
            }
        }
        return result.toString();
    }

    private String indent(String code, String rawQuery) {
        int firstNewlineIndex = rawQuery.indexOf("\n");
        if (firstNewlineIndex == -1) {
            return rawQuery;
        }
        String firstLine = rawQuery.substring(0, firstNewlineIndex + 1);
        String rest = rawQuery.substring(firstNewlineIndex + 1);
        return new IndentDetector()
                .detect("MATCH (m:Movie", code)
                .map(indentation -> firstLine + indentation.indent(rest))
                .orElse(rawQuery);

    }
}
