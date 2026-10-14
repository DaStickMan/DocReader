package com.docreader.core.application

import com.docreader.core.domain.Document
import com.docreader.core.ports.Clock
import com.docreader.core.ports.RecentStore

/** Marca o documento como aberto agora e persiste no store. */
class OpenDocument(
    private val store: RecentStore,
    private val clock: Clock,
) {
    suspend operator fun invoke(document: Document): Document {
        val opened = document.copy(openedAt = clock.now())
        store.upsert(opened)
        return opened
    }
}
