package com.docreader.core.data

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class PdfPageRendererFactoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val factory = PdfPageRendererFactory(context.contentResolver)

    @Test
    fun `returns null when the uri cannot be opened`() = runTest {
        val renderer = factory.open(Uri.parse("content://com.example.missing/doc/1"))

        assertThat(renderer).isNull()
    }

    @Test
    fun `rejects an out-of-range page index`() = runTest {
        val file = File(context.cacheDir, "doc.pdf").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val renderer = factory.open(Uri.fromFile(file))
        assertThat(renderer).isNotNull()
        val loaded = requireNotNull(renderer)

        val error = runCatching { loaded.renderPage(loaded.pageCount, 100) }.exceptionOrNull()
        loaded.close()

        assertThat(error).isInstanceOf(IllegalArgumentException::class.java)
    }
}
