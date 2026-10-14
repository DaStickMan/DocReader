package com.docreader.core.application.fakes

import com.docreader.core.domain.Document
import com.docreader.core.ports.Clock
import com.docreader.core.ports.DocumentSource
import com.docreader.core.ports.RecentStore
import java.time.Instant

class InMemoryRecentStore(initial: List<Document> = emptyList()) : RecentStore {
    private val records = LinkedHashMap<String, Document>().apply {
        initial.forEach { put(it.uri, it) }
    }

    override suspend fun all(): List<Document> = records.values.toList()

    override suspend fun upsert(document: Document) {
        records[document.uri] = document
    }

    override suspend fun remove(uri: String) {
        records.remove(uri)
    }
}

class FakeDocumentSource(private val documents: List<Document>) : DocumentSource {
    var lastLimit: Int? = null
        private set

    override suspend fun recentDocuments(limit: Int): List<Document> {
        lastLimit = limit
        return documents
    }
}

class MutableClock(private var current: Instant) : Clock {
    override fun now(): Instant = current
    fun advanceTo(instant: Instant) {
        current = instant
    }
}
