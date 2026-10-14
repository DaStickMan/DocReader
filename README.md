# DocReader

Leitor/editor Android de **PDF**, **DOCX** e **Markdown**, com tela inicial que lista os
arquivos recentes do aparelho. Desenvolvido com **SDD (spec-driven)** + **TDD**, por etapas.

> **Estado atual:** o app abre, lista os recentes do aparelho e **visualiza os três formatos**:
> PDF (paginação e zoom), DOCX (leitura) e Markdown (edição com pré-visualização e salvamento).
> Gera APK de debug e de **release assinado**. As anotações de PDF têm modelo/validação
> testados, mas ainda não a UI.

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
./gradlew :app:assembleDebug     # app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:assembleRelease   # APK assinado (usa keystore.properties)
```
Requer o Android SDK (veja `local.properties`).

Para assinar o release, copie `keystore.properties.example` para `keystore.properties`
e gere o keystore com `keytool` (ambos são ignorados pelo Git). Sem o arquivo, o build de
release ainda funciona usando a chave de debug.

## Release automático
Ao empurrar uma tag `v*`, o workflow `.github/workflows/release.yml` roda os testes,
gera o APK de release e o anexa a uma GitHub Release. Configure os secrets
`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` e `KEY_PASSWORD`.

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
| 10 | Visualizadores: PDF (paginação/zoom), DOCX, editor Markdown | ✅ |
| 11 | UI de anotações de PDF (modelo pronto) | ⏳ |

Cada etapa tem uma spec em `specs/` e testes escritos antes da implementação.
