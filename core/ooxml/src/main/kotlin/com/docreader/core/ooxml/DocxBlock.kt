package com.docreader.core.ooxml

/** Bloco de conteúdo de um documento DOCX. */
sealed interface DocxBlock {
    data class Heading(val level: Int, val text: String) : DocxBlock
    data class Paragraph(val runs: List<DocxRun>) : DocxBlock
    data class ListItem(val text: String, val ordered: Boolean = false) : DocxBlock
}

/** Trecho de texto com formatação inline. */
data class DocxRun(
    val text: String,
    val bold: Boolean = false,
    val italic: Boolean = false,
)
