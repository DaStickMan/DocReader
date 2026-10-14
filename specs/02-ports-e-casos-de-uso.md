# Spec 02 — Ports e casos de uso de abertura

**Etapa:** 2 · **Módulos:** `:core:ports`, `:core:application`

## Objetivo
Definir as fronteiras (ports) com o mundo externo e os casos de uso que orquestram a
tela "Abrir arquivo", sem tocar em Android.

## Contratos

```kotlin
fun interface Clock { fun now(): Instant }

interface RecentStore {          // persistência dos arquivos abertos pelo app
    suspend fun all(): List<Document>
    suspend fun upsert(document: Document)
    suspend fun remove(uri: String)
}

interface DocumentSource {       // varredura do aparelho (MediaStore, SAF, etc.)
    suspend fun recentDocuments(limit: Int): List<Document>
}

class GetRecentDocuments(source: DocumentSource, store: RecentStore) {
    suspend operator fun invoke(limit: Int = RecentDocuments.DEFAULT_LIMIT): List<Document>
}

class OpenDocument(store: RecentStore, clock: Clock) {
    suspend operator fun invoke(document: Document): Document
}
```

## Regras
- `GetRecentDocuments` = `RecentDocuments.merge(store.all(), source.recentDocuments(limit), limit)`.
- `OpenDocument` grava `openedAt = clock.now()` no store e devolve o documento atualizado.
- Os casos de uso não conhecem implementação concreta; recebem ports por construtor.

## Casos de aceite
- **CA-2.1** Dado store com `own` e source com `device`, Quando `GetRecentDocuments`, Então lista mesclada e ordenada.
- **CA-2.2** Dado store vazio e source com 3 docs, Quando `GetRecentDocuments(limit=2)`, Então 2 itens.
- **CA-2.3** Dado um `Document`, Quando `OpenDocument`, Então `openedAt` = agora e o store contém o registro.
- **CA-2.4** Dado o mesmo arquivo aberto duas vezes, Quando `OpenDocument`, Então o store mantém 1 registro com o `openedAt` mais novo.
