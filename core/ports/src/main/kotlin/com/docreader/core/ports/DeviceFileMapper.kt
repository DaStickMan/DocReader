package com.docreader.core.ports

import com.docreader.core.domain.Document
import com.docreader.core.domain.DocumentClassifier
import java.time.Instant

/** Converte uma linha de provedor de arquivos (MediaStore/SAF) em [Document]. */
object DeviceFileMapper {

    /** @return o [Document] correspondente, ou `null` se o tipo não for suportado. */
    fun toDocument(
        uri: String,
        displayName: String,
        mimeType: String?,
        sizeBytes: Long?,
        lastModified: Instant,
    ): Document? {
        val type = DocumentClassifier.classify(displayName, mimeType)
        if (!type.isSupported) return null
        return Document(
            uri = uri,
            displayName = displayName,
            type = type,
            lastModified = lastModified,
            sizeBytes = sizeBytes,
        )
    }
}
