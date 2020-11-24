package com.neo4j.sandbox;

import com.neo4j.sandbox.updater.SandboxCodeBatchUpdater;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main implements CommandLineRunner {

    private final SandboxCodeBatchUpdater batchUpdater;

    public Main(SandboxCodeBatchUpdater batchUpdater) {
        this.batchUpdater = batchUpdater;
    }

    @Override
    public void run(String... args) throws Exception {
        batchUpdater.updateBatch();
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
