package com.docreader.core.application

import com.docreader.core.domain.Document
import com.docreader.core.markdown.SaveMode
import com.docreader.core.markdown.SavePlanner
import com.docreader.core.ports.Clock

/** Destino de um salvamento de Markdown. */
data class MarkdownSavePlan(
    /** Uri de destino quando já é conhecido (sobrescrever); `null` quando o usuário escolhe (SAF). */
    val targetUri: String?,
    val suggestedName: String,
    val mode: SaveMode,
)

/** Decide o destino do salvamento de um Markdown. */
class PlanMarkdownSave(private val clock: Clock) {

    operator fun invoke(document: Document, mode: SaveMode): MarkdownSavePlan = when (mode) {
        SaveMode.OVERWRITE -> MarkdownSavePlan(
            targetUri = document.uri,
            suggestedName = document.displayName,
            mode = mode,
        )

        SaveMode.COPY -> MarkdownSavePlan(
            targetUri = null,
            suggestedName = SavePlanner.targetName(document.displayName, mode, clock.now()),
            mode = mode,
        )
    }
}
