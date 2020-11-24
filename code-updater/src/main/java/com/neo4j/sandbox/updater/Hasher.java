package com.neo4j.sandbox.updater;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Hasher {

    public static String hashFiles(List<Path> paths) throws IOException {
        MessageDigest digest = getDigest("SHA-256");
        for (Path path : paths) {
            digest.update(Files.readAllBytes(path));
        }
        return toHexString(digest.digest());
    }

    private static MessageDigest getDigest(String algorithm) throws IOException {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Could not compute hash of files", e);
        }
    }

    // courtesy of https://stackoverflow.com/a/332101/277128
    private static String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
