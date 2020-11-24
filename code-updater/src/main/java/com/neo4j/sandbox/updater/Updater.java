package com.neo4j.sandbox.updater;

import com.neo4j.sandbox.git.Git;
import com.neo4j.sandbox.git.RepositoryUrls;
import com.neo4j.sandbox.github.GithubSettings;
import com.neo4j.sandbox.updater.formatting.DefaultQueryFormatter;
import com.neo4j.sandbox.updater.formatting.IndentDetector;
import com.neo4j.sandbox.updater.formatting.JavaQueryFormatter;
import com.neo4j.sandbox.updater.formatting.QueryFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;

@Component
public class Updater {

    private static final Logger LOGGER = LoggerFactory.getLogger(Updater.class);

    private final Git cloner;

    private final MetadataReader metadataReader;

    private final GithubSettings githubSettings;

    public Updater(Git cloner,
                   MetadataReader metadataReader,
                   GithubSettings githubSettings) {

        this.cloner = cloner;
        this.metadataReader = metadataReader;
        this.githubSettings = githubSettings;
    }

    /**
     * Updates every code sample of the provided sandbox repository.
     * <p>
     * The updater will clone the repository if {@code cloneLocation} does not denote an existing path.
     * <p>
     * The updater generates the code samples based on the configured {@code githubSettings} workspace location. This
     * location denotes the path to the local clone of https://github.com/neo4j-graph-examples/template/, where the
     * canonical examples live.
     *
     * @param cloneLocation path to the local clone of the sandbox repository (will be created by `git clone` if it does
     *                      not exist)
     * @param repositoryUri URI of the sandbox repository
     * @return the list of every code file
     * @throws IOException if any of the underlying file operations fail
     */
    public List<Path> updateCodeExamples(Path cloneLocation, String repositoryUri) throws IOException {
        String repositoryName = RepositoryUrls.repositoryName(repositoryUri);
        if (cloneLocation.toFile().exists()) {
            LOGGER.debug("Clone of {} already exists at location {}. Skipping git clone operation.", repositoryUri, cloneLocation);
        } else {
            LOGGER.trace("About to clone {} at {}.", repositoryUri, cloneLocation);
            this.cloner.clone(cloneLocation, withToken(repositoryUri));
        }

        Path workspace = this.githubSettings.getWorkspace();
        LOGGER.trace("About to update samples based on {} code.", workspace);
        Path sandboxCodeFolder = cloneLocation.resolve("code");
        CodeVisitor visitor = new CodeVisitor();
        Files.walkFileTree(workspace.resolve("code"), visitor);
        List<Path> visitedFiles = visitor.getMatchedFiles();
        for (Path sourceExample : visitedFiles) {
            String languageName = sourceExample.getParent().toFile().getName();
            LOGGER.trace("About to update {} sample of {}", languageName, repositoryUri);
            Path languageFolder = sandboxCodeFolder.resolve(languageName);
            languageFolder.toFile().mkdirs();
            String code = substituteValues(repositoryName, cloneLocation, sourceExample, newQueryFormatter(languageName));
            Files.write(languageFolder.resolve(sourceExample.toFile().getName()), code.getBytes(StandardCharsets.UTF_8));
        }
        return visitedFiles;
    }

    // with this one weird trick, authentication works in Github Action
    private String withToken(String repositoryUri) {
        return repositoryUri.replaceFirst("(https?)://", String.format("$1://%s@", githubSettings.getToken()));
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
            Metadata metadata = metadataReader.readMetadata(readmeReader);
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
