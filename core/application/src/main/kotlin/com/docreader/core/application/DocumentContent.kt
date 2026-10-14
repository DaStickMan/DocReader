package com.docreader.core.application

import com.docreader.core.domain.Document
import com.docreader.core.ports.DocumentContentReader
import com.docreader.core.ports.DocumentTextWriter

/** Acesso ao conteúdo dos documentos, delegando aos ports de leitura/escrita. */
class DocumentContent(
    private val reader: DocumentContentReader,
    private val writer: DocumentTextWriter,
) {
    suspend fun readText(document: Document): String = reader.readText(document.uri)

    suspend fun readBytes(document: Document): ByteArray = reader.readBytes(document.uri)

    suspend fun saveText(uri: String, text: String) = writer.writeText(uri, text)
}
