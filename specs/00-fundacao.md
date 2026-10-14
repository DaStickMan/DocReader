# Spec 00 — Fundação

**Etapa:** 0
**Status:** aprovada
**Objetivo:** definir a estrutura do projeto, a arquitetura testável (ports & adapters)
e o harness de testes que todas as etapas seguintes usam.

## Contexto
App Android (Kotlin + Jetpack Compose) leitor/editor de PDF, DOCX e Markdown.
Distribuição por APK assinado (GitHub Releases). Processo **SDD + TDD**: spec antes do
código, teste antes da implementação.

## Arquitetura (ports & adapters)
O núcleo de regras roda em **JVM puro** (sem Android) para permitir TDD rápido.
Tudo que toca o sistema (MediaStore, SAF, arquivos) fica atrás de **interfaces (ports)**.

```
core/domain/   regras puras (Kotlin JVM, zero Android)   <- TDD principal
core/ports/    interfaces (DocumentSource, RecentStore...) [etapas futuras]
core/data/     adapters: Room, MediaStore, SAF             [etapas futuras]
feature/*/     ViewModels + Compose UI                     [etapas futuras]
app/           Android application                         [etapas futuras]
```

Nesta etapa existe apenas `:core:domain` (Kotlin JVM). Módulos Android entram quando o
Android SDK estiver disponível no ambiente.

## Decisões
- Kotlin 2.1.0, Gradle 8.11.1, JVM toolchain 17.
- Testes: JUnit5 + AssertJ + MockK (`useJUnitPlatform()`).
- Modelo de datas com `java.time.Instant` (minSdk 26 permite).

## Definition of Done (todas as etapas)
1. Spec aprovada e versionada.
2. Testes escritos antes da implementação (red -> green -> refactor).
3. Suíte verde via `./gradlew test`.
4. Nenhuma regra de negócio fora de teste.

## Fora de escopo (Etapa 0)
- UI, permissões, leitura de arquivos reais.
