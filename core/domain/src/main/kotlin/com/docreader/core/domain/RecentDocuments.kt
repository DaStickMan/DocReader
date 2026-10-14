package com.docreader.core.domain

/** Combina os recentes do aparelho com os arquivos já abertos pelo app. */
object RecentDocuments {

    const val DEFAULT_LIMIT: Int = 50

    /**
     * @param own arquivos abertos pelo app (registos persistidos, autoritativos).
     * @param device arquivos encontrados no aparelho (varredura MediaStore).
     * @return tipos suportados, deduplicados por uri, do mais recente para o mais antigo.
     */
    fun merge(
        own: List<Document>,
        device: List<Document>,
        limit: Int = DEFAULT_LIMIT,
    ): List<Document> {
        if (limit <= 0) return emptyList()

        val byUri = LinkedHashMap<String, Document>()
        own.asSequence().filter { it.type.isSupported }.forEach { byUri[it.uri] = it }
        device.asSequence().filter { it.type.isSupported }.forEach { deviceDoc ->
            val ownDoc = byUri[deviceDoc.uri]
            byUri[deviceDoc.uri] = if (ownDoc == null) deviceDoc else mergeDuplicates(ownDoc, deviceDoc)
        }

        return byUri.values
            .sortedWith(
                compareByDescending<Document> { it.lastModified }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.displayName },
            )
            .take(limit)
    }

    /** Campos de [own] prevalecem; [Document.lastModified] mantém o mais recente dos dois. */
    private fun mergeDuplicates(own: Document, device: Document): Document = own.copy(
        displayName = own.displayName.ifBlank { device.displayName },
        lastModified = maxOf(own.lastModified, device.lastModified),
        sizeBytes = own.sizeBytes ?: device.sizeBytes,
    )
}
