package com.docreader.core.markdown

/** Heading ATX extraído de um documento Markdown. `line` é 1-based. */
data class MarkdownHeading(val level: Int, val text: String, val line: Int)

/** Extrai o sumário (headings ATX), ignorando conteúdo dentro de blocos cercados. */
object MarkdownOutline {

    private val atx = Regex("^ {0,3}(#{1,6})\\s+(.*)$")
    private val fence = Regex("^ {0,3}(`{3,}|~{3,})")
    private val trailingHashes = Regex("\\s+#+$")

    fun extract(markdown: String): List<MarkdownHeading> {
        val headings = mutableListOf<MarkdownHeading>()
        var insideFence = false

        markdown.lineSequence().forEachIndexed { index, rawLine ->
            val line = rawLine.trimEnd()
            if (fence.containsMatchIn(line)) {
                insideFence = !insideFence
                return@forEachIndexed
            }
            if (insideFence) return@forEachIndexed

            val match = atx.matchEntire(line) ?: return@forEachIndexed
            val level = match.groupValues[1].length
            val text = match.groupValues[2].trim().replace(trailingHashes, "").trim()
            headings += MarkdownHeading(level = level, text = text, line = index + 1)
        }
        return headings
    }
}
