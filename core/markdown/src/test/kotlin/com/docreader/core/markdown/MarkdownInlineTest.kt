package com.docreader.core.markdown

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class MarkdownInlineTest {

    @Test
    @DisplayName("negrito, itálico e código")
    fun `parses inline formatting`() {
        val spans = MarkdownInline.parse("a **b** c *d* e `f`")

        assertThat(spans).containsExactly(
            InlineSpan("a "),
            InlineSpan("b", bold = true),
            InlineSpan(" c "),
            InlineSpan("d", italic = true),
            InlineSpan(" e "),
            InlineSpan("f", code = true),
        )
    }

    @Test
    @DisplayName("sublinhado duplo também é negrito")
    fun `double underscore is bold`() {
        assertThat(MarkdownInline.parse("__forte__")).containsExactly(InlineSpan("forte", bold = true))
    }

    @Test
    @DisplayName("texto sem marcação vira um único span")
    fun `plain text is a single span`() {
        assertThat(MarkdownInline.parse("simples")).containsExactly(InlineSpan("simples"))
    }

    @Test
    @DisplayName("texto vazio")
    fun `empty text yields nothing`() {
        assertThat(MarkdownInline.parse("")).isEmpty()
    }
}
