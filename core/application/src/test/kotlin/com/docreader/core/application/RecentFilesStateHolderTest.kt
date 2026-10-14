package com.docreader.core.application

import com.docreader.core.application.fakes.FakeDocumentSource
import com.docreader.core.application.fakes.InMemoryRecentStore
import com.docreader.core.domain.Document
import com.docreader.core.domain.DocumentType
import com.docreader.core.ports.DocumentSource
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.IOException
import java.time.Instant

class RecentFilesStateHolderTest {

    private fun doc(uri: String, modified: String) = Document(
        uri = uri,
        displayName = uri.substringAfterLast('/'),
        type = DocumentType.PDF,
        lastModified = Instant.parse(modified),
    )

    @Test
    @DisplayName("estado inicial está ocioso")
    fun `initial state is idle`() {
        val holder = RecentFilesStateHolder(
            GetRecentDocuments(FakeDocumentSource(emptyList()), InMemoryRecentStore()),
        )

        assertThat(holder.state.value).isEqualTo(RecentFilesUiState())
    }

    @Test
    @DisplayName("refresh com sucesso publica os documentos")
    fun `refresh publishes documents`() = runTest {
        val device = listOf(doc("content://x/a.pdf", "2026-01-01T00:00:00Z"))
        val holder = RecentFilesStateHolder(
            GetRecentDocuments(FakeDocumentSource(device), InMemoryRecentStore()),
        )

        holder.refresh()

        assertThat(holder.state.value.documents).hasSize(1)
        assertThat(holder.state.value.isLoading).isFalse()
        assertThat(holder.state.value.errorMessage).isNull()
    }

    @Test
    @DisplayName("falha na fonte vira mensagem de erro (sem lançar)")
    fun `source failure becomes error state`() = runTest {
        val failing = object : DocumentSource {
            override suspend fun recentDocuments(limit: Int): List<Document> =
                throw IOException("sem permissão")
        }
        val holder = RecentFilesStateHolder(GetRecentDocuments(failing, InMemoryRecentStore()))

        holder.refresh()

        assertThat(holder.state.value.errorMessage).isEqualTo("sem permissão")
        assertThat(holder.state.value.documents).isEmpty()
        assertThat(holder.state.value.isLoading).isFalse()
    }

    @Test
    @DisplayName("erro é limpo em um refresh bem-sucedido seguinte")
    fun `successful refresh clears previous error`() = runTest {
        val flaky = object : DocumentSource {
            var fail = true
            override suspend fun recentDocuments(limit: Int): List<Document> {
                if (fail) throw IOException("instável")
                return listOf(doc("content://x/a.pdf", "2026-01-01T00:00:00Z"))
            }
        }
        val holder = RecentFilesStateHolder(GetRecentDocuments(flaky, InMemoryRecentStore()))

        holder.refresh()
        flaky.fail = false
        holder.refresh()

        assertThat(holder.state.value.errorMessage).isNull()
        assertThat(holder.state.value.documents).hasSize(1)
    }
}
