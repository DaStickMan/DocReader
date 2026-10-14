package com.docreader.core.ports

import com.docreader.core.domain.DocumentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant

class DeviceFileMapperTest {

    private val modified = Instant.parse("2026-07-01T00:00:00Z")

    @DisplayName("CA-3.1: defaults da consulta")
    @Test
    fun `query defaults are supported types ordered by date desc`() {
        val query = DocumentQuery()

        assertThat(query.mimeTypes).containsExactly(
            SupportedMimeTypes.PDF,
            SupportedMimeTypes.DOCX,
            SupportedMimeTypes.MARKDOWN,
        )
        assertThat(query.sort).isEqualTo(SortOrder.DATE_MODIFIED_DESC)
        assertThat(query.limit).isEqualTo(50)
    }

    @DisplayName("CA-3.2: mapeia pdf preservando metadados")
    @Test
    fun `maps a pdf preserving metadata`() {
        val doc = DeviceFileMapper.toDocument(
            uri = "content://media/1",
            displayName = "relatorio.pdf",
            mimeType = SupportedMimeTypes.PDF,
            sizeBytes = 1234L,
            lastModified = modified,
        )

        assertThat(doc).isNotNull()
        assertThat(doc!!.type).isEqualTo(DocumentType.PDF)
        assertThat(doc.sizeBytes).isEqualTo(1234L)
        assertThat(doc.lastModified).isEqualTo(modified)
    }

    @DisplayName("CA-3.3: tipo não suportado vira null")
    @Test
    fun `unsupported file maps to null`() {
        val doc = DeviceFileMapper.toDocument(
            uri = "content://media/2",
            displayName = "foto.jpg",
            mimeType = null,
            sizeBytes = 10L,
            lastModified = modified,
        )

        assertThat(doc).isNull()
    }

    @DisplayName("CA-3.4: usa MIME quando não há extensão")
    @Test
    fun `uses mime when no extension`() {
        val doc = DeviceFileMapper.toDocument(
            uri = "content://media/3",
            displayName = "notas",
            mimeType = SupportedMimeTypes.MARKDOWN,
            sizeBytes = null,
            lastModified = modified,
        )

        assertThat(doc!!.type).isEqualTo(DocumentType.MARKDOWN)
    }
}
