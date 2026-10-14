package com.docreader.core.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant

class RecentDocumentsTest {

    private fun doc(
        uri: String,
        name: String = uri.substringAfterLast('/'),
        type: DocumentType = DocumentType.PDF,
        modified: Instant = Instant.parse("2026-01-01T00:00:00Z"),
        size: Long? = null,
        openedAt: Instant? = null,
    ) = Document(
        uri = uri,
        displayName = name,
        type = type,
        lastModified = modified,
        sizeBytes = size,
        openedAt = openedAt,
    )

    @DisplayName("CA-6: listas vazias -> vazio")
    @Test
    fun `empty inputs produce empty result`() {
        assertThat(RecentDocuments.merge(emptyList(), emptyList())).isEmpty()
    }

    @DisplayName("CA-7: tipo OTHER é descartado")
    @Test
    fun `unsupported type is dropped`() {
        val image = doc("content://x/foto.jpg", type = DocumentType.OTHER)
        val pdf = doc("content://x/doc.pdf")

        val result = RecentDocuments.merge(emptyList(), listOf(image, pdf))

        assertThat(result).containsExactly(pdf)
    }

    @DisplayName("CA-8: duplicado por uri mescla dados com prioridade para 'own'")
    @Test
    fun `duplicate uri is merged preferring own`() {
        val opened = Instant.parse("2026-03-01T10:00:00Z")
        val newer = Instant.parse("2026-04-01T00:00:00Z")
        val own = doc("content://x/doc.pdf", name = "doc.pdf", modified = Instant.parse("2026-01-01T00:00:00Z"), size = 100, openedAt = opened)
        val device = doc("content://x/doc.pdf", name = "outro-nome.pdf", modified = newer, size = 999)

        val result = RecentDocuments.merge(listOf(own), listOf(device))

        assertThat(result).hasSize(1)
        val merged = result.single()
        assertThat(merged.openedAt).isEqualTo(opened)
        assertThat(merged.lastModified).isEqualTo(newer)
        assertThat(merged.sizeBytes).isEqualTo(100)
        assertThat(merged.displayName).isEqualTo("doc.pdf")
    }

    @DisplayName("CA-8b: sizeBytes de 'own' nulo cai para o de 'device'")
    @Test
    fun `merge falls back to device size when own is null`() {
        val own = doc("content://x/doc.pdf", size = null)
        val device = doc("content://x/doc.pdf", size = 42)

        val result = RecentDocuments.merge(listOf(own), listOf(device))

        assertThat(result.single().sizeBytes).isEqualTo(42)
    }

    @DisplayName("CA-9: ordena por lastModified decrescente")
    @Test
    fun `sorts by lastModified descending`() {
        val a = doc("content://x/a.pdf", modified = Instant.parse("2026-01-01T00:00:00Z"))
        val b = doc("content://x/b.pdf", modified = Instant.parse("2026-03-01T00:00:00Z"))
        val c = doc("content://x/c.pdf", modified = Instant.parse("2026-02-01T00:00:00Z"))

        val result = RecentDocuments.merge(emptyList(), listOf(a, b, c))

        assertThat(result.map { it.uri }).containsExactly(
            "content://x/b.pdf",
            "content://x/c.pdf",
            "content://x/a.pdf",
        )
    }

    @DisplayName("CA-10: empate em lastModified desempata por nome")
    @Test
    fun `ties are broken by display name`() {
        val t = Instant.parse("2026-01-01T00:00:00Z")
        val z = doc("content://x/z.pdf", name = "zeta.pdf", modified = t)
        val a = doc("content://x/a.pdf", name = "alfa.pdf", modified = t)

        val result = RecentDocuments.merge(emptyList(), listOf(z, a))

        assertThat(result.map { it.displayName }).containsExactly("alfa.pdf", "zeta.pdf")
    }

    @DisplayName("CA-11: respeita o limite")
    @Test
    fun `applies limit`() {
        val docs = (1..5).map {
            doc("content://x/$it.pdf", modified = Instant.parse("2026-01-0${it}T00:00:00Z"))
        }

        val result = RecentDocuments.merge(emptyList(), docs, limit = 2)

        assertThat(result).hasSize(2)
    }

    @DisplayName("CA-12: limite <= 0 retorna vazio")
    @Test
    fun `non positive limit returns empty`() {
        val docs = listOf(doc("content://x/a.pdf"))

        assertThat(RecentDocuments.merge(emptyList(), docs, limit = 0)).isEmpty()
        assertThat(RecentDocuments.merge(emptyList(), docs, limit = -3)).isEmpty()
    }
}
