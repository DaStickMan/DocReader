package com.docreader.core.data

import android.content.Context
import androidx.room.Room
import com.docreader.core.data.db.DocReaderDatabase
import com.docreader.core.ports.RecentStore

/** Cria o [RecentStore] persistido em Room, sem expor os tipos do Room para o chamador. */
fun createRecentStore(context: Context): RecentStore =
    RoomRecentStore(
        Room.databaseBuilder(
            context.applicationContext,
            DocReaderDatabase::class.java,
            "docreader.db",
        ).build().recentDocumentDao(),
    )
