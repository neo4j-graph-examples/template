package com.neo4j.sandbox.git;

import java.io.IOException;
import java.nio.file.Path;

public interface Git {

    void clone(Path cloneLocation, String repositoryUri) throws IOException;

    void checkoutNewBranch(Path cloneLocation, String branchName) throws IOException;

    void commitAll(Path cloneLocation, String message) throws IOException;

    void push(Path cloneLocation, String remote, String branch) throws IOException;
}
