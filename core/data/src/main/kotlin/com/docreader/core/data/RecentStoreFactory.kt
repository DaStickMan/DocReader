package com.docreader.core.data

import android.content.Context
import androidx.room.Room
import com.docreader.core.data.db.DocReaderDatabase
import com.docreader.core.ports.AnnotationStore
import com.docreader.core.ports.RecentStore

fun buildDocReaderDatabase(context: Context): DocReaderDatabase =
    Room.databaseBuilder(
        context.applicationContext,
        DocReaderDatabase::class.java,
        "docreader.db",
    ).fallbackToDestructiveMigration().build()

/** Cria o [RecentStore] persistido em Room. */
fun createRecentStore(context: Context): RecentStore =
    RoomRecentStore(buildDocReaderDatabase(context).recentDocumentDao())

/** Cria o [AnnotationStore] persistido em Room. */
fun createAnnotationStore(context: Context): AnnotationStore =
    RoomAnnotationStore(buildDocReaderDatabase(context).pdfAnnotationDao())
