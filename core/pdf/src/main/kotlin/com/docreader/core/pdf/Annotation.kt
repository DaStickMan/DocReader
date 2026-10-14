package com.docreader.core.pdf

/** Ponto em coordenadas de página do PDF. */
data class PdfPoint(val x: Float, val y: Float)

/** Retângulo alinhado aos eixos, em coordenadas de página. */
data class Rect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    init {
        require(left <= right) { "left ($left) must be <= right ($right)" }
        require(top <= bottom) { "top ($top) must be <= bottom ($bottom)" }
    }

    val width: Float get() = right - left
    val height: Float get() = bottom - top
}

/** Anotação feita pelo usuário sobre um PDF. */
sealed interface Annotation {
    val id: String
    val page: Int
}

data class Highlight(
    override val id: String,
    override val page: Int,
    val rect: Rect,
    val colorArgb: Int,
    val note: String? = null,
) : Annotation

data class StickyNote(
    override val id: String,
    override val page: Int,
    val anchor: PdfPoint,
    val text: String,
) : Annotation

data class Freehand(
    override val id: String,
    override val page: Int,
    val points: List<PdfPoint>,
    val colorArgb: Int,
    val width: Float,
) : Annotation
