package com.neo4j.sandbox.updater;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class CodeVisitor extends SimpleFileVisitor<Path> {

    private static final Pattern EXAMPLE_FILE_REGEX = Pattern.compile("[a-z]xample\\.[a-z]+");

    private final List<Path> matchedFiles = new ArrayList<>(5);

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String lowerCaseFileName = file.toFile().getName().toLowerCase(Locale.ENGLISH);
        if (EXAMPLE_FILE_REGEX.matcher(lowerCaseFileName).find()) {
            matchedFiles.add(file);
            return FileVisitResult.CONTINUE;
        }
        return super.visitFile(file, attrs);
    }

    public List<Path> getMatchedFiles() {
        return matchedFiles;
    }
}
