package com.docreader.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.docreader.core.data.db.DocReaderDatabase
import com.docreader.core.domain.Document
import com.docreader.core.domain.DocumentType
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class RoomRecentStoreTest {

    private lateinit var database: DocReaderDatabase
    private lateinit var store: RoomRecentStore

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DocReaderDatabase::class.java,
        ).allowMainThreadQueries().build()
        store = RoomRecentStore(database.recentDocumentDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun document(uri: String, openedAt: Instant?) = Document(
        uri = uri,
        displayName = uri.substringAfterLast('/'),
        type = DocumentType.PDF,
        lastModified = Instant.ofEpochMilli(1_700_000_000_000),
        sizeBytes = 10,
        openedAt = openedAt,
    )

    @Test
    fun `upsert then all returns the document`() = runTest {
        val doc = document("content://x/a.pdf", openedAt = Instant.ofEpochMilli(1))

        store.upsert(doc)

        assertThat(store.all()).containsExactly(doc)
    }

    @Test
    fun `re-upserting the same uri keeps one record`() = runTest {
        store.upsert(document("content://x/a.pdf", openedAt = Instant.ofEpochMilli(1)))
        store.upsert(document("content://x/a.pdf", openedAt = Instant.ofEpochMilli(2)))

        val all = store.all()

        assertThat(all).hasSize(1)
        assertThat(all.single().openedAt).isEqualTo(Instant.ofEpochMilli(2))
    }

    @Test
    fun `remove deletes the record`() = runTest {
        store.upsert(document("content://x/a.pdf", openedAt = Instant.ofEpochMilli(1)))

        store.remove("content://x/a.pdf")

        assertThat(store.all()).isEmpty()
    }
}
