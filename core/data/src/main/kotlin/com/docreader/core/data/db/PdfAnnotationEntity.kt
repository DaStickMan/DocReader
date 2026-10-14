package com.docreader.core.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pdf_annotations",
    indices = [Index(value = ["documentUri", "page"])],
)
data class PdfAnnotationEntity(
    @PrimaryKey val id: String,
    val documentUri: String,
    val page: Int,
    val type: String, // "HIGHLIGHT", "STICKY_NOTE", "FREEHAND"
    val colorArgb: Int,
    val strokeWidth: Float?,
    val noteOrText: String?,
    val rectLeft: Float?,
    val rectTop: Float?,
    val rectRight: Float?,
    val rectBottom: Float?,
    val pointsData: String?,
)
