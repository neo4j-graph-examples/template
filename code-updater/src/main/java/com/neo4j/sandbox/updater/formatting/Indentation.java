package com.neo4j.sandbox.updater.formatting;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class Indentation {
    private final char symbol;
    private final int count;

    private Indentation(char symbol, int count) {
        this.symbol = symbol;
        this.count = count;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String indent(String query) {
        return Arrays.stream(query.split("\n"))
                .map(this::applyIndent)
                .collect(Collectors.joining("\n"));
    }

    public char getCharacter() {
        return symbol;
    }

    public int getCount() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Indentation that = (Indentation) o;
        return symbol == that.symbol &&
                count == that.count;
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, count);
    }

    private String applyIndent(String line) {
        return String.format("%s%s",
                String.valueOf(this.symbol).repeat(this.count),
                line
        );
    }

    static class Builder {

        private char symbol;

        private int count;

        private Builder() {
        }

        public Builder setSymbol(char symbol) {
            this.symbol = symbol;
            return this;
        }

        public Builder setCount(int count) {
            this.count = count;
            return this;
        }

        public Indentation build() {
            return new Indentation(this.symbol, this.count);
        }
    }
}
