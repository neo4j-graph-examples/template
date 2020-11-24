package com.neo4j.sandbox.updater.formatting;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class IndentDetectorTest {

    @Test
    void computes_tab_indents() {
        Optional<Indentation> indentation = new IndentDetector().detect("MATCH (p:Product",
                "cypher_query = '''\n" +
                        "\tMATCH (p:Product)-[:PART_OF]->(:Category)-[:PARENT*0..]-> \n" +
                        "\t(:Category {categoryName:$category}) \n" +
                        "\tRETURN p.productName as product \n" +
                        "'''");

        assertThat(indentation).hasValueSatisfying((indent) -> {
            assertThat(indent.getCharacter()).isEqualTo('\t');
            assertThat(indent.getCount()).isEqualTo(1);
        });
    }

    @Test
    void computes_space_indents() {
        Optional<Indentation> indentation = new IndentDetector().detect("MATCH (p:Product",
                "cypher_query = '''\n" +
                        "   MATCH (p:Product)-[:PART_OF]->(:Category)-[:PARENT*0..]-> \n" +
                        "   (:Category {categoryName:$category}) \n" +
                        "   RETURN p.productName as product \n" +
                        "'''");

        assertThat(indentation).hasValueSatisfying((indent) -> {
            assertThat(indent.getCharacter()).isEqualTo(' ');
            assertThat(indent.getCount()).isEqualTo(3);
        });
    }

    @Test
    void computes_no_indents() {
        Optional<Indentation> indentation = new IndentDetector().detect("MATCH (p:Product",
                "cypher_query = '''\n" +
                        "MATCH (p:Product)-[:PART_OF]->(:Category)-[:PARENT*0..]-> \n" +
                        "(:Category {categoryName:$category}) \n" +
                        "RETURN p.productName as product \n" +
                        "'''");

        assertThat(indentation).isEmpty();
    }
}
