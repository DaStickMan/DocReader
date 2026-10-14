package com.docreader.core.markdown

/** Converte texto inline de Markdown em [InlineSpan]s (negrito, itálico, código). */
object MarkdownInline {

    private val token = Regex("(`[^`]+`|\\*\\*[^*]+\\*\\*|__[^_]+__|\\*[^*]+\\*|_[^_]+_)")

    fun parse(text: String): List<InlineSpan> {
        val spans = mutableListOf<InlineSpan>()
        var lastIndex = 0

        for (match in token.findAll(text)) {
            if (match.range.first > lastIndex) {
                spans += InlineSpan(text.substring(lastIndex, match.range.first))
            }
            val raw = match.value
            spans += when {
                raw.startsWith("`") -> InlineSpan(raw.trim('`'), code = true)
                raw.startsWith("**") -> InlineSpan(raw.removeSurrounding("**"), bold = true)
                raw.startsWith("__") -> InlineSpan(raw.removeSurrounding("__"), bold = true)
                raw.startsWith("*") -> InlineSpan(raw.removeSurrounding("*"), italic = true)
                else -> InlineSpan(raw.removeSurrounding("_"), italic = true)
            }
            lastIndex = match.range.last + 1
        }
        if (lastIndex < text.length) {
            spans += InlineSpan(text.substring(lastIndex))
        }
        return spans.filter { it.text.isNotEmpty() }
    }
}
