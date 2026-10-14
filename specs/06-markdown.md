# Spec 06 — Markdown: outline e salvamento

**Etapa:** 6 · **Módulo:** `:core:markdown`

## Objetivo
Lógica pura do editor Markdown que não depende da UI: extrair o sumário (headings ATX)
e decidir o nome de destino ao salvar.

## Contratos
```kotlin
data class MarkdownHeading(val level: Int, val text: String, val line: Int)
object MarkdownOutline { fun extract(markdown: String): List<MarkdownHeading> }

enum class SaveMode { OVERWRITE, COPY }
object SavePlanner { fun targetName(displayName: String, mode: SaveMode, timestamp: Instant): String }
```

## Regras
### extract
- Heading ATX: `^ {0,3}(#{1,6})\s+(.*)$`; `level` = nº de `#`; `line` 1-based.
- Ignora linhas dentro de blocos cercados (```` ``` ```` ou `~~~`).
- Remove a sequência de fechamento de `#` apenas se precedida de espaço (`"Foo ##"` → `Foo`);
  `"C#"` permanece `C#`.

### SavePlanner
- `OVERWRITE` → mesmo nome.
- `COPY` → `"<base> (copy <stamp>)<ext>"`, com `stamp = yyyyMMdd-HHmmss`.
- Sem extensão não adiciona sufixo de extensão.

## Casos de aceite
- **CA-6.1** `"# A"` → `[Heading(1,"A",1)]`.
- **CA-6.2** headings com níveis variados preservam ordem e níveis.
- **CA-6.3** `"## Foo ##"` → texto `"Foo"`.
- **CA-6.4** `"## C#"` → texto `"C#"`.
- **CA-6.5** `#` dentro de bloco cercado é ignorado.
- **CA-6.6** `OVERWRITE` devolve o mesmo nome.
- **CA-6.7** `COPY` de `nota.md` → `nota (copy <stamp>).md`.
- **CA-6.8** `COPY` de `README` (sem ext) → `README (copy <stamp>)`.
- **CA-6.9** `COPY` preserva `.markdown`.
