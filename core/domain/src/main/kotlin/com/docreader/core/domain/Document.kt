package com.docreader.core.domain

import java.time.Instant

/**
 * Um documento conhecido pelo app, identificado por sua [uri] (`content://` ou `file://`).
 *
 * @param lastModified data de modificação do arquivo no dispositivo.
 * @param openedAt quando o usuário abriu o arquivo através deste app (null se nunca).
 */
data class Document(
    val uri: String,
    val displayName: String,
    val type: DocumentType,
    val lastModified: Instant,
    val sizeBytes: Long? = null,
    val openedAt: Instant? = null,
)
