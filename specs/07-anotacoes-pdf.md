# Spec 07 — Anotações de PDF: modelo e validação

**Etapa:** 7 · **Módulo:** `:core:pdf`

## Objetivo
Modelar as anotações (destaque, nota adesiva, desenho livre) e validá-las antes de
gravá-las no PDF. A escrita no arquivo (PDFBox) fica atrás de um port, em etapa Android.

## Contratos
```kotlin
data class PdfPoint(val x: Float, val y: Float)
data class Rect(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width: Float; val height: Float
}

sealed interface Annotation { val id: String; val page: Int }
data class Highlight(id, page, rect, colorArgb, note: String? = null) : Annotation
data class StickyNote(id, page, anchor: PdfPoint, text: String) : Annotation
data class Freehand(id, page, points: List<PdfPoint>, colorArgb, width: Float) : Annotation

data class AnnotationIssue(val id: String, val reason: String)
object AnnotationValidator { fun validate(annotations: List<Annotation>, pageCount: Int): List<AnnotationIssue> }
```

## Regras
- `Rect` exige `left <= right` e `top <= bottom` (senão `IllegalArgumentException`).
- Validação gera issues (não lança) para: `id` em branco, `page` fora de `[0, pageCount)`,
  `StickyNote.text` em branco, `Freehand.points < 2`, `Freehand.width <= 0`.

## Casos de aceite
- **CA-7.1** `Rect` invertido lança `IllegalArgumentException`.
- **CA-7.2** anotações válidas → sem issues.
- **CA-7.3** página fora do intervalo → issue.
- **CA-7.4** `id` em branco → issue.
- **CA-7.5** `StickyNote` com texto em branco → issue.
- **CA-7.6** `Freehand` com 1 ponto → issue; `width <= 0` → issue.
