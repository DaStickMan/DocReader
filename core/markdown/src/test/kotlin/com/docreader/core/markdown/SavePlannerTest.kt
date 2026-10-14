package com.docreader.core.markdown

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant

class SavePlannerTest {

    private val when1 = Instant.parse("2026-06-01T12:00:00Z")

    @DisplayName("CA-6.6: OVERWRITE mantém o nome")
    @Test
    fun `overwrite keeps name`() {
        assertThat(SavePlanner.targetName("nota.md", SaveMode.OVERWRITE, when1)).isEqualTo("nota.md")
    }

    @DisplayName("CA-6.7: COPY insere carimbo antes da extensão")
    @Test
    fun `copy inserts stamp before extension`() {
        assertThat(SavePlanner.targetName("nota.md", SaveMode.COPY, when1))
            .isEqualTo("nota (copy 20260601-120000).md")
    }

    @DisplayName("CA-6.8: COPY sem extensão")
    @Test
    fun `copy without extension`() {
        assertThat(SavePlanner.targetName("README", SaveMode.COPY, when1))
            .isEqualTo("README (copy 20260601-120000)")
    }

    @DisplayName("CA-6.9: COPY preserva .markdown")
    @Test
    fun `copy preserves markdown extension`() {
        assertThat(SavePlanner.targetName("guia.markdown", SaveMode.COPY, when1))
            .isEqualTo("guia (copy 20260601-120000).markdown")
    }
}
