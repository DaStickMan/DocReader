package com.docreader.core.data

import android.content.ContentResolver
import android.net.Uri
import com.docreader.core.ports.DocumentContentReader
import com.docreader.core.ports.DocumentTextWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/** Lê bytes/texto de uma uri via [ContentResolver]. */
class ContentUriContentReader(private val contentResolver: ContentResolver) : DocumentContentReader {

    override suspend fun readBytes(uri: String): ByteArray = withContext(Dispatchers.IO) {
        contentResolver.openInputStream(Uri.parse(uri))?.use { it.readBytes() } ?: ByteArray(0)
    }

    override suspend fun readText(uri: String): String = withContext(Dispatchers.IO) {
        contentResolver.openInputStream(Uri.parse(uri))
            ?.use { it.readBytes().toString(Charsets.UTF_8) }
            ?: ""
    }
}

/** Sobrescreve o conteúdo de uma uri com texto UTF-8. */
class ContentUriTextWriter(private val contentResolver: ContentResolver) : DocumentTextWriter {

    override suspend fun writeText(uri: String, text: String) = withContext(Dispatchers.IO) {
        val output = contentResolver.openOutputStream(Uri.parse(uri), "wt")
            ?: throw IOException("Não foi possível abrir $uri para escrita")
        output.use { it.write(text.toByteArray(Charsets.UTF_8)) }
    }
}
