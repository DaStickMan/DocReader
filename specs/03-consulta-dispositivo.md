# Spec 03 — Especificação de consulta e mapeamento de arquivos do aparelho

**Etapa:** 3 · **Módulo:** `:core:ports`

## Objetivo
Modelar (de forma pura e portável) *o que* pedir ao provedor de arquivos do aparelho e
*como* converter a linha retornada em [Document]. A implementação Android (MediaStore/SAF),
quando existir, apenas executa esta especificação.

## Contratos
```kotlin
object SupportedMimeTypes { val ALL: List<String> }   // PDF, DOCX, MARKDOWN

enum class SortOrder { DATE_MODIFIED_DESC, DATE_MODIFIED_ASC, DISPLAY_NAME_ASC }

data class DocumentQuery(
    val mimeTypes: List<String> = SupportedMimeTypes.ALL,
    val sort: SortOrder = SortOrder.DATE_MODIFIED_DESC,
    val limit: Int = 50,
)

object DeviceFileMapper {
    fun toDocument(uri, displayName, mimeType, sizeBytes, lastModified): Document?
}
```

## Regras
- Ordenação padrão é **mais recente primeiro**.
- `DeviceFileMapper.toDocument` devolve `null` para tipos não suportados.
- Classificação delegada a `DocumentClassifier` (núcleo).

## Casos de aceite
- **CA-3.1** Default de `DocumentQuery` = três MIME suportados, `DATE_MODIFIED_DESC`, limit 50.
- **CA-3.2** Dado um `.pdf`, Quando `toDocument`, Então `Document` com tipo `PDF` e metadados preservados.
- **CA-3.3** Dado `foto.jpg` sem MIME, Quando `toDocument`, Então `null`.
- **CA-3.4** Dado nome sem extensão e MIME `text/markdown`, Então tipo `MARKDOWN`.
