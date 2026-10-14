package com.docreader.core.data

import com.docreader.core.data.db.RecentDocumentDao
import com.docreader.core.data.db.RecentDocumentMapper
import com.docreader.core.domain.Document
import com.docreader.core.ports.RecentStore

/** Implementação de [RecentStore] sobre Room. */
class RoomRecentStore(private val dao: RecentDocumentDao) : RecentStore {

    override suspend fun all(): List<Document> =
        dao.getAll().map(RecentDocumentMapper::toDomain)

    override suspend fun upsert(document: Document) =
        dao.upsert(RecentDocumentMapper.toEntity(document))

    override suspend fun remove(uri: String) {
        dao.deleteByUri(uri)
    }
}
