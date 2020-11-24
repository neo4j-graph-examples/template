package com.neo4j.sandbox.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PullRequestResult {

    private final long id;
    private final String status;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public PullRequestResult(@JsonProperty("id") long id, @JsonProperty("state") String status) {
        this.id = id;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }
}
