package com.docreader.core.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class DocumentClassifierTest {

    @DisplayName("CA-1/CA-2: classifica por extensão (case-insensitive)")
    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource(
        "report.pdf, PDF",
        "NOTE.MD, MARKDOWN",
        "guia.markdown, MARKDOWN",
        "contrato.docx, DOCX",
    )
    fun `classifies by extension`(fileName: String, expected: DocumentType) {
        assertThat(DocumentClassifier.classify(fileName)).isEqualTo(expected)
    }

    @DisplayName("CA-3: usa MIME quando a extensão não decide")
    @Test
    fun `falls back to mime when extension is unknown`() {
        assertThat(DocumentClassifier.classify("a.txt", "application/pdf"))
            .isEqualTo(DocumentType.PDF)
    }

    @DisplayName("CA-4: usa MIME quando não há extensão")
    @Test
    fun `classifies by mime when there is no extension`() {
        assertThat(DocumentClassifier.classify("sem_extensao", "text/markdown"))
            .isEqualTo(DocumentType.MARKDOWN)
    }

    @DisplayName("CA-5: desconhecido vira OTHER")
    @Test
    fun `unknown file is OTHER`() {
        assertThat(DocumentClassifier.classify("foto.jpg")).isEqualTo(DocumentType.OTHER)
    }

    @DisplayName("MIME com parâmetros (;charset) ainda classifica")
    @Test
    fun `mime with parameters is still classified`() {
        assertThat(DocumentClassifier.classify("nota", "text/markdown; charset=utf-8"))
            .isEqualTo(DocumentType.MARKDOWN)
    }

    @DisplayName("extensão desconhecida usa MIME como fallback")
    @Test
    fun `unknown extension falls back to mime`() {
        assertThat(DocumentClassifier.classify("backup.bak", "application/pdf"))
            .isEqualTo(DocumentType.PDF)
    }
}
