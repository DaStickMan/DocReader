# Spec 11 — Sessão e UI de Anotações de PDF

**Etapa:** 11 · **Módulos:** `:core:pdf`, `:app`

## Objetivo
Gerenciar o estado interativo de anotações (destaque, nota adesiva, desenho livre)
e integrá-lo ao visualizador de PDF no Compose, com suporte a criação, seleção,
remoção, desfazer e validação contra as páginas do documento.

## Contratos
```kotlin
enum class AnnotationTool {
    VIEW,
    HIGHLIGHT,
    STICKY_NOTE,
    FREEHAND,
}

data class PdfAnnotationSession(
    val pageCount: Int,
    val annotations: List<Annotation> = emptyList(),
    val activeTool: AnnotationTool = AnnotationTool.VIEW,
    val selectedColorArgb: Int = DEFAULT_COLOR,
    val strokeWidth: Float = DEFAULT_STROKE_WIDTH,
) {
    fun annotationsFor(page: Int): List<Annotation>
    fun add(annotation: Annotation): Result<PdfAnnotationSession>
    fun remove(id: String): PdfAnnotationSession
    fun undoLast(page: Int? = null): PdfAnnotationSession
    fun clearPage(page: Int): PdfAnnotationSession
    fun clearAll(): PdfAnnotationSession
    fun setTool(tool: AnnotationTool): PdfAnnotationSession
    fun setColor(colorArgb: Int): PdfAnnotationSession
    fun setStrokeWidth(width: Float): PdfAnnotationSession
    fun findAnnotationAt(page: Int, point: PdfPoint, tolerance: Float = 20f): Annotation?

    companion object {
        const val DEFAULT_COLOR: Int = 0xFFFFEB3B.toInt() // Amarelo
        const val DEFAULT_STROKE_WIDTH: Float = 4f
    }
}
```

## Regras
- `pageCount >= 0`. Se `pageCount < 0`, lança `IllegalArgumentException`.
- `add` valida a anotação com `AnnotationValidator.validate(listOf(annotation), pageCount)`.
  Se houver issues, retorna `Result.failure(IllegalArgumentException(...))` sem alterar as anotações.
  Caso seja válida, retorna `Result.success(copy(annotations = annotations + annotation))`.
- `remove(id)` remove a anotação com o identificador correspondente.
- `undoLast(page)` remove a última anotação inserida na página informada (ou no documento inteiro se `page == null`).
- `clearPage(page)` remove todas as anotações daquela página.
- `findAnnotationAt(page, point, tolerance)` busca na ordem inversa de inserção:
  - StickyNote: distância euclidiana entre `point` e `anchor` <= `tolerance`.
  - Highlight: ponto dentro do retângulo expandido por `tolerance`.
  - Freehand: menor distância do ponto a qualquer segmento do traço <= `tolerance`.

## Casos de aceite
- **CA-11.1** `pageCount < 0` lança `IllegalArgumentException`.
- **CA-11.2** Adicionar anotação válida incrementa a lista e retorna sucesso.
- **CA-11.3** Adicionar anotação inválida falha e mantém a sessão inalterada.
- **CA-11.4** `annotationsFor(page)` retorna apenas as anotações daquela página.
- **CA-11.5** `remove(id)` remove a anotação correspondente.
- **CA-11.6** `undoLast(page)` remove a última anotação daquela página.
- **CA-11.7** `clearPage(page)` remove todas as anotações da página e preserva as demais.
- **CA-11.8** `findAnnotationAt` identifica StickyNote, Highlight e Freehand dentro da tolerância.
- **CA-11.9** Alteração de ferramenta (`setTool`), cor (`setColor`) e largura (`setStrokeWidth`) reflete no estado.
