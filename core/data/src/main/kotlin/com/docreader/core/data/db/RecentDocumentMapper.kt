package com.docreader.core.data.db

import com.docreader.core.domain.Document
import com.docreader.core.domain.DocumentType
import java.time.Instant

object RecentDocumentMapper {

    fun toEntity(document: Document): RecentDocumentEntity = RecentDocumentEntity(
        uri = document.uri,
        displayName = document.displayName,
        type = document.type.name,
        lastModified = document.lastModified.toEpochMilli(),
        sizeBytes = document.sizeBytes,
        openedAt = document.openedAt?.toEpochMilli(),
    )

    fun toDomain(entity: RecentDocumentEntity): Document = Document(
        uri = entity.uri,
        displayName = entity.displayName,
        type = parseType(entity.type),
        lastModified = Instant.ofEpochMilli(entity.lastModified),
        sizeBytes = entity.sizeBytes,
        openedAt = entity.openedAt?.let(Instant::ofEpochMilli),
    )

    private fun parseType(raw: String): DocumentType =
        DocumentType.entries.firstOrNull { it.name == raw } ?: DocumentType.OTHER
}
