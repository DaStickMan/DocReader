package com.docreader.core.application

import com.docreader.core.application.fakes.InMemoryRecentStore
import com.docreader.core.application.fakes.MutableClock
import com.docreader.core.domain.Document
import com.docreader.core.domain.DocumentType
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant

class OpenDocumentTest {

    private val document = Document(
        uri = "content://x/doc.pdf",
        displayName = "doc.pdf",
        type = DocumentType.PDF,
        lastModified = Instant.parse("2026-01-01T00:00:00Z"),
    )

    @DisplayName("CA-2.3: registra openedAt e persiste no store")
    @Test
    fun `records openedAt and persists`() = runTest {
        val openedAt = Instant.parse("2026-06-01T12:00:00Z")
        val store = InMemoryRecentStore()
        val useCase = OpenDocument(store, MutableClock(openedAt))

        val result = useCase(document)

        assertThat(result.openedAt).isEqualTo(openedAt)
        assertThat(store.all()).containsExactly(result)
    }

    @DisplayName("CA-2.4: reabrir mantém 1 registro com o openedAt mais novo")
    @Test
    fun `reopening keeps a single record with newest openedAt`() = runTest {
        val clock = MutableClock(Instant.parse("2026-06-01T12:00:00Z"))
        val store = InMemoryRecentStore()
        val useCase = OpenDocument(store, clock)

        useCase(document)
        clock.advanceTo(Instant.parse("2026-06-02T09:00:00Z"))
        val second = useCase(document)

        assertThat(store.all()).hasSize(1)
        assertThat(store.all().single().openedAt).isEqualTo(Instant.parse("2026-06-02T09:00:00Z"))
        assertThat(second.openedAt).isEqualTo(Instant.parse("2026-06-02T09:00:00Z"))
    }
}
