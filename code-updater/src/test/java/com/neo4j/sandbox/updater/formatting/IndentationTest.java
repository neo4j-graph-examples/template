package com.neo4j.sandbox.updater.formatting;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IndentationTest {

    @Test
    void indents_multiline_content() {
        Indentation indentation = Indentation.builder().setSymbol('\t').setCount(3).build();

        String result = indentation.indent("Hello\nWorld\n!!");

        assertThat(result).isEqualTo("\t\t\tHello\n\t\t\tWorld\n\t\t\t!!");
    }

    @Test
    void indents_single_line_content() {
        Indentation indentation = Indentation.builder().setSymbol(' ').setCount(1).build();

        String result = indentation.indent("Hello World !!");

        assertThat(result).isEqualTo(" Hello World !!");
    }
}