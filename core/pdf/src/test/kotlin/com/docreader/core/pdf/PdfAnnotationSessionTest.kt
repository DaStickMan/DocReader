package com.docreader.core.pdf

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class PdfAnnotationSessionTest {

    private val validHighlight = Highlight(
        id = "h1",
        page = 0,
        rect = Rect(left = 10f, top = 20f, right = 100f, bottom = 50f),
        colorArgb = 0xFFFFEB3B.toInt(),
        note = "Importante",
    )

    private val validStickyNote = StickyNote(
        id = "n1",
        page = 0,
        anchor = PdfPoint(50f, 50f),
        text = "Revisar este parágrafo",
    )

    private val validFreehand = Freehand(
        id = "f1",
        page = 0,
        points = listOf(PdfPoint(0f, 0f), PdfPoint(10f, 10f), PdfPoint(20f, 20f)),
        colorArgb = 0xFF000000.toInt(),
        width = 3f,
    )

    @DisplayName("CA-11.1: pageCount < 0 lança IllegalArgumentException")
    @Test
    fun `negative pageCount throws`() {
        assertThatThrownBy { PdfAnnotationSession(pageCount = -1) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @DisplayName("CA-11.2: Adicionar anotação válida incrementa a lista e retorna sucesso")
    @Test
    fun `add valid annotation succeeds`() {
        val session = PdfAnnotationSession(pageCount = 2)

        val result = session.add(validHighlight)

        assertThat(result.isSuccess).isTrue()
        val updated = result.getOrThrow()
        assertThat(updated.annotations).containsExactly(validHighlight)
    }

    @DisplayName("CA-11.3: Adicionar anotação inválida falha e mantém a sessão inalterada")
    @Test
    fun `add invalid annotation fails`() {
        val session = PdfAnnotationSession(pageCount = 1)
        val invalidStickyNote = StickyNote(id = "n2", page = 5, anchor = PdfPoint(0f, 0f), text = "")

        val result = session.add(invalidStickyNote)

        assertThat(result.isFailure).isTrue()
        assertThat(session.annotations).isEmpty()
    }

    @DisplayName("CA-11.4: annotationsFor(page) retorna apenas as anotações da página")
    @Test
    fun `annotationsFor filters by page`() {
        val page1Note = StickyNote("n2", page = 1, anchor = PdfPoint(1f, 1f), text = "Página 2")
        val session = PdfAnnotationSession(pageCount = 3)
            .add(validHighlight).getOrThrow()
            .add(page1Note).getOrThrow()

        assertThat(session.annotationsFor(page = 0)).containsExactly(validHighlight)
        assertThat(session.annotationsFor(page = 1)).containsExactly(page1Note)
        assertThat(session.annotationsFor(page = 2)).isEmpty()
    }

    @DisplayName("CA-11.5: remove(id) remove a anotação correspondente")
    @Test
    fun `remove removes annotation by id`() {
        val session = PdfAnnotationSession(pageCount = 2)
            .add(validHighlight).getOrThrow()
            .add(validStickyNote).getOrThrow()

        val updated = session.remove("h1")

        assertThat(updated.annotations).containsExactly(validStickyNote)
    }

    @DisplayName("CA-11.6: undoLast(page) remove a última anotação daquela página")
    @Test
    fun `undoLast removes last annotation of page`() {
        val session = PdfAnnotationSession(pageCount = 2)
            .add(validHighlight).getOrThrow()
            .add(validStickyNote).getOrThrow()

        val undone = session.undoLast(page = 0)

        assertThat(undone.annotations).containsExactly(validHighlight)
    }

    @DisplayName("CA-11.7: clearPage(page) remove anotações apenas da página indicada")
    @Test
    fun `clearPage removes only annotations on that page`() {
        val page1Note = StickyNote("n2", page = 1, anchor = PdfPoint(1f, 1f), text = "Página 2")
        val session = PdfAnnotationSession(pageCount = 2)
            .add(validHighlight).getOrThrow()
            .add(validStickyNote).getOrThrow()
            .add(page1Note).getOrThrow()

        val cleared = session.clearPage(page = 0)

        assertThat(cleared.annotationsFor(0)).isEmpty()
        assertThat(cleared.annotationsFor(1)).containsExactly(page1Note)
    }

    @DisplayName("CA-11.8: findAnnotationAt identifica StickyNote, Highlight e Freehand dentro da tolerância")
    @Test
    fun `findAnnotationAt locates annotations by point`() {
        val session = PdfAnnotationSession(pageCount = 2)
            .add(validHighlight).getOrThrow()
            .add(validStickyNote).getOrThrow()
            .add(validFreehand).getOrThrow()

        // Próximo à nota adesiva (50, 50)
        val hitNote = session.findAnnotationAt(page = 0, point = PdfPoint(52f, 48f), tolerance = 10f)
        assertThat(hitNote).isEqualTo(validStickyNote)

        // Dentro do destaque (10..100, 20..50)
        val hitHighlight = session.findAnnotationAt(page = 0, point = PdfPoint(30f, 30f), tolerance = 5f)
        assertThat(hitHighlight).isEqualTo(validHighlight)

        // Próximo ao traço do freehand (ponto intermediário entre (0,0) e (10,10) -> (5, 5))
        val hitFreehand = session.findAnnotationAt(page = 0, point = PdfPoint(5f, 6f), tolerance = 3f)
        assertThat(hitFreehand).isEqualTo(validFreehand)

        // Ponto distante -> null
        val hitNone = session.findAnnotationAt(page = 0, point = PdfPoint(500f, 500f), tolerance = 10f)
        assertThat(hitNone).isNull()
    }

    @DisplayName("CA-11.9: Alteração de ferramenta, cor e espessura reflete no estado")
    @Test
    fun `tool and style configurations update session`() {
        val session = PdfAnnotationSession(pageCount = 1)
            .setTool(AnnotationTool.FREEHAND)
            .setColor(0xFFFF0000.toInt())
            .setStrokeWidth(8f)

        assertThat(session.activeTool).isEqualTo(AnnotationTool.FREEHAND)
        assertThat(session.selectedColorArgb).isEqualTo(0xFFFF0000.toInt())
        assertThat(session.strokeWidth).isEqualTo(8f)
    }
}
