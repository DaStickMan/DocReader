package com.docreader.core.ports

/** Lê o conteúdo de um documento a partir da sua uri. */
interface DocumentContentReader {
    suspend fun readBytes(uri: String): ByteArray
    suspend fun readText(uri: String): String
}

/** Grava texto em um documento (sobrescrevendo). */
interface DocumentTextWriter {
    suspend fun writeText(uri: String, text: String)
}
