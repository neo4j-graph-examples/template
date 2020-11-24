package com.neo4j.sandbox;

import com.neo4j.sandbox.updater.BatchUpdater;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main implements CommandLineRunner {

    private final BatchUpdater batchUpdater;

    public Main(BatchUpdater batchUpdater) {
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
