package com.docreader.core.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_documents")
data class RecentDocumentEntity(
    @PrimaryKey val uri: String,
    val displayName: String,
    val type: String,
    val lastModified: Long,
    val sizeBytes: Long?,
    val openedAt: Long?,
)
