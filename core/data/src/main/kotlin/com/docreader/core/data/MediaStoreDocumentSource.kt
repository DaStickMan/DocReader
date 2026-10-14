package com.docreader.core.data

import android.content.ContentResolver
import android.content.ContentUris
import android.os.Bundle
import android.provider.MediaStore
import com.docreader.core.domain.Document
import com.docreader.core.ports.DeviceFileMapper
import com.docreader.core.ports.DocumentQuery
import com.docreader.core.ports.DocumentSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

/**
 * Lista documentos recentes do aparelho via MediaStore, filtrando pelos MIME suportados.
 * Em Android 11+ exige "acesso a todos os arquivos" para enxergar documentos arbitrários.
 */
class MediaStoreDocumentSource(
    private val contentResolver: ContentResolver,
    private val query: DocumentQuery = DocumentQuery(),
) : DocumentSource {

    override suspend fun recentDocuments(limit: Int): List<Document> = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_MODIFIED,
        )
        val placeholders = query.mimeTypes.joinToString(separator = ",") { "?" }
        val args = Bundle().apply {
            putString(
                ContentResolver.QUERY_ARG_SQL_SELECTION,
                "${MediaStore.MediaColumns.MIME_TYPE} IN ($placeholders)",
            )
            putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, query.mimeTypes.toTypedArray())
            putString(
                ContentResolver.QUERY_ARG_SQL_SORT_ORDER,
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC",
            )
            putInt(ContentResolver.QUERY_ARG_LIMIT, limit.coerceAtLeast(0))
        }

        val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val documents = mutableListOf<Document>()

        contentResolver.query(collection, projection, args, null)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val modifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val displayName = cursor.getString(nameColumn) ?: continue
                val uri = ContentUris.withAppendedId(collection, id).toString()
                val mimeType = cursor.getString(mimeColumn)
                val sizeBytes = if (cursor.isNull(sizeColumn)) null else cursor.getLong(sizeColumn)
                val lastModified = Instant.ofEpochSecond(cursor.getLong(modifiedColumn))

                DeviceFileMapper.toDocument(uri, displayName, mimeType, sizeBytes, lastModified)
                    ?.let(documents::add)
            }
        }
        documents
    }
}
