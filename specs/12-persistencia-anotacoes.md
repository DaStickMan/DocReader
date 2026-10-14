# Spec 12 — Persistência de anotações em Room

**Etapa:** 12 · **Módulos:** `:core:ports`, `:core:data`, `:app`

## Objetivo
Persistir as anotações feitas pelo usuário por documento (`uri`), permitindo que
ao fechar e reabrir um PDF as anotações (destaques, notas adesivas e desenho livre)
sejam restauradas.

## Contratos
```kotlin
interface AnnotationStore {
    suspend fun getAnnotations(uri: String): List<Annotation>
    suspend fun saveAnnotations(uri: String, annotations: List<Annotation>)
    suspend fun clearAnnotations(uri: String)
}
```

## Regras
- `saveAnnotations(uri, annotations)` substitui todas as anotações salvas para a `uri` informada.
- `getAnnotations(uri)` retorna as anotações do documento na ordem em que foram salvas.
- Suporta todas as variantes de `Annotation`: `Highlight`, `StickyNote` e `Freehand`.
- `clearAnnotations(uri)` remove as anotações daquele documento sem afetar outros.

## Casos de aceite
- **CA-12.1** Salvar anotações para um documento e recuperá-las retorna exatamente a mesma lista.
- **CA-12.2** Sobrescrever as anotações com nova lista atualiza os registros daquele documento.
- **CA-12.3** `clearAnnotations` remove os registros do documento indicado e mantém os demais.
- **CA-12.4** Suporta polilinhas com múltiplos pontos (`Freehand`), retângulos (`Highlight`) e âncoras de notas (`StickyNote`).
