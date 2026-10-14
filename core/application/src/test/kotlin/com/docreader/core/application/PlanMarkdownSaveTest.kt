package com.docreader.core.application

import com.docreader.core.application.fakes.MutableClock
import com.docreader.core.domain.Document
import com.docreader.core.domain.DocumentType
import com.docreader.core.markdown.SaveMode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant

class PlanMarkdownSaveTest {

    private val document = Document(
        uri = "content://x/nota.md",
        displayName = "nota.md",
        type = DocumentType.MARKDOWN,
        lastModified = Instant.parse("2026-01-01T00:00:00Z"),
    )

    private val plan = PlanMarkdownSave(MutableClock(Instant.parse("2026-06-01T12:00:00Z")))

    @Test
    @DisplayName("OVERWRITE aponta para a própria uri")
    fun `overwrite targets the document uri`() {
        val result = plan(document, SaveMode.OVERWRITE)

        assertThat(result.targetUri).isEqualTo("content://x/nota.md")
        assertThat(result.suggestedName).isEqualTo("nota.md")
    }

    @Test
    @DisplayName("COPY não tem uri de destino e sugere nome com carimbo")
    fun `copy has no target uri and suggests a stamped name`() {
        val result = plan(document, SaveMode.COPY)

        assertThat(result.targetUri).isNull()
        assertThat(result.suggestedName).isEqualTo("nota (copy 20260601-120000).md")
    }
}
