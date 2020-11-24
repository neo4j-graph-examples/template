package com.neo4j.sandbox.updater;

import com.neo4j.sandbox.git.Git;
import com.neo4j.sandbox.github.Github;
import com.neo4j.sandbox.github.PullRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class SandboxCodeBatchUpdaterTest {

    SandboxCodeUpdater codeUpdater;
    Git git;
    Github github;
    SandboxCodeBatchUpdater updater;

    @BeforeEach
    public void prepare() {
        codeUpdater = mock(SandboxCodeUpdater.class);
        git = mock(Git.class);
        github = mock(Github.class);

        updater = new SandboxCodeBatchUpdater(
                settings(
                        "https://example.com/sandbox/number-1",
                        "https://example.com/sandbox/number-2",
                        "https://example.com/sandbox/number-3"
                ),
                codeUpdater,
                git,
                github
        );
    }

    @Test
    void reads_list_of_sandboxes_to_update() throws Exception {
        updater.updateBatch();

        InOrder inOrder = inOrder(codeUpdater, git, github);
        inOrder.verify(codeUpdater).updateCodeExamples(any(Path.class), eq("https://example.com/sandbox/number-1"));
        verifyGitAndGithubInteractions(inOrder, "number-1");
        inOrder.verify(codeUpdater).updateCodeExamples(any(Path.class), eq("https://example.com/sandbox/number-2"));
        verifyGitAndGithubInteractions(inOrder, "number-2");
        inOrder.verify(codeUpdater).updateCodeExamples(any(Path.class), eq("https://example.com/sandbox/number-3"));
        verifyGitAndGithubInteractions(inOrder, "number-3");
    }

    private void verifyGitAndGithubInteractions(InOrder inOrder, String repositoryName) throws Exception {
        String branchPrefix = String.format("%s-", repositoryName);
        inOrder.verify(git).checkoutNewBranch(any(Path.class), startsWith(branchPrefix));
        inOrder.verify(git).commitAll(any(Path.class), eq(String.format("Updating sandbox %s", repositoryName)));
        inOrder.verify(git).push(any(Path.class), eq("origin"), startsWith(branchPrefix));

        ArgumentCaptor<PullRequest> pullRequestCaptor = ArgumentCaptor.forClass(PullRequest.class);
        inOrder.verify(github).openPullRequest(eq("sandbox"), eq(repositoryName), pullRequestCaptor.capture());
        PullRequest pullRequest = pullRequestCaptor.getValue();
        assertThat(pullRequest.getTitle()).isEqualTo("🤖 Sandbox update");
        assertThat(pullRequest.getDescription()).isEmpty();
        assertThat(pullRequest.isDraft()).isFalse();
        assertThat(pullRequest.maintainersCanModify()).isTrue();
        assertThat(pullRequest.getBase()).isEqualTo("master");
        assertThat(pullRequest.getBranch()).startsWith(branchPrefix);
    }

    private SandboxCodeBatchUpdaterSettings settings(String... urls) {
        SandboxCodeBatchUpdaterSettings settings = new SandboxCodeBatchUpdaterSettings();
        settings.setRepositories(Arrays.asList(urls));
        return settings;
    }
}
