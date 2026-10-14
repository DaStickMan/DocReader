package com.docreader.core.domain

/** Tipo de documento suportado pelo app. */
enum class DocumentType {
    PDF,
    DOCX,
    MARKDOWN,
    OTHER,
    ;

    /** Tipos que o app sabe abrir. */
    val isSupported: Boolean get() = this != OTHER
}
