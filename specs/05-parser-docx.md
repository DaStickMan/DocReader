# Spec 05 — Parser DOCX (OOXML)

**Etapa:** 5 · **Módulo:** `:core:ooxml` · **Formato:** leitura apenas

## Objetivo
Extrair uma lista de blocos a partir de um `.docx` (que é um ZIP com `word/document.xml`),
para renderização read-only no Compose. Sem dependência de Android (usa `java.util.zip` + StAX).

## Contratos
```kotlin
sealed interface DocxBlock {
    data class Heading(val level: Int, val text: String) : DocxBlock
    data class Paragraph(val runs: List<DocxRun>) : DocxBlock
    data class ListItem(val text: String, val ordered: Boolean) : DocxBlock
}
data class DocxRun(val text: String, val bold: Boolean = false, val italic: Boolean = false)

object DocxParser { fun parse(docx: InputStream): List<DocxBlock> }
```

## Regras
- Lê a entrada `word/document.xml` do ZIP.
- `<w:p>` com `<w:pStyle w:val="HeadingN">` (N=1..9) → `Heading(level=N)`; `Title` → nível 1.
- `<w:p>` com `<w:numPr>` → `ListItem(ordered=false)`.
- `<w:r>` agrega runs; `<w:b/>` → negrito, `<w:i/>` → itálico, `<w:t>` → texto.
- Parágrafo sem texto, sem estilo e sem lista é ignorado.
- Entrada inválida (não é ZIP, falta a entrada, XML quebrado) → lista vazia (leitor tolerante).

## Casos de aceite
- **CA-5.1** heading + parágrafo → `[Heading(1), Paragraph]`.
- **CA-5.2** run com `<w:b/>` → `bold=true`.
- **CA-5.3** run com `<w:i/>` → `italic=true`.
- **CA-5.4** `<w:numPr>` → `ListItem`.
- **CA-5.5** `<w:pStyle w:val="Heading2">` → `Heading(level=2)`.
- **CA-5.6** corpo vazio → `[]`.
- **CA-5.7** bytes aleatórios (não ZIP) → `[]`.
- **CA-5.8** ZIP sem `word/document.xml` → `[]`.
- **CA-5.9** parágrafos em branco são ignorados.
