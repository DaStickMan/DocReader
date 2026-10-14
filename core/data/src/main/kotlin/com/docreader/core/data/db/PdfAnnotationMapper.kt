package com.docreader.core.data.db

import com.docreader.core.pdf.Annotation
import com.docreader.core.pdf.Freehand
import com.docreader.core.pdf.Highlight
import com.docreader.core.pdf.PdfPoint
import com.docreader.core.pdf.Rect
import com.docreader.core.pdf.StickyNote

object PdfAnnotationMapper {

    fun toEntity(documentUri: String, annotation: Annotation): PdfAnnotationEntity = when (annotation) {
        is Highlight -> PdfAnnotationEntity(
            id = annotation.id,
            documentUri = documentUri,
            page = annotation.page,
            type = "HIGHLIGHT",
            colorArgb = annotation.colorArgb,
            strokeWidth = null,
            noteOrText = annotation.note,
            rectLeft = annotation.rect.left,
            rectTop = annotation.rect.top,
            rectRight = annotation.rect.right,
            rectBottom = annotation.rect.bottom,
            pointsData = null,
        )

        is StickyNote -> PdfAnnotationEntity(
            id = annotation.id,
            documentUri = documentUri,
            page = annotation.page,
            type = "STICKY_NOTE",
            colorArgb = 0xFFFFEB3B.toInt(),
            strokeWidth = null,
            noteOrText = annotation.text,
            rectLeft = annotation.anchor.x,
            rectTop = annotation.anchor.y,
            rectRight = null,
            rectBottom = null,
            pointsData = null,
        )

        is Freehand -> PdfAnnotationEntity(
            id = annotation.id,
            documentUri = documentUri,
            page = annotation.page,
            type = "FREEHAND",
            colorArgb = annotation.colorArgb,
            strokeWidth = annotation.width,
            noteOrText = null,
            rectLeft = null,
            rectTop = null,
            rectRight = null,
            rectBottom = null,
            pointsData = annotation.points.joinToString(";") { "${it.x},${it.y}" },
        )
    }

    fun toDomain(entity: PdfAnnotationEntity): Annotation? = when (entity.type) {
        "HIGHLIGHT" -> {
            val left = entity.rectLeft ?: return null
            val top = entity.rectTop ?: return null
            val right = entity.rectRight ?: return null
            val bottom = entity.rectBottom ?: return null
            Highlight(
                id = entity.id,
                page = entity.page,
                rect = Rect(left, top, right, bottom),
                colorArgb = entity.colorArgb,
                note = entity.noteOrText,
            )
        }

        "STICKY_NOTE" -> {
            val x = entity.rectLeft ?: return null
            val y = entity.rectTop ?: return null
            StickyNote(
                id = entity.id,
                page = entity.page,
                anchor = PdfPoint(x, y),
                text = entity.noteOrText.orEmpty(),
            )
        }

        "FREEHAND" -> {
            val raw = entity.pointsData.orEmpty()
            val points = raw.split(";").mapNotNull { segment ->
                val parts = segment.split(",")
                if (parts.size == 2) {
                    val px = parts[0].toFloatOrNull()
                    val py = parts[1].toFloatOrNull()
                    if (px != null && py != null) PdfPoint(px, py) else null
                } else null
            }
            if (points.size < 2) null
            else Freehand(
                id = entity.id,
                page = entity.page,
                points = points,
                colorArgb = entity.colorArgb,
                width = entity.strokeWidth ?: 3f,
            )
        }

        else -> null
    }
}
