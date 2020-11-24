package com.neo4j.sandbox.git;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.neo4j.sandbox.git.RepositoryUrls.repositoryName;
import static com.neo4j.sandbox.git.RepositoryUrls.repositoryOwner;
import static org.assertj.core.api.Assertions.assertThat;

class RepositoryUrlsTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "https://github.com/fbiville/graph-data-science",
            "https://github.com/fbiville/graph-data-science.git",
            "git@github.com:fbiville/graph-data-science.git"
    })
    void extracts_name(String input) {
        assertThat(repositoryName(input)).isEqualTo("graph-data-science");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://github.com/JMHReif/graph-data-science",
            "https://github.com/JMHReif/graph-data-science.git",
            "git@github.com:JMHReif/graph-data-science.git"
    })
    void extracts_owner(String input) {
        assertThat(repositoryOwner(input)).isEqualTo("JMHReif");
    }
}