package com.docreader.core.ports

/** MIME types dos formatos suportados pelo app. */
object SupportedMimeTypes {
    const val PDF: String = "application/pdf"
    const val DOCX: String = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    const val MARKDOWN: String = "text/markdown"

    val ALL: List<String> = listOf(PDF, DOCX, MARKDOWN)
}

/** Ordenação da listagem de arquivos. */
enum class SortOrder {
    DATE_MODIFIED_DESC,
    DATE_MODIFIED_ASC,
    DISPLAY_NAME_ASC,
}

/**
 * Especificação de consulta ao provedor de arquivos do aparelho.
 * Implementações Android (MediaStore/SAF) traduzem isto para sua API nativa.
 */
data class DocumentQuery(
    val mimeTypes: List<String> = SupportedMimeTypes.ALL,
    val sort: SortOrder = SortOrder.DATE_MODIFIED_DESC,
    val limit: Int = 50,
)
