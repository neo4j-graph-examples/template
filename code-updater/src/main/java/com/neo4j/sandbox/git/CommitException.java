package com.neo4j.sandbox.git;

import java.io.IOException;

public final class CommitException extends IOException {

    public CommitException(IOException exception) {
        super(exception);
    }
}
