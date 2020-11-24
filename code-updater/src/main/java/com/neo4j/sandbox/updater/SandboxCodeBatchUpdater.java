package com.neo4j.sandbox.updater;

import com.neo4j.sandbox.git.Git;
import com.neo4j.sandbox.github.Github;
import com.neo4j.sandbox.github.PullRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.neo4j.sandbox.git.RepositoryUrls.repositoryName;
import static com.neo4j.sandbox.git.RepositoryUrls.repositoryOwner;

@Service
public class SandboxCodeBatchUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(SandboxCodeBatchUpdater.class);

    private final SandboxCodeBatchUpdaterSettings settings;
    private final SandboxCodeUpdater sandboxCodeUpdater;
    private final Git git;
    private final Github github;

    public SandboxCodeBatchUpdater(SandboxCodeBatchUpdaterSettings settings,
                                   SandboxCodeUpdater sandboxCodeUpdater,
                                   Git git,
                                   Github github) {
        this.settings = settings;
        this.sandboxCodeUpdater = sandboxCodeUpdater;
        this.git = git;
        this.github = github;
    }

    public void updateBatch() throws IOException {
        Path cloneLocationsBaseDir = Files.createTempDirectory("sandbox-updater");
        for (String repositoryUri : settings.getRepositories()) {
            LOGGER.debug("About to update {}", repositoryUri);
            String repositoryName = repositoryName(repositoryUri);
            Path cloneLocation = cloneLocationsBaseDir.resolve(repositoryName);
            String branch = randomize(repositoryName);

            try {
                sandboxCodeUpdater.updateCodeExamples(cloneLocation, repositoryUri);
                git.checkoutNewBranch(cloneLocation, branch);
                git.commitAll(cloneLocation, String.format("Updating sandbox %s", repositoryName));
                git.push(cloneLocation, "origin", branch);
                github.openPullRequest(
                        repositoryOwner(repositoryUri),
                        repositoryName,
                        newPullRequest(branch, "master", "🤖 Sandbox update"));
            } catch (IOException exception) {
                LOGGER.error("Could not update {}, skipping it. See error below:\n{}", repositoryUri, exception);
            }
        }
    }

    private static String randomize(String base) {
        return String.format("%s-%d", base, System.nanoTime());
    }

    private PullRequest newPullRequest(String from, String into, String title) {
        return new PullRequest(
                title,
                "",
                false,
                true,
                into,
                from
        );
    }
}
