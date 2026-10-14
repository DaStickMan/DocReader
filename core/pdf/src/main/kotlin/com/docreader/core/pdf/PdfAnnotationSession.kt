package com.docreader.core.pdf

import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/** Ferramentas disponíveis para interação e anotação sobre o PDF. */
enum class AnnotationTool {
    VIEW,
    HIGHLIGHT,
    STICKY_NOTE,
    FREEHAND,
}

/**
 * Mantém e gerencia a sessão de anotações sobre um PDF.
 * Modelo puro, independente do framework Android.
 */
data class PdfAnnotationSession(
    val pageCount: Int,
    val annotations: List<Annotation> = emptyList(),
    val activeTool: AnnotationTool = AnnotationTool.VIEW,
    val selectedColorArgb: Int = DEFAULT_COLOR,
    val strokeWidth: Float = DEFAULT_STROKE_WIDTH,
) {
    init {
        require(pageCount >= 0) { "pageCount must be >= 0, was $pageCount" }
    }

    fun annotationsFor(page: Int): List<Annotation> =
        annotations.filter { it.page == page }

    fun add(annotation: Annotation): Result<PdfAnnotationSession> {
        val issues = AnnotationValidator.validate(listOf(annotation), pageCount)
        if (issues.isNotEmpty()) {
            val reasons = issues.joinToString("; ") { it.reason }
            return Result.failure(IllegalArgumentException(reasons))
        }
        return Result.success(copy(annotations = annotations + annotation))
    }

    fun remove(id: String): PdfAnnotationSession =
        copy(annotations = annotations.filterNot { it.id == id })

    fun undoLast(page: Int? = null): PdfAnnotationSession {
        val target = if (page != null) {
            annotations.filter { it.page == page }.lastOrNull()
        } else {
            annotations.lastOrNull()
        } ?: return this

        return copy(annotations = annotations.filterNot { it.id == target.id })
    }

    fun clearPage(page: Int): PdfAnnotationSession =
        copy(annotations = annotations.filterNot { it.page == page })

    fun clearAll(): PdfAnnotationSession =
        copy(annotations = emptyList())

    fun setTool(tool: AnnotationTool): PdfAnnotationSession =
        copy(activeTool = tool)

    fun setColor(colorArgb: Int): PdfAnnotationSession =
        copy(selectedColorArgb = colorArgb)

    fun setStrokeWidth(width: Float): PdfAnnotationSession =
        copy(strokeWidth = width.coerceAtLeast(1f))

    fun findAnnotationAt(page: Int, point: PdfPoint, tolerance: Float = 20f): Annotation? {
        val pageAnnotations = annotationsFor(page)
        for (i in pageAnnotations.indices.reversed()) {
            val candidate = pageAnnotations[i]
            if (isHit(candidate, point, tolerance)) {
                return candidate
            }
        }
        return null
    }

    private fun isHit(annotation: Annotation, point: PdfPoint, tolerance: Float): Boolean = when (annotation) {
        is StickyNote -> {
            hypot(point.x - annotation.anchor.x, point.y - annotation.anchor.y) <= tolerance
        }

        is Highlight -> {
            point.x in (annotation.rect.left - tolerance)..(annotation.rect.right + tolerance) &&
                point.y in (annotation.rect.top - tolerance)..(annotation.rect.bottom + tolerance)
        }

        is Freehand -> {
            val points = annotation.points
            var hit = false
            for (j in 0 until points.size - 1) {
                if (distanceToSegment(point, points[j], points[j + 1]) <= tolerance + annotation.width / 2f) {
                    hit = true
                    break
                }
            }
            hit
        }
    }

    private fun distanceToSegment(p: PdfPoint, a: PdfPoint, b: PdfPoint): Float {
        val dx = b.x - a.x
        val dy = b.y - a.y
        val lengthSq = dx * dx + dy * dy
        if (lengthSq == 0f) return hypot(p.x - a.x, p.y - a.y)

        val t = max(0f, min(1f, ((p.x - a.x) * dx + (p.y - a.y) * dy) / lengthSq))
        val projX = a.x + t * dx
        val projY = a.y + t * dy
        return hypot(p.x - projX, p.y - projY)
    }

    companion object {
        const val DEFAULT_COLOR: Int = 0xFFFFEB3B.toInt()
        const val DEFAULT_STROKE_WIDTH: Float = 4f
    }
}
