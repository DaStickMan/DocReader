package com.docreader.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface PdfAnnotationDao {

    @Query("SELECT * FROM pdf_annotations WHERE documentUri = :uri ORDER BY page ASC")
    suspend fun getByDocumentUri(uri: String): List<PdfAnnotationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<PdfAnnotationEntity>)

    @Query("DELETE FROM pdf_annotations WHERE documentUri = :uri")
    suspend fun deleteByDocumentUri(uri: String)

    @Transaction
    suspend fun replaceForDocument(uri: String, entities: List<PdfAnnotationEntity>) {
        deleteByDocumentUri(uri)
        if (entities.isNotEmpty()) {
            insertAll(entities)
        }
    }
}
