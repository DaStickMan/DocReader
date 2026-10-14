package com.docreader.core.data

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import com.docreader.core.domain.Document
import com.docreader.core.ports.DeviceFileMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

/** Converte um `content://` obtido via SAF em [Document] (metadados do provedor). */
class ContentUriDocumentLoader(private val contentResolver: ContentResolver) {

    suspend fun load(uri: Uri): Document? = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            OpenableColumns.DISPLAY_NAME,
            OpenableColumns.SIZE,
            MediaStore.MediaColumns.DATE_MODIFIED,
        )
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null

            val nameColumn = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndex(OpenableColumns.SIZE)
            val modifiedColumn = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)

            val displayName = if (nameColumn >= 0 && !cursor.isNull(nameColumn)) {
                cursor.getString(nameColumn)
            } else {
                uri.lastPathSegment ?: "documento"
            }
            val sizeBytes = if (sizeColumn >= 0 && !cursor.isNull(sizeColumn)) {
                cursor.getLong(sizeColumn)
            } else {
                null
            }
            val lastModified = if (modifiedColumn >= 0 && !cursor.isNull(modifiedColumn)) {
                Instant.ofEpochSecond(cursor.getLong(modifiedColumn))
            } else {
                Instant.EPOCH
            }

            DeviceFileMapper.toDocument(
                uri = uri.toString(),
                displayName = displayName,
                mimeType = contentResolver.getType(uri),
                sizeBytes = sizeBytes,
                lastModified = lastModified,
            )
        }
    }
}
