# Spec 01 — Domínio: classificação e arquivos recentes

**Etapa:** 1
**Status:** aprovada
**Módulo:** `:core:domain`

## Objetivo
Regras puras de (a) classificar um arquivo em PDF/DOCX/Markdown e (b) combinar a lista
de recentes do aparelho com os arquivos já abertos pelo app, produzindo a lista da tela
"Abrir arquivo".

## Contratos

```kotlin
enum class DocumentType { PDF, DOCX, MARKDOWN, OTHER }
// DocumentType.isSupported == (this != OTHER)

data class Document(
    val uri: String,
    val displayName: String,
    val type: DocumentType,
    val lastModified: Instant,
    val sizeBytes: Long? = null,
    val openedAt: Instant? = null,   // preenchido quando aberto pelo app
)

object DocumentClassifier {
    fun classify(displayName: String, mimeType: String? = null): DocumentType
}

object RecentDocuments {
    const val DEFAULT_LIMIT = 50
    fun merge(own: List<Document>, device: List<Document>, limit: Int = DEFAULT_LIMIT): List<Document>
}
```

## Regras de negócio

### classify
1. Extensão (após o último `.`, case-insensitive): `pdf`->PDF, `docx`->DOCX, `md`|`markdown`->MARKDOWN.
2. Se a extensão não decidir, usa o MIME (ignora parâmetros após `;`):
   `application/pdf`->PDF,
   `application/vnd.openxmlformats-officedocument.wordprocessingml.document`->DOCX,
   `text/markdown`|`text/x-markdown`->MARKDOWN.
3. Nada decidiu -> `OTHER`.

### merge(own, device, limit)
1. Somente tipos suportados entram; `OTHER` é descartado.
2. Deduplica por `uri`.
3. Quando a mesma `uri` aparece em `own` e `device`, o registo resultante:
   - `lastModified` = o **maior** dos dois;
   - `openedAt` = o de `own`;
   - `sizeBytes` = o de `own` se existir, senão o de `device`;
   - `displayName`/`type` = os de `own` (registos persistidos pelo app são autoritativos).
4. Ordenação: `lastModified` **decrescente**; empate -> `displayName` crescente (case-insensitive).
5. `limit <= 0` -> lista vazia; caso contrário, no máximo `limit` itens.

## Casos de aceite (Given/When/Then)

- **CA-1** Dado nome `"report.pdf"`, Quando `classify`, Então `PDF`.
- **CA-2** Dado nome `"NOTE.MD"`, Quando `classify`, Então `MARKDOWN`.
- **CA-3** Dado nome `"a.txt"` e mime `application/pdf`, Quando `classify`, Então `PDF`.
- **CA-4** Dado nome `"sem_extensao"` e mime `text/markdown`, Quando `classify`, Então `MARKDOWN`.
- **CA-5** Dado nome `"foto.jpg"`, Quando `classify`, Então `OTHER`.
- **CA-6** Dados `own=[]`, `device=[]`, Quando `merge`, Então `[]`.
- **CA-7** Dado um `device` com um `Document` de tipo `OTHER`, Quando `merge`, Então ele é descartado.
- **CA-8** Dada a mesma `uri` em `own` e `device`, Quando `merge`, Então há 1 item com
  `openedAt` de `own` e `lastModified` = maior dos dois.
- **CA-9** Dados 3 documentos com `lastModified` distintos, Quando `merge`, Então vêm em ordem decrescente.
- **CA-10** Dados 2 documentos com mesmo `lastModified`, Quando `merge`, Então desempata por nome.
- **CA-11** Dados 5 documentos e `limit=2`, Quando `merge`, Então retorna 2.
- **CA-12** Dado `limit=0`, Quando `merge`, Então `[]`.

## Fora de escopo (Etapa 1)
- Ler MediaStore, permissões, persistência (Etapas 2 e 3).
