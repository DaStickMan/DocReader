# DocReader

Leitor/editor Android de **PDF**, **DOCX** e **Markdown**, com tela inicial que lista os
arquivos recentes do aparelho. Desenvolvido com **SDD (spec-driven)** + **TDD**, por etapas.

> **Estado atual:** todo o núcleo de regras (JVM puro) está implementado e testado.
> As camadas Android (UI Compose, Room, MediaStore, renderização de PDF) **dependem do
> Android SDK**, que não está instalado neste ambiente — por isso não existem ainda.
> O design mantém essas camadas atrás de *ports*, de modo que entram sem reescrever o núcleo.

## Como rodar os testes
```bash
./gradlew test
```

## Estrutura
```
specs/               # specs SDD (uma por etapa) com casos de aceite Given/When/Then
core/domain/         # Document, DocumentType, DocumentClassifier, RecentDocuments
core/ports/          # Clock, RecentStore, DocumentSource, DocumentQuery, DeviceFileMapper
core/application/    # GetRecentDocuments, OpenDocument
core/ooxml/          # DocxParser (leitura DOCX, StAX + zip)
core/markdown/       # MarkdownOutline, SavePlanner
core/pdf/            # PdfViewerState, Annotation + AnnotationValidator
```

## Arquitetura
Ports & adapters: o núcleo roda em JVM puro e testável; tudo que toca o sistema
(MediaStore/SAF, Room, renderização) fica atrás de interfaces em `core/ports`.

## Etapas
| Etapa | Assunto | Estado |
|---|---|---|
| 0 | Fundação (Gradle, harness de testes) | ✅ |
| 1 | Domínio: classificação + recentes | ✅ |
| 2 | Ports + casos de uso | ✅ |
| 3 | Especificação de consulta + mapeamento | ✅ |
| 4 | Estado do visualizador de PDF | ✅ |
| 5 | Parser DOCX | ✅ |
| 6 | Markdown: outline + salvamento | ✅ |
| 7 | Anotações de PDF: modelo + validação | ✅ |
| 8 | CI | ✅ (testes) |
| 9+ | UI Compose, Room, MediaStore, renderização | ⏳ requer Android SDK |

Cada etapa tem uma spec em `specs/` e testes escritos antes da implementação.
