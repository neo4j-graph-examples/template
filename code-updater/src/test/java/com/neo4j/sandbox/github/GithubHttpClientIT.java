package com.neo4j.sandbox.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neo4j.sandbox.ContextHttpHandler;
import com.neo4j.sandbox.TestHttpServer;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GithubHttpClientIT {

    private static final String REPOSITORY_OWNER = "neo4j-graph-examples";

    private static final String REPOSITORY_NAME = "northwind";

    @RegisterExtension
    TestHttpServer fakeGithubServer = TestHttpServer
            .withRandomPort()
            .withHandler(new GithubPullRequestHandler(REPOSITORY_OWNER, REPOSITORY_NAME));

    PullRequest pullRequest;

    GithubHttpClient githubClient;

    @BeforeEach
    void prepare() {
        pullRequest = new PullRequest("some-title", "some-description", true, true, "main", "some-branch");
        githubClient = new GithubHttpClient(
                githubSettings(fakeGithubServer.getAddress()),
                new ObjectMapper()
        );
    }

    @Test
    void opens_pull_requests() throws Exception {
        PullRequestResult result = githubClient.openPullRequest(REPOSITORY_OWNER, REPOSITORY_NAME, pullRequest);

        assertThat(result.getId()).isEqualTo(42);
        assertThat(result.getStatus()).isEqualTo("open");
    }

    @Test
    void throws_exception_upon_unsuccessful_response() throws Exception {
        assertThatThrownBy(() -> githubClient.openPullRequest(REPOSITORY_OWNER, "repo-404", pullRequest))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Expected successful response but got status code: 404. See response body below:");
    }

    private GithubSettings githubSettings(String address) {
        GithubSettings settings = new GithubSettings();
        settings.setAddress(address);
        return settings;
    }

    static class GithubPullRequestHandler implements ContextHttpHandler {

        private final String repositoryOwner;
        private final String repositoryName;

        public GithubPullRequestHandler(String repositoryOwner, String repositoryName) {
            this.repositoryOwner = repositoryOwner;
            this.repositoryName = repositoryName;
        }

        @Override
        public String getContext() {
            return String.format("/repos/%s/%s/pulls", this.repositoryOwner, this.repositoryName);
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String json = "{\"id\": 42, \"state\": \"open\"}";
            exchange.sendResponseHeaders(201, json.length());
            try (OutputStream responseBody = exchange.getResponseBody()) {
                responseBody.write(json.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}