package com.docreader.core.ports

import com.docreader.core.domain.Document

/** Persistência dos documentos abertos pelo app (registos "próprios"). */
interface RecentStore {
    suspend fun all(): List<Document>
    suspend fun upsert(document: Document)
    suspend fun remove(uri: String)
}
