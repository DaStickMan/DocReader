# DocReader

Leitor/editor Android de **PDF**, **DOCX** e **Markdown**, com tela inicial que lista os
arquivos recentes do aparelho. Desenvolvido com **SDD (spec-driven)** + **TDD**, por etapas.

> **Estado atual:** o núcleo de regras (JVM puro) e as camadas Android básicas
> (Room, MediaStore, UI Compose da tela "Abrir arquivo") estão implementadas e testadas.
> `./gradlew :app:assembleDebug` gera um APK. A renderização/edição dos documentos
> dentro do app ainda não existe (próximas etapas).

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
core/data/           # Android: RoomRecentStore, MediaStoreDocumentSource, loader de URI
app/                 # Android: Compose UI da tela "Abrir arquivo", AppContainer, MainActivity
```

## Build
```bash
./gradlew :app:assembleDebug   # gera app/build/outputs/apk/debug/app-debug.apk
```
Requer o Android SDK (veja `local.properties`).

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
| 9 | Camadas Android: Room, MediaStore, UI Compose | ✅ |
| 10+ | Renderização de PDF/DOCX/MD na UI, anotações | ⏳ |

Cada etapa tem uma spec em `specs/` e testes escritos antes da implementação.
