package com.docreader.app

import android.content.Context
import com.docreader.core.application.GetRecentDocuments
import com.docreader.core.application.OpenDocument
import com.docreader.core.application.RecentFilesStateHolder
import com.docreader.core.data.ContentUriDocumentLoader
import com.docreader.core.data.MediaStoreDocumentSource
import com.docreader.core.data.createRecentStore
import com.docreader.core.ports.Clock
import com.docreader.core.ports.DocumentSource
import com.docreader.core.ports.RecentStore
import java.time.Instant

/** Injeção de dependência manual, sem framework. */
class AppContainer(context: Context) {

    private val appContext: Context = context.applicationContext

    private val recentStore: RecentStore = createRecentStore(appContext)
    private val documentSource: DocumentSource = MediaStoreDocumentSource(appContext.contentResolver)
    private val clock: Clock = Clock { Instant.now() }

    val getRecentDocuments: GetRecentDocuments = GetRecentDocuments(documentSource, recentStore)
    val openDocument: OpenDocument = OpenDocument(recentStore, clock)
    val recentFilesStateHolder: RecentFilesStateHolder = RecentFilesStateHolder(getRecentDocuments)
    val documentLoader: ContentUriDocumentLoader = ContentUriDocumentLoader(appContext.contentResolver)
}
