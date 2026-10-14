package com.docreader.core.markdown

/** Parser de Markdown para blocos renderizáveis. Não depende de Android. */
object MarkdownParser {

    private val fence = Regex("^ {0,3}(`{3,}|~{3,})\\s*(\\S*)\\s*$")
    private val atx = Regex("^ {0,3}(#{1,6})\\s+(.*)$")
    private val trailingHashes = Regex("\\s+#+$")
    private val bullet = Regex("^ {0,3}[-*+]\\s+(.*)$")
    private val ordered = Regex("^ {0,3}(\\d{1,9})[.)]\\s+(.*)$")
    private val quote = Regex("^ {0,3}>\\s?(.*)$")
    private val divider = Regex("^ {0,3}(-{3,}|\\*{3,}|_{3,})\\s*$")

    fun parse(markdown: String): List<MarkdownBlock> {
        val blocks = mutableListOf<MarkdownBlock>()
        val lines = markdown.lines()
        val paragraph = StringBuilder()

        fun flushParagraph() {
            if (paragraph.isNotEmpty()) {
                blocks += MarkdownBlock.Paragraph(paragraph.toString().trim())
                paragraph.clear()
            }
        }

        var index = 0
        while (index < lines.size) {
            val line = lines[index]
            val fenceMatch = fence.find(line.trimEnd())

            if (fenceMatch != null) {
                flushParagraph()
                val language = fenceMatch.groupValues[2].ifEmpty { null }
                val code = StringBuilder()
                index++
                while (index < lines.size && fence.find(lines[index].trimEnd()) == null) {
                    code.appendLine(lines[index])
                    index++
                }
                if (index < lines.size) index++ // consome o fechamento
                blocks += MarkdownBlock.CodeBlock(code.toString().trimEnd('\n'), language)
                continue
            }

            when {
                line.isBlank() -> {
                    flushParagraph()
                    index++
                }

                divider.matches(line.trimEnd()) -> {
                    flushParagraph()
                    blocks += MarkdownBlock.Divider
                    index++
                }

                atx.matches(line.trimEnd()) -> {
                    flushParagraph()
                    val match = atx.matchEntire(line.trimEnd())!!
                    val level = match.groupValues[1].length
                    val text = match.groupValues[2].trim().replace(trailingHashes, "").trim()
                    blocks += MarkdownBlock.Heading(level, text)
                    index++
                }

                bullet.matches(line) -> {
                    flushParagraph()
                    blocks += MarkdownBlock.BulletItem(bullet.matchEntire(line)!!.groupValues[1].trim())
                    index++
                }

                ordered.matches(line) -> {
                    flushParagraph()
                    val match = ordered.matchEntire(line)!!
                    blocks += MarkdownBlock.OrderedItem(
                        number = match.groupValues[1].toIntOrNull() ?: 1,
                        text = match.groupValues[2].trim(),
                    )
                    index++
                }

                quote.matches(line) -> {
                    flushParagraph()
                    val text = StringBuilder()
                    while (index < lines.size && quote.matches(lines[index])) {
                        if (text.isNotEmpty()) text.append('\n')
                        text.append(quote.matchEntire(lines[index])!!.groupValues[1].trim())
                        index++
                    }
                    blocks += MarkdownBlock.Quote(text.toString())
                }

                else -> {
                    paragraph.appendLine(line)
                    index++
                }
            }
        }
        flushParagraph()
        return blocks
    }
}
