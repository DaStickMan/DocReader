package com.docreader.core.data

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/** Renderiza páginas de um PDF em bitmaps. Precisa ser fechado ao terminar. */
interface PdfPageRenderer : AutoCloseable {
    val pageCount: Int

    suspend fun renderPage(index: Int, targetWidth: Int): Bitmap
}

/** Abre PDFs a partir de uma uri. Retorna `null` quando o arquivo não é um PDF legível. */
class PdfPageRendererFactory(private val contentResolver: ContentResolver) {

    suspend fun open(uri: Uri): PdfPageRenderer? = withContext(Dispatchers.IO) {
        val descriptor = try {
            contentResolver.openFileDescriptor(uri, "r")
        } catch (_: IOException) {
            null
        } catch (_: SecurityException) {
            null
        }

        if (descriptor == null) return@withContext null

        AndroidPdfPageRenderer.open(descriptor).also { renderer ->
            if (renderer == null) descriptor.close()
        }
    }
}

private class AndroidPdfPageRenderer(
    private val descriptor: ParcelFileDescriptor,
    private val renderer: PdfRenderer,
) : PdfPageRenderer {

    override val pageCount: Int get() = renderer.pageCount

    override suspend fun renderPage(index: Int, targetWidth: Int): Bitmap = withContext(Dispatchers.IO) {
        require(index in 0 until renderer.pageCount) { "página $index fora do intervalo" }
        require(targetWidth > 0) { "targetWidth deve ser > 0" }

        renderer.openPage(index).use { page ->
            val scale = targetWidth.toFloat() / page.width
            val height = (page.height * scale).toInt().coerceAtLeast(1)
            val bitmap = Bitmap.createBitmap(targetWidth, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmap
        }
    }

    override fun close() {
        renderer.close()
        descriptor.close()
    }

    companion object {
        fun open(descriptor: ParcelFileDescriptor): AndroidPdfPageRenderer? = try {
            AndroidPdfPageRenderer(descriptor, PdfRenderer(descriptor))
        } catch (_: IOException) {
            // Arquivo corrompido ou protegido por senha.
            null
        }
    }
}
