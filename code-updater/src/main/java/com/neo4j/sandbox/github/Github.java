package com.neo4j.sandbox.github;

import java.io.IOException;

public interface Github {

    PullRequestResult openPullRequest(String repositoryOwner, String repositoryName, PullRequest pullRequest) throws IOException;
}
