package com.docreader.core.data

import com.docreader.core.data.db.PdfAnnotationDao
import com.docreader.core.data.db.PdfAnnotationMapper
import com.docreader.core.pdf.Annotation
import com.docreader.core.ports.AnnotationStore

/** Implementação de [AnnotationStore] sobre Room. */
class RoomAnnotationStore(private val dao: PdfAnnotationDao) : AnnotationStore {

    override suspend fun getAnnotations(uri: String): List<Annotation> =
        dao.getByDocumentUri(uri).mapNotNull(PdfAnnotationMapper::toDomain)

    override suspend fun saveAnnotations(uri: String, annotations: List<Annotation>) {
        val entities = annotations.map { PdfAnnotationMapper.toEntity(uri, it) }
        dao.replaceForDocument(uri, entities)
    }

    override suspend fun clearAnnotations(uri: String) {
        dao.deleteByDocumentUri(uri)
    }
}
