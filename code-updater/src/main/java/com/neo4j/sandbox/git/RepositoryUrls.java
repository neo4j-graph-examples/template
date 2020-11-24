package com.neo4j.sandbox.git;

public class RepositoryUrls {

    public static String repositoryOwner(String uri) {
        String namelessUri = uri.replace(String.format("/%s", rawName(uri)), "");
        char delimiter = '/';
        if (namelessUri.contains("@")) {
            delimiter = ':';
        }
        return namelessUri.substring(namelessUri.lastIndexOf(delimiter) + 1);
    }

    public static String repositoryName(String uri) {
        String name = rawName(uri);
        int extensionIndex = name.lastIndexOf(".git");
        if (extensionIndex == -1) {
            return name;
        }
        return name.substring(0, extensionIndex);
    }

    private static String rawName(String uri) {
        return uri.substring(uri.lastIndexOf("/") + 1);
    }
}
