package com.neo4j.sandbox.updater;

import com.neo4j.sandbox.git.CommitException;
import com.neo4j.sandbox.git.Git;
import com.neo4j.sandbox.git.PushException;
import com.neo4j.sandbox.github.Github;
import com.neo4j.sandbox.github.PullRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.neo4j.sandbox.git.RepositoryUrls.repositoryName;
import static com.neo4j.sandbox.git.RepositoryUrls.repositoryOwner;

@Service
public class BatchUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchUpdater.class);

    private final BatchUpdaterSettings settings;

    private final Updater updater;

    private final Git git;

    private final Github github;

    public BatchUpdater(BatchUpdaterSettings settings,
                        Updater updater,
                        Git git,
                        Github github) {
        this.settings = settings;
        this.updater = updater;
        this.git = git;
        this.github = github;
    }

    /**
     * Creates a Pull Request with code updates for every configured sandbox repository.
     * <p>
     * The generated branch name is based on a consistent hash so that a Pull Request will not be open if an existing
     * branch with the same name still exists (the corresponding IOException for `git push` is ignored in that specific
     * case).
     * <p>
     * The batch updater will try to update every sandbox repository and report failures at the end of the batch.
     *
     * @throws IOException if any of the underlying Git or Github operation fails in an unexpected way.
     * @see Hasher for the hash computation
     * @see BatchUpdaterSettings for the batch updater configuration
     */
    public void updateBatch() throws IOException {
        Path cloneLocationsBaseDir = Files.createTempDirectory("sandbox-updater");
        List<String> repositories = settings.getRepositories();
        Map<String, Exception> updateFailures = new LinkedHashMap<>(repositories.size());
        for (String repositoryUri : repositories) {
            LOGGER.debug("About to update sandbox {}", repositoryUri);
            String repositoryName = repositoryName(repositoryUri);
            Path cloneLocation = cloneLocationsBaseDir.resolve(repositoryName);

            String branch = null;
            try {
                List<Path> updatedFiles = updater.updateCodeExamples(cloneLocation, repositoryUri);
                branch = generateConsistentBranchName(repositoryName, updatedFiles);
                git.checkoutNewBranch(cloneLocation, branch);
                git.commitAll(cloneLocation, String.format("Updating sandbox %s", repositoryName));
                git.push(cloneLocation, "origin", branch);
                github.openPullRequest(
                        repositoryOwner(repositoryUri),
                        repositoryName,
                        newPullRequest(branch, "master", "🤖 Sandbox update"));
            } catch (CommitException exception) {
                LOGGER.info("Could not update sandbox {} as git commit failed. This is likely due to the fact there is nothing to update. Skipping the update", repositoryUri);
            } catch (PushException exception) {
                if (failedPushingExistingBranch(exception)) {
                    LOGGER.info("Could not update sandbox {} as the branch {} already exists. Skipping the update.", repositoryUri, branch);
                } else {
                    throw exception;
                }
            } catch (IOException exception) {
                updateFailures.put(repositoryUri, exception);
                LOGGER.error("Could not update sandbox {}. Skipping the update.", repositoryUri);
            }
        }
        if (updateFailures.size() > 0) {
            throw new IOException(formatFailure(updateFailures));
        }
    }


    /*
     * create a consistent branch name
     * so that the update will fail if a branch already exists with the same changes
     */
    private static String generateConsistentBranchName(String repositoryName, List<Path> updatedFiles) throws IOException {
        return String.format("%s-%s", repositoryName, Hasher.hashFiles(updatedFiles));
    }

    private static boolean failedPushingExistingBranch(PushException exception) {
        return exception.getMessage().contains("error: failed to push some refs");
    }

    private static PullRequest newPullRequest(String from, String into, String title) {
        return new PullRequest(
                title,
                "",
                false,
                true,
                into,
                from
        );
    }

    private static String formatFailure(Map<String, Exception> updateFailures) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%d of the updates failed, see details below%n---%n", updateFailures.size()));
        updateFailures.forEach((sandbox, failure) -> {
            builder.append(String.format(" - Update for sandbox %s failed with the following error:%n", sandbox));
            builder.append(indent(failure.toString()));
            builder.append("\n");
        });
        return builder.append(String.format("---%n")).toString();
    }

    private static String indent(String content) {
        return content.replaceAll("\n", "\n\t");
    }
}
