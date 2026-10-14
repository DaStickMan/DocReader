package com.docreader.core.application

import com.docreader.core.application.fakes.FakeDocumentSource
import com.docreader.core.application.fakes.InMemoryRecentStore
import com.docreader.core.domain.Document
import com.docreader.core.domain.DocumentType
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant

class GetRecentDocumentsTest {

    private fun doc(uri: String, name: String = uri.substringAfterLast('/'), modified: String) =
        Document(
            uri = uri,
            displayName = name,
            type = DocumentType.PDF,
            lastModified = Instant.parse(modified),
        )

    @DisplayName("CA-2.1: mescla próprios + aparelho e ordena")
    @Test
    fun `merges own and device sources`() = runTest {
        val own = listOf(doc("content://x/own.pdf", modified = "2026-03-01T00:00:00Z"))
        val device = listOf(doc("content://x/dev.pdf", modified = "2026-05-01T00:00:00Z"))
        val useCase = GetRecentDocuments(FakeDocumentSource(device), InMemoryRecentStore(own))

        val result = useCase()

        assertThat(result.map { it.uri })
            .containsExactly("content://x/dev.pdf", "content://x/own.pdf")
    }

    @DisplayName("CA-2.2: respeita o limite")
    @Test
    fun `respects limit`() = runTest {
        val device = (1..3).map { doc("content://x/$it.pdf", modified = "2026-01-0${it}T00:00:00Z") }
        val source = FakeDocumentSource(device)
        val useCase = GetRecentDocuments(source, InMemoryRecentStore())

        val result = useCase(limit = 2)

        assertThat(result).hasSize(2)
        assertThat(source.lastLimit).isEqualTo(2)
    }

    @DisplayName("sem fontes devolve vazio")
    @Test
    fun `empty sources produce empty list`() = runTest {
        val useCase = GetRecentDocuments(FakeDocumentSource(emptyList()), InMemoryRecentStore())

        assertThat(useCase()).isEmpty()
    }
}
