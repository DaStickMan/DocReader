package com.docreader.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        RecentDocumentEntity::class,
        PdfAnnotationEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class DocReaderDatabase : RoomDatabase() {
    abstract fun recentDocumentDao(): RecentDocumentDao
    abstract fun pdfAnnotationDao(): PdfAnnotationDao
}
