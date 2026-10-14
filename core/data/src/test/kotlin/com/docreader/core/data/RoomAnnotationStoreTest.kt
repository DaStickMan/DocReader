package com.docreader.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.docreader.core.data.db.DocReaderDatabase
import com.docreader.core.pdf.Freehand
import com.docreader.core.pdf.Highlight
import com.docreader.core.pdf.PdfPoint
import com.docreader.core.pdf.Rect
import com.docreader.core.pdf.StickyNote
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoomAnnotationStoreTest {

    private lateinit var database: DocReaderDatabase
    private lateinit var store: RoomAnnotationStore

    private val highlight = Highlight(
        id = "h1",
        page = 0,
        rect = Rect(10f, 20f, 100f, 50f),
        colorArgb = 0xFFFFEB3B.toInt(),
        note = "Destaque 1",
    )

    private val stickyNote = StickyNote(
        id = "s1",
        page = 0,
        anchor = PdfPoint(25f, 35f),
        text = "Lembrete importante",
    )

    private val freehand = Freehand(
        id = "f1",
        page = 1,
        points = listOf(PdfPoint(0f, 0f), PdfPoint(15f, 15f), PdfPoint(30f, 10f)),
        colorArgb = 0xFF000000.toInt(),
        width = 4f,
    )

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DocReaderDatabase::class.java,
        ).allowMainThreadQueries().build()
        store = RoomAnnotationStore(database.pdfAnnotationDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `save and get annotations returns saved list`() = runTest {
        val uri = "content://docs/manual.pdf"
        val list = listOf(highlight, stickyNote, freehand)

        store.saveAnnotations(uri, list)

        val retrieved = store.getAnnotations(uri)
        assertThat(retrieved).hasSize(3)
        assertThat(retrieved[0]).isEqualTo(highlight)
        assertThat(retrieved[1]).isEqualTo(stickyNote)
        assertThat(retrieved[2]).isEqualTo(freehand)
    }

    @Test
    fun `re-saving replaces previous annotations for that document`() = runTest {
        val uri = "content://docs/manual.pdf"
        store.saveAnnotations(uri, listOf(highlight, stickyNote))

        store.saveAnnotations(uri, listOf(freehand))

        val retrieved = store.getAnnotations(uri)
        assertThat(retrieved).containsExactly(freehand)
    }

    @Test
    fun `clearAnnotations removes only the targeted document annotations`() = runTest {
        val uri1 = "content://docs/doc1.pdf"
        val uri2 = "content://docs/doc2.pdf"

        store.saveAnnotations(uri1, listOf(highlight))
        store.saveAnnotations(uri2, listOf(stickyNote))

        store.clearAnnotations(uri1)

        assertThat(store.getAnnotations(uri1)).isEmpty()
        assertThat(store.getAnnotations(uri2)).containsExactly(stickyNote)
    }
}
