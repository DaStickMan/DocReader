package com.docreader.core.data.db

import com.docreader.core.domain.Document
import com.docreader.core.domain.DocumentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.Instant

class RecentDocumentMapperTest {

    @Test
    fun `round trips a document`() {
        val document = Document(
            uri = "content://x/doc.pdf",
            displayName = "doc.pdf",
            type = DocumentType.PDF,
            lastModified = Instant.ofEpochMilli(1_700_000_000_000),
            sizeBytes = 1234,
            openedAt = Instant.ofEpochMilli(1_800_000_000_000),
        )

        val result = RecentDocumentMapper.toDomain(RecentDocumentMapper.toEntity(document))

        assertThat(result).isEqualTo(document)
    }

    @Test
    fun `unknown stored type falls back to OTHER`() {
        val entity = RecentDocumentEntity(
            uri = "content://x/legacy",
            displayName = "legacy",
            type = "SOMETHING_ELSE",
            lastModified = 0,
            sizeBytes = null,
            openedAt = null,
        )

        assertThat(RecentDocumentMapper.toDomain(entity).type).isEqualTo(DocumentType.OTHER)
    }
}
