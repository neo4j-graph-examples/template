package com.neo4j.sandbox.git;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
public class GitCli implements Git {


    private static final Logger LOGGER = LoggerFactory.getLogger(GitCli.class);

    @Override
    public void clone(Path cloneLocation, String repositoryUri) throws IOException {
        File workingDirectory = cloneLocation.toFile();
        workingDirectory.mkdirs();
        executeCommand(workingDirectory, "git", "clone", repositoryUri, ".");
    }

    @Override
    public void checkoutNewBranch(Path cloneLocation, String branchName) throws IOException {
        executeCommand(cloneLocation.toFile(), "git", "checkout", "-b", branchName);
    }

    @Override
    public void commitAll(Path cloneLocation, String message) throws IOException {
        File workingDirectory = cloneLocation.toFile();
        executeCommand(workingDirectory, "git", "add", "--all");
        try {
            executeCommand(workingDirectory, "git", "commit", "-m", message);
        } catch (IOException e) {
            throw new CommitException(e);
        }
    }

    @Override
    public void push(Path cloneLocation, String remote, String branch) throws IOException {
        try {
            executeCommand(cloneLocation.toFile(), "git", "push", remote, branch);
        } catch (IOException e) {
            throw new PushException(e);
        }
    }

    private void executeCommand(File workingDirectory, String... commands) throws IOException {
        String command = String.join(" ", commands);
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(workingDirectory);
        Process process = processBuilder.start();
        waitForProcess(command, process);
        int exitStatus = process.exitValue();
        LOGGER.trace("Git command {} exited with status code {}", command, exitStatus);
        if (exitStatus != 0) {
            String error = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            throw new IOException(
                    String.format("Expected Git operation %s to succeed but got exit status %d. See error below%n%s",
                            command, exitStatus, error));
        }
    }

    private void waitForProcess(String command, Process process) throws IOException {
        try {
            if (!process.waitFor(1, TimeUnit.MINUTES)) {
                throw new IOException(String.format("Could not perform Git operation %s in less than 1 minute", command));
            }
        } catch (InterruptedException e) {
            throw new IOException(String.format("Interrupted while performing Git operation %s. See error below%n%s", command, e));
        }
    }
}
