package com.docreader.core.markdown

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class MarkdownParserTest {

    @Test
    @DisplayName("headings")
    fun `parses headings`() {
        assertThat(MarkdownParser.parse("# A\n### B"))
            .containsExactly(
                MarkdownBlock.Heading(1, "A"),
                MarkdownBlock.Heading(3, "B"),
            )
    }

    @Test
    @DisplayName("parágrafo com múltiplas linhas")
    fun `joins consecutive lines into a paragraph`() {
        assertThat(MarkdownParser.parse("linha um\nlinha dois\n\noutro")).containsExactly(
            MarkdownBlock.Paragraph("linha um\nlinha dois"),
            MarkdownBlock.Paragraph("outro"),
        )
    }

    @Test
    @DisplayName("listas com e sem marcador")
    fun `parses lists`() {
        val markdown = "- a\n- b\n\n1. primeiro\n2. segundo"

        assertThat(MarkdownParser.parse(markdown)).containsExactly(
            MarkdownBlock.BulletItem("a"),
            MarkdownBlock.BulletItem("b"),
            MarkdownBlock.OrderedItem(1, "primeiro"),
            MarkdownBlock.OrderedItem(2, "segundo"),
        )
    }

    @Test
    @DisplayName("bloco de código cercado")
    fun `parses fenced code block`() {
        val markdown = "```kotlin\nval x = 1\nval y = 2\n```"

        assertThat(MarkdownParser.parse(markdown)).containsExactly(
            MarkdownBlock.CodeBlock("val x = 1\nval y = 2", "kotlin"),
        )
    }

    @Test
    @DisplayName("citação, divisor e lista viram blocos distintos")
    fun `parses quote and divider`() {
        assertThat(MarkdownParser.parse("> citado\n> segunda\n\n---")).containsExactly(
            MarkdownBlock.Quote("citado\nsegunda"),
            MarkdownBlock.Divider,
        )
    }

    @Test
    @DisplayName("documento vazio")
    fun `empty document yields no blocks`() {
        assertThat(MarkdownParser.parse("")).isEmpty()
        assertThat(MarkdownParser.parse("\n\n")).isEmpty()
    }
}
