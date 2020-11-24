package com.neo4j.sandbox.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class GithubHttpClient implements Github {

    private static final Logger LOGGER = LoggerFactory.getLogger(GithubHttpClient.class);

    private final HttpClient httpClient;

    private final GithubSettings settings;

    private final ObjectMapper objectMapper;

    public GithubHttpClient(GithubSettings settings, ObjectMapper objectMapper) {
        this.settings = settings;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().build();
    }

    @Override
    public PullRequestResult openPullRequest(String repositoryOwner, String repositoryName, PullRequest pullRequest) throws IOException {
        HttpResponse<String> response = sendRequest(HttpRequest.newBuilder()
                .header("Authorization", String.format("Basic %s", encodeCredentials()))
                .header("Accept", "application/vnd.github.v3+json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(serializeRequest(pullRequest)))
                .uri(URI.create(String.format("%s/repos/%s/%s/pulls", this.settings.getAddress(), repositoryOwner, repositoryName)))
                .build());

        return deserializeResponse(assertSuccessfulResponse(response));
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws IOException {
        try {
            return this.httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            throw new IOException("Interrupted while creating pull request", e);
        }
    }

    private HttpResponse<String> assertSuccessfulResponse(HttpResponse<String> response) throws IOException {
        int statusCode = response.statusCode();
        LOGGER.debug("Pull-request HTTP response status code is {}", statusCode);
        if (400 <= statusCode && statusCode < 600) {
            throw new IOException(String.format("Expected successful response but got status code: %d. See response body below:%n%s", statusCode, response.body()));
        }
        return response;
    }

    private String encodeCredentials() {
        String encodedToken = String.format("%s:%s", this.settings.getUsername(), this.settings.getToken());
        return Base64.getEncoder().encodeToString(encodedToken.getBytes(StandardCharsets.UTF_8));
    }

    private String serializeRequest(PullRequest pullRequest) throws IOException {
        return objectMapper.writeValueAsString(pullRequest);
    }

    private PullRequestResult deserializeResponse(HttpResponse<String> response) throws IOException {
        return objectMapper.readValue(response.body(), PullRequestResult.class);
    }
}
