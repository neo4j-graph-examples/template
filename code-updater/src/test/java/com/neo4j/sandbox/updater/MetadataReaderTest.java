package com.neo4j.sandbox.updater;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;

public class MetadataReaderTest {

    @Test
    void reads_metadata() throws Exception {
        StringReader asciidocContents = new StringReader(
                ":param-name: category\n" +
                        ":param-value: Dairy Products\n" +
                        ":result-column: product\n" +
                        ":expected-result: Geitost\n" +
                        ":query: MATCH (p:Product)-[:PART_OF]->(:Category)-[:PARENT*0..]-> +\n" +
                        " (:Category {categoryName:$category}) +\n" +
                        " RETURN p.productName as product +");

        Metadata metadata = new MetadataReader().readMetadata(asciidocContents);

        assertThat(metadata.getQuery()).isEqualTo("MATCH (p:Product)-[:PART_OF]->(:Category)-[:PARENT*0..]->\n" +
                "(:Category {categoryName:$category})\n" +
                "RETURN p.productName as product");
        assertThat(metadata.getExpectedResult()).isEqualTo("Geitost");
        assertThat(metadata.getParameterName()).isEqualTo("category");
        assertThat(metadata.getParameterValue()).isEqualTo("Dairy Products");
        assertThat(metadata.getResultColumn()).isEqualTo("product");
    }
}
