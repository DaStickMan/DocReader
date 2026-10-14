package com.docreader.core.pdf

import kotlin.math.max

/**
 * Estado de navegação e zoom do leitor de PDF. Não depende de renderização.
 * Um documento vazio (`pageCount == 0`) é válido: não há páginas para navegar.
 */
data class PdfViewerState(
    val pageCount: Int,
    val currentPage: Int = 0,
    val zoom: Float = MIN_ZOOM,
) {
    init {
        require(pageCount >= 0) { "pageCount must be >= 0, was $pageCount" }
        require(currentPage in 0 until max(1, pageCount)) {
            "currentPage must be in 0 until ${max(1, pageCount)}, was $currentPage"
        }
        require(zoom in MIN_ZOOM..MAX_ZOOM) {
            "zoom must be in [$MIN_ZOOM, $MAX_ZOOM], was $zoom"
        }
    }

    val isFirstPage: Boolean get() = currentPage <= 0

    // Não há página depois da atual (vazio: verdadeiro, portanto a navegação é no-op).
    val isLastPage: Boolean get() = currentPage >= pageCount - 1

    fun goTo(page: Int): PdfViewerState =
        copy(currentPage = page.coerceIn(0, max(0, pageCount - 1)))

    fun next(): PdfViewerState = if (isLastPage) this else copy(currentPage = currentPage + 1)

    fun previous(): PdfViewerState = if (isFirstPage) this else copy(currentPage = currentPage - 1)

    fun zoomBy(factor: Float): PdfViewerState =
        copy(zoom = (zoom * factor).coerceIn(MIN_ZOOM, MAX_ZOOM))

    companion object {
        const val MIN_ZOOM: Float = 1f
        const val MAX_ZOOM: Float = 5f
    }
}
