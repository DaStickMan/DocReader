package com.docreader.core.ports

import com.docreader.core.domain.Document

/** Fonte de documentos existentes no aparelho (ex.: MediaStore, SAF). */
interface DocumentSource {
    suspend fun recentDocuments(limit: Int): List<Document>
}
