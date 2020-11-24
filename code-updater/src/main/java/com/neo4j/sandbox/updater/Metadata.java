package com.neo4j.sandbox.updater;

import java.util.Objects;

public class Metadata {

    private final String query;
    private final String parameterName;
    private final String parameterValue;
    private final String resultColumn;
    private final String expectedResult;

    private Metadata(String query, String parameterName, String parameterValue, String resultColumn, String expectedResult) {
        this.query = query;
        this.parameterName = parameterName;
        this.parameterValue = parameterValue;
        this.resultColumn = resultColumn;
        this.expectedResult = expectedResult;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getQuery() {
        return query;
    }

    public String getParameterName() {
        return parameterName;
    }

    public String getParameterValue() {
        return parameterValue;
    }

    public String getResultColumn() {
        return resultColumn;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metadata that = (Metadata) o;
        return Objects.equals(query, that.query) &&
                Objects.equals(parameterName, that.parameterName) &&
                Objects.equals(parameterValue, that.parameterValue) &&
                Objects.equals(resultColumn, that.resultColumn) &&
                Objects.equals(expectedResult, that.expectedResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(query, parameterName, parameterValue, resultColumn, expectedResult);
    }

    static class Builder {

        private String query;
        private String parameterName;
        private String parameterValue;
        private String resultColumn;
        private String expectedResult;

        public void setQuery(String query) {
            this.query = query;
        }

        public void setParameterName(String parameterName) {
            this.parameterName = parameterName;
        }

        public void setParameterValue(String parameterValue) {
            this.parameterValue = parameterValue;
        }

        public void setResultColumn(String resultColumn) {
            this.resultColumn = resultColumn;
        }

        public void setExpectedResult(String expectedResult) {
            this.expectedResult = expectedResult;
        }

        public Metadata build() {
            return new Metadata(
                    query,
                    parameterName,
                    parameterValue,
                    resultColumn,
                    expectedResult
            );
        }
    }
}
