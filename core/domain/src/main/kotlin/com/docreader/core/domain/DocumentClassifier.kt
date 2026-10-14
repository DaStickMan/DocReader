package com.docreader.core.domain

/** Classifica um arquivo em [DocumentType] pela extensão e, como fallback, pelo MIME. */
object DocumentClassifier {

    private val byExtension: Map<String, DocumentType> = mapOf(
        "pdf" to DocumentType.PDF,
        "docx" to DocumentType.DOCX,
        "md" to DocumentType.MARKDOWN,
        "markdown" to DocumentType.MARKDOWN,
    )

    private val byMime: Map<String, DocumentType> = mapOf(
        "application/pdf" to DocumentType.PDF,
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" to DocumentType.DOCX,
        "text/markdown" to DocumentType.MARKDOWN,
        "text/x-markdown" to DocumentType.MARKDOWN,
    )

    fun classify(displayName: String, mimeType: String? = null): DocumentType {
        val extension = displayName.substringAfterLast('.', missingDelimiterValue = "").lowercase()
        if (extension.isNotEmpty()) {
            byExtension[extension]?.let { return it }
        }

        val mime = mimeType?.substringBefore(';')?.trim()?.lowercase()
        if (!mime.isNullOrEmpty()) {
            byMime[mime]?.let { return it }
        }

        return DocumentType.OTHER
    }
}
