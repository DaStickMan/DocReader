package com.docreader.core.markdown

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** Como salvar as alterações de um documento. */
enum class SaveMode {
    /** Sobrescreve o arquivo original. */
    OVERWRITE,

    /** Gera uma cópia com carimbo de data. */
    COPY,
}

/** Decide o nome de destino ao salvar, sem tocar em I/O. */
object SavePlanner {

    private val stamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC)

    fun targetName(displayName: String, mode: SaveMode, timestamp: Instant): String = when (mode) {
        SaveMode.OVERWRITE -> displayName
        SaveMode.COPY -> {
            val dot = displayName.lastIndexOf('.')
            val hasExtension = dot in 1 until displayName.length - 1
            val base = if (hasExtension) displayName.substring(0, dot) else displayName
            val extension = if (hasExtension) displayName.substring(dot) else ""
            "$base (copy ${stamp.format(timestamp)})$extension"
        }
    }
}
