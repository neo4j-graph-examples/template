package com.neo4j.sandbox.updater.formatting;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IndentDetector {

    public Optional<Indentation> detect(String substring, String content) {
        return Arrays.stream(content.split("\n"))
                .filter(line -> line.contains(substring))
                .findFirst()
                .flatMap(this::computeIndent);
    }

    private Optional<Indentation> computeIndent(String line) {
        Matcher matcher = Pattern.compile("^(\\s+)").matcher(line);
        if (!matcher.find()) {
            return Optional.empty();
        }
        String indent = matcher.group(1);
        return Optional.of(Indentation.builder()
                .setSymbol(indent.charAt(0))
                .setCount(indent.length())
                .build());
    }
}
