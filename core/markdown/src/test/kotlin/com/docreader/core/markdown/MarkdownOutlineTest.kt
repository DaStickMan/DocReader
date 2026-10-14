package com.docreader.core.markdown

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class MarkdownOutlineTest {

    @DisplayName("CA-6.1: heading simples")
    @Test
    fun `extracts a single heading`() {
        assertThat(MarkdownOutline.extract("# A"))
            .containsExactly(MarkdownHeading(1, "A", 1))
    }

    @DisplayName("CA-6.2: níveis variados na ordem")
    @Test
    fun `extracts headings in order with levels`() {
        val md = """
            # Título
            texto
            ## Seção
            ### Sub
        """.trimIndent()

        assertThat(MarkdownOutline.extract(md)).containsExactly(
            MarkdownHeading(1, "Título", 1),
            MarkdownHeading(2, "Seção", 3),
            MarkdownHeading(3, "Sub", 4),
        )
    }

    @DisplayName("CA-6.3: remove '#' de fechamento")
    @Test
    fun `strips trailing closing hashes`() {
        assertThat(MarkdownOutline.extract("## Foo ##").single().text).isEqualTo("Foo")
    }

    @DisplayName("CA-6.4: não remove '#' sem espaço antes")
    @Test
    fun `keeps sharp without preceding space`() {
        assertThat(MarkdownOutline.extract("## C#").single().text).isEqualTo("C#")
    }

    @DisplayName("CA-6.5: ignora '# ' dentro de bloco cercado")
    @Test
    fun `ignores headings inside fenced blocks`() {
        val md = """
            # Real
            ```
            # Não é heading
            ```
            ## Também real
        """.trimIndent()

        assertThat(MarkdownOutline.extract(md).map { it.text }).containsExactly("Real", "Também real")
    }
}
