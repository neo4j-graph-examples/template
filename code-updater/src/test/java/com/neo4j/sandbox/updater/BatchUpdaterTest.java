package com.neo4j.sandbox.updater;

import com.neo4j.sandbox.git.CommitException;
import com.neo4j.sandbox.git.Git;
import com.neo4j.sandbox.git.PushException;
import com.neo4j.sandbox.github.Github;
import com.neo4j.sandbox.github.PullRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BatchUpdaterTest {

    private final Updater updater = mock(Updater.class);

    private final Git git = mock(Git.class);

    private final Github github = mock(Github.class);

    private final BatchUpdater batchUpdater = new BatchUpdater(
            settings(
                    "https://example.com/sandbox/number-1",
                    "https://example.com/sandbox/number-2",
                    "https://example.com/sandbox/number-3"
            ),
            updater,
            git,
            github
    );

    @Test
    void reads_list_of_sandboxes_to_update() throws Exception {
        batchUpdater.updateBatch();

        InOrder inOrder = inOrder(updater, git, github);
        inOrder.verify(updater).updateCodeExamples(any(Path.class), eq("https://example.com/sandbox/number-1"));
        verifyGitAndGithubInteractions(inOrder, "number-1");
        inOrder.verify(updater).updateCodeExamples(any(Path.class), eq("https://example.com/sandbox/number-2"));
        verifyGitAndGithubInteractions(inOrder, "number-2");
        inOrder.verify(updater).updateCodeExamples(any(Path.class), eq("https://example.com/sandbox/number-3"));
        verifyGitAndGithubInteractions(inOrder, "number-3");
    }

    @Test
    void throws_when_failure_happens() throws IOException {
        when(updater.updateCodeExamples(any(Path.class), eq("https://example.com/sandbox/number-2")))
                .thenThrow(new IOException("oopsie 2"));
        when(updater.updateCodeExamples(any(Path.class), eq("https://example.com/sandbox/number-3")))
                .thenThrow(new IOException("oopsie 3"));

        assertThatThrownBy(batchUpdater::updateBatch)
                .isInstanceOf(IOException.class)
                .hasMessage(
                        "2 of the updates failed, see details below\n" +
                                "---\n" +
                                " - Update for sandbox https://example.com/sandbox/number-2 failed with the following error:\n" +
                                "java.io.IOException: oopsie 2\n" +
                                " - Update for sandbox https://example.com/sandbox/number-3 failed with the following error:\n" +
                                "java.io.IOException: oopsie 3\n" +
                                "---\n");
    }

    @Test
    void ignores_when_commits_fails() throws IOException {
        when(updater.updateCodeExamples(any(Path.class), eq("https://example.com/sandbox/number-3")))
                .thenThrow(new CommitException(new IOException("oopsie")));

        assertThatCode(batchUpdater::updateBatch).doesNotThrowAnyException();
    }

    @Test
    void ignores_when_trying_to_push_an_existing_branch() throws IOException {
        when(updater.updateCodeExamples(any(Path.class), eq("https://example.com/sandbox/number-1")))
                .thenThrow(new PushException(new IOException("! [rejected]        number-1-452fb7da8bdfa8d748bc7b8992d0526088fbefffe9376ca2caca2b7afc9f072c -> number-1-452fb7da8bdfa8d748bc7b8992d0526088fbefffe9376ca2caca2b7afc9f072c (non-fast-forward)\n" +
                        "\terror: failed to push some refs to 'https://example.com/sandbox/number-1'\n" +
                        "\thint: Updates were rejected because the tip of your current branch is behind\n" +
                        "\thint: its remote counterpart. Integrate the remote changes (e.g.\n" +
                        "\thint: 'git pull ...') before pushing again.\n" +
                        "\thint: See the 'Note about fast-forwards' in 'git push --help' for details.")));

        assertThatCode(batchUpdater::updateBatch).doesNotThrowAnyException();
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

    private BatchUpdaterSettings settings(String... urls) {
        BatchUpdaterSettings settings = new BatchUpdaterSettings();
        settings.setRepositories(Arrays.asList(urls));
        return settings;
    }
}
