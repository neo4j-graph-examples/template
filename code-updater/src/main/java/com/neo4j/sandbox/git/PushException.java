package com.neo4j.sandbox.git;

import java.io.IOException;

public final class PushException extends IOException {

    public PushException(IOException exception) {
        super(exception);
    }
}
