package com.docreader.core.application

import com.docreader.core.domain.Document
import com.docreader.core.domain.DocumentType
import com.docreader.core.ports.DocumentContentReader
import com.docreader.core.ports.DocumentTextWriter
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant

class DocumentContentTest {

    private class FakeReader : DocumentContentReader {
        val bytes = mutableMapOf<String, ByteArray>()
        val texts = mutableMapOf<String, String>()
        override suspend fun readBytes(uri: String) = bytes[uri] ?: ByteArray(0)
        override suspend fun readText(uri: String) = texts[uri] ?: ""
    }

    private class FakeWriter : DocumentTextWriter {
        val written = mutableMapOf<String, String>()
        override suspend fun writeText(uri: String, text: String) {
            written[uri] = text
        }
    }

    private val document = Document(
        uri = "content://x/nota.md",
        displayName = "nota.md",
        type = DocumentType.MARKDOWN,
        lastModified = Instant.EPOCH,
    )

    @Test
    @DisplayName("lê texto e bytes pela uri do documento")
    fun `reads content through the document uri`() = runTest {
        val reader = FakeReader().apply {
            texts["content://x/nota.md"] = "# Olá"
            bytes["content://x/nota.md"] = byteArrayOf(1, 2, 3)
        }
        val content = DocumentContent(reader, FakeWriter())

        assertThat(content.readText(document)).isEqualTo("# Olá")
        assertThat(content.readBytes(document)).hasSize(3)
    }

    @Test
    @DisplayName("salva texto na uri indicada")
    fun `saves text to the given uri`() = runTest {
        val writer = FakeWriter()
        val content = DocumentContent(FakeReader(), writer)

        content.saveText("content://x/nota.md", "novo")

        assertThat(writer.written["content://x/nota.md"]).isEqualTo("novo")
    }
}
