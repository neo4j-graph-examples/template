package com.neo4j.sandbox.updater;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class HasherTest {

    @Test
    void consistently_hashes_folder_contents() throws Exception {
        Path path = testResourcesPath("/fake-template-repo").resolve("code");
        List<Path> files = asList(
                path.resolve("csharp").resolve("Example.cs"),
                path.resolve("java").resolve("Example.java"),
                path.resolve("python").resolve("example.py")
        );

        String hash = Hasher.hashFiles(files);

        assertThat(hash).isEqualTo("9af486741dbf84d3e9449583b92f098134816262a8346173db283e3ef7815d35");
        assertThat(hash)
                .overridingErrorMessage("hash should be the same when the input is the same")
                .isEqualTo(Hasher.hashFiles(files));
    }

    private Path testResourcesPath(String resourceName) throws URISyntaxException {
        URL resource = this.getClass().getResource(resourceName);
        return new File(resource.toURI()).toPath();
    }
}