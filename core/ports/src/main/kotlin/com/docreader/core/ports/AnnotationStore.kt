package com.docreader.core.ports

import com.docreader.core.pdf.Annotation

/** Persistência das anotações de documentos (organizadas por uri). */
interface AnnotationStore {
    suspend fun getAnnotations(uri: String): List<Annotation>
    suspend fun saveAnnotations(uri: String, annotations: List<Annotation>)
    suspend fun clearAnnotations(uri: String)
}
