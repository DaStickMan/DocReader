package com.docreader.core.application

import com.docreader.core.domain.Document
import com.docreader.core.domain.RecentDocuments
import com.docreader.core.ports.DocumentSource
import com.docreader.core.ports.RecentStore

/** Monta a lista da tela "Abrir arquivo": próprios + varredura do aparelho, mesclados. */
class GetRecentDocuments(
    private val source: DocumentSource,
    private val store: RecentStore,
) {
    suspend operator fun invoke(limit: Int = RecentDocuments.DEFAULT_LIMIT): List<Document> =
        RecentDocuments.merge(
            own = store.all(),
            device = source.recentDocuments(limit),
            limit = limit,
        )
}
