package com.docreader.core.ooxml

import com.docreader.core.ooxml.DocxFixtures.docx
import com.docreader.core.ooxml.DocxFixtures.heading
import com.docreader.core.ooxml.DocxFixtures.listItem
import com.docreader.core.ooxml.DocxFixtures.paragraph
import com.docreader.core.ooxml.DocxFixtures.run
import com.docreader.core.ooxml.DocxFixtures.zipOf
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

class DocxParserTest {

    private fun parse(bytes: ByteArray) = DocxParser.parse(ByteArrayInputStream(bytes))

    @DisplayName("CA-5.1: heading + parágrafo")
    @Test
    fun `parses heading and paragraph`() {
        val bytes = docx(heading(1, "Título") + paragraph(run("Olá mundo")))

        val blocks = parse(bytes)

        assertThat(blocks).containsExactly(
            DocxBlock.Heading(1, "Título"),
            DocxBlock.Paragraph(listOf(DocxRun("Olá mundo"))),
        )
    }

    @DisplayName("CA-5.2/CA-5.3: negrito e itálico")
    @Test
    fun `parses bold and italic runs`() {
        val bytes = docx(paragraph(run("a", bold = true), run("b", italic = true), run("c")))

        val blocks = parse(bytes)

        assertThat(blocks.single()).isEqualTo(
            DocxBlock.Paragraph(
                listOf(
                    DocxRun("a", bold = true),
                    DocxRun("b", italic = true),
                    DocxRun("c"),
                ),
            ),
        )
    }

    @DisplayName("CA-5.4: item de lista")
    @Test
    fun `parses list item`() {
        val bytes = docx(listItem("Primeiro"))

        val blocks = parse(bytes)

        assertThat(blocks).containsExactly(DocxBlock.ListItem("Primeiro"))
    }

    @DisplayName("CA-5.5: HeadingN define o nível")
    @Test
    fun `heading level comes from style`() {
        val bytes = docx(heading(2, "Subtítulo"))

        val blocks = parse(bytes)

        assertThat(blocks).containsExactly(DocxBlock.Heading(2, "Subtítulo"))
    }

    @DisplayName("CA-5.6: corpo vazio")
    @Test
    fun `empty body yields no blocks`() {
        assertThat(parse(docx(""))).isEmpty()
    }

    @DisplayName("CA-5.9: parágrafos em branco são ignorados")
    @Test
    fun `blank paragraphs are skipped`() {
        val bytes = docx(paragraph(run("   ")) + paragraph(run("Real")))

        assertThat(parse(bytes)).containsExactly(
            DocxBlock.Paragraph(listOf(DocxRun("Real"))),
        )
    }

    @DisplayName("CA-5.7: bytes que não são ZIP")
    @Test
    fun `random bytes yield no blocks`() {
        assertThat(parse(byteArrayOf(1, 2, 3, 4, 5))).isEmpty()
    }

    @DisplayName("CA-5.8: ZIP sem word/document.xml")
    @Test
    fun `zip without document entry yields no blocks`() {
        val bytes = zipOf("word/styles.xml" to "<x/>".toByteArray())

        assertThat(parse(bytes)).isEmpty()
    }
}
