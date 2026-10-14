package com.docreader.core.pdf

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class AnnotationValidatorTest {

    private val rect = Rect(left = 1f, top = 2f, right = 10f, bottom = 20f)

    @DisplayName("CA-7.1: Rect invertido lança")
    @Test
    fun `invalid rect throws`() {
        assertThatThrownBy { Rect(left = 10f, top = 0f, right = 1f, bottom = 5f) }
            .isInstanceOf(IllegalArgumentException::class.java)
        assertThat(rect.width).isEqualTo(9f)
        assertThat(rect.height).isEqualTo(18f)
    }

    @DisplayName("CA-7.2: anotações válidas não geram issues")
    @Test
    fun `valid annotations have no issues`() {
        val annotations = listOf(
            Highlight("h1", 0, rect, colorArgb = 0xFFFF00),
            StickyNote("n1", 1, PdfPoint(3f, 4f), "lembrar"),
            Freehand("f1", 2, listOf(PdfPoint(0f, 0f), PdfPoint(5f, 5f)), colorArgb = 0, width = 2f),
        )

        assertThat(AnnotationValidator.validate(annotations, pageCount = 3)).isEmpty()
    }

    @DisplayName("CA-7.3: página fora do intervalo")
    @Test
    fun `page out of range is flagged`() {
        val annotations = listOf(Highlight("h1", 5, rect, colorArgb = 0))

        val issues = AnnotationValidator.validate(annotations, pageCount = 3)

        assertThat(issues).hasSize(1)
        assertThat(issues.single().id).isEqualTo("h1")
    }

    @DisplayName("CA-7.4: id em branco")
    @Test
    fun `blank id is flagged`() {
        val annotations = listOf(Highlight("", 0, rect, colorArgb = 0))

        assertThat(AnnotationValidator.validate(annotations, pageCount = 1))
            .anySatisfy { assertThat(it.reason).contains("id") }
    }

    @DisplayName("CA-7.5: nota com texto vazio")
    @Test
    fun `empty note text is flagged`() {
        val annotations = listOf(StickyNote("n1", 0, PdfPoint(0f, 0f), "   "))

        assertThat(AnnotationValidator.validate(annotations, pageCount = 1)).hasSize(1)
    }

    @DisplayName("CA-7.6: freehand inválido")
    @Test
    fun `invalid freehand is flagged`() {
        val tooFewPoints = Freehand("f1", 0, listOf(PdfPoint(0f, 0f)), colorArgb = 0, width = 1f)
        val zeroWidth = Freehand("f2", 0, listOf(PdfPoint(0f, 0f), PdfPoint(1f, 1f)), colorArgb = 0, width = 0f)

        assertThat(AnnotationValidator.validate(listOf(tooFewPoints), pageCount = 1)).hasSize(1)
        assertThat(AnnotationValidator.validate(listOf(zeroWidth), pageCount = 1)).hasSize(1)
    }
}
