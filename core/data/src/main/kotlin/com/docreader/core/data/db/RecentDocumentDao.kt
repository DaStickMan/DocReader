package com.docreader.core.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface RecentDocumentDao {

    @Query(
        "SELECT * FROM recent_documents " +
            "ORDER BY openedAt IS NULL, openedAt DESC, lastModified DESC",
    )
    suspend fun getAll(): List<RecentDocumentEntity>

    @Upsert
    suspend fun upsert(entity: RecentDocumentEntity)

    @Query("DELETE FROM recent_documents WHERE uri = :uri")
    suspend fun deleteByUri(uri: String)
}
