package com.neo4j.sandbox.updater;

import com.neo4j.sandbox.git.Git;
import com.neo4j.sandbox.git.RepositoryUrls;
import com.neo4j.sandbox.updater.formatting.DefaultQueryFormatter;
import com.neo4j.sandbox.updater.formatting.IndentDetector;
import com.neo4j.sandbox.updater.formatting.JavaQueryFormatter;
import com.neo4j.sandbox.updater.formatting.QueryFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;

@Component
public class SandboxCodeUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(SandboxCodeUpdater.class);

    private final Git cloner;
    private final SandboxMetadataReader metadataReader;
    private final Path sourceCodePath;

    public SandboxCodeUpdater(Git cloner, SandboxMetadataReader metadataReader, @Value("${sandbox.template-repository-path}") Path sourceCodePath) {
        this.cloner = cloner;
        this.metadataReader = metadataReader;
        this.sourceCodePath = sourceCodePath;
    }

    public void updateCodeExamples(Path cloneLocation, String repositoryUri) throws IOException {
        String repositoryName = RepositoryUrls.repositoryName(repositoryUri);
        if (cloneLocation.toFile().exists()) {
            LOGGER.debug("Clone of {} already exists at location {}. Skipping git clone operation.", repositoryUri, cloneLocation);
        } else {
            this.cloner.clone(cloneLocation, repositoryUri);
        }

        Path sandboxCodeFolder = cloneLocation.resolve("code");

        CodeExampleFileVisitor visitor = new CodeExampleFileVisitor();
        Files.walkFileTree(this.sourceCodePath.resolve("code"), visitor);
        for (Path sourceExample : visitor.getMatchedFiles()) {
            String languageName = sourceExample.getParent().toFile().getName();
            LOGGER.debug("About to update {} sample of {}", languageName, repositoryUri);
            Path languageFolder = sandboxCodeFolder.resolve(languageName);
            languageFolder.toFile().mkdirs();
            String code = substituteValues(repositoryName, cloneLocation, sourceExample, newQueryFormatter(languageName));
            Files.write(languageFolder.resolve(sourceExample.toFile().getName()), code.getBytes(StandardCharsets.UTF_8));
        }
    }

    private QueryFormatter newQueryFormatter(String languageName) {
        QueryFormatter queryFormatter;
        IndentDetector indentDetector = new IndentDetector();
        if (languageName.equals("java")) {
            queryFormatter = new JavaQueryFormatter(indentDetector);
        } else {
            queryFormatter = new DefaultQueryFormatter(indentDetector);
        }
        return queryFormatter;
    }

    private String substituteValues(String repositoryName,
                                    Path sandboxRepositoryRootFolder,
                                    Path sourceExample,
                                    QueryFormatter queryFormatter) throws IOException {

        String code = Files.readString(sourceExample);
        try (FileReader readmeReader = new FileReader(sandboxRepositoryRootFolder.resolve("README.adoc").toFile())) {
            SandboxMetadata metadata = metadataReader.readMetadata(readmeReader);
            String indentedQuery = queryFormatter.format(code, metadata.getQuery());
            code = code.replaceFirst("[^\\S\\n]*MATCH \\(m:Movie.*", Matcher.quoteReplacement(indentedQuery));
            code = code.replace("mUser", repositoryName);
            code = code.replace("s3cr3t", repositoryName);
            code = code.replace("movies", repositoryName);
            code = code.replace("movieTitle", metadata.getParameterName());
            code = code.replace("The Matrix", metadata.getParameterValue());
            code = code.replace("actorName", metadata.getResultColumn());
        }
        return code;
    }

}
