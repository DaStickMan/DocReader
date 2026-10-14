# Spec 04 — Máquina de estado do visualizador de PDF

**Etapa:** 4 · **Módulo:** `:core:pdf`

## Objetivo
Modelar o estado puro de navegação/zoom do leitor de PDF, independente da renderização
(que fica atrás de um port no Android).

## Contrato
```kotlin
data class PdfViewerState(pageCount: Int, currentPage: Int = 0, zoom: Float = 1f) {
    val isFirstPage: Boolean
    val isLastPage: Boolean
    fun goTo(page: Int): PdfViewerState
    fun next(): PdfViewerState
    fun previous(): PdfViewerState
    fun zoomBy(factor: Float): PdfViewerState
    companion object { const val MIN_ZOOM = 1f; const val MAX_ZOOM = 5f }
}
```

## Regras
- `pageCount >= 0`; `currentPage` em `0 until max(1, pageCount)`; `zoom` em `[MIN_ZOOM, MAX_ZOOM]`.
- Estado inválido lança `IllegalArgumentException` no construtor.
- `goTo` faz clamp para `[0, pageCount-1]`.
- `next`/`previous` não passam dos limites (idempotentes no fim).
- `zoomBy` multiplica e faz clamp em `[MIN_ZOOM, MAX_ZOOM]`.

## Casos de aceite
- **CA-4.1** `pageCount < 0` → `IllegalArgumentException`.
- **CA-4.2** `currentPage` fora do intervalo → `IllegalArgumentException`.
- **CA-4.3** `zoom` fora do intervalo → `IllegalArgumentException`.
- **CA-4.4** `goTo(99)` com 3 páginas → página 2.
- **CA-4.5** `next()` na última página → sem mudança.
- **CA-4.6** `previous()` na primeira página → sem mudança.
- **CA-4.7** `zoomBy(10)` no máximo → mantém `MAX_ZOOM`.
- **CA-4.8** documento vazio (`pageCount=0`) é válido e `isLastPage` é verdadeiro.
