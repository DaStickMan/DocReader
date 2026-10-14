package com.docreader.core.pdf

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class PdfViewerStateTest {

    @DisplayName("CA-4.1: pageCount negativo é inválido")
    @Test
    fun `negative page count is rejected`() {
        assertThatThrownBy { PdfViewerState(pageCount = -1) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @DisplayName("CA-4.2: currentPage fora do intervalo é inválido")
    @Test
    fun `current page out of range is rejected`() {
        assertThatThrownBy { PdfViewerState(pageCount = 3, currentPage = 3) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @DisplayName("CA-4.3: zoom fora do intervalo é inválido")
    @Test
    fun `zoom out of range is rejected`() {
        assertThatThrownBy { PdfViewerState(pageCount = 3, zoom = 9f) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @DisplayName("CA-4.4: goTo faz clamp")
    @Test
    fun `goTo clamps to bounds`() {
        val state = PdfViewerState(pageCount = 3)

        assertThat(state.goTo(99).currentPage).isEqualTo(2)
        assertThat(state.goTo(-5).currentPage).isEqualTo(0)
    }

    @DisplayName("CA-4.5/CA-4.6: next/previous respeitam os limites")
    @Test
    fun `navigation is clamped at both ends`() {
        val first = PdfViewerState(pageCount = 3, currentPage = 0)
        val last = PdfViewerState(pageCount = 3, currentPage = 2)

        assertThat(first.previous().currentPage).isEqualTo(0)
        assertThat(last.next().currentPage).isEqualTo(2)
        assertThat(last.isLastPage).isTrue()
        assertThat(first.isFirstPage).isTrue()
        assertThat(first.next().currentPage).isEqualTo(1)
    }

    @DisplayName("CA-4.7: zoomBy faz clamp no máximo")
    @Test
    fun `zoomBy clamps`() {
        val state = PdfViewerState(pageCount = 1, zoom = PdfViewerState.MAX_ZOOM)

        assertThat(state.zoomBy(10f).zoom).isEqualTo(PdfViewerState.MAX_ZOOM)
        assertThat(state.zoomBy(0.0001f).zoom).isEqualTo(PdfViewerState.MIN_ZOOM)
    }

    @DisplayName("CA-4.8: documento vazio é válido e está na última página")
    @Test
    fun `empty document is valid`() {
        val state = PdfViewerState(pageCount = 0)

        assertThat(state.isLastPage).isTrue()
        assertThat(state.goTo(5).currentPage).isEqualTo(0)
        assertThat(state.next().currentPage).isEqualTo(0)
    }
}
