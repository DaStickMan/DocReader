package com.docreader.core.data

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import java.io.File

/** Provedor de teste que serve um único arquivo em cache. */
class InMemoryDocumentProvider : ContentProvider() {
    private lateinit var file: File

    override fun onCreate(): Boolean {
        file = File(requireNotNull(context).cacheDir, "doc.txt")
        return true
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        var flags = ParcelFileDescriptor.MODE_READ_WRITE or ParcelFileDescriptor.MODE_CREATE
        if (mode.contains('w') || mode.contains('t')) {
            flags = flags or ParcelFileDescriptor.MODE_TRUNCATE
        }
        return ParcelFileDescriptor.open(file, flags)
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = "text/markdown"

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0
}

@RunWith(RobolectricTestRunner::class)
class ContentUriContentTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val reader = ContentUriContentReader(context.contentResolver)
    private val writer = ContentUriTextWriter(context.contentResolver)

    @Test
    fun `writes then reads text back`() = runTest {
        Robolectric.setupContentProvider(InMemoryDocumentProvider::class.java, "test.provider")
        val uri = Uri.parse("content://test.provider/doc.txt")

        writer.writeText(uri.toString(), "# Olá\nconteúdo")

        assertThat(reader.readText(uri.toString())).isEqualTo("# Olá\nconteúdo")
        assertThat(reader.readBytes(uri.toString())).isNotEmpty()
    }
}
