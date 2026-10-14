package com.docreader.core.ooxml

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants
import javax.xml.stream.XMLStreamReader

/**
 * Parser tolerante de `.docx` (pacote OOXML). Leitor apenas, sem dependência de Android.
 * Qualquer entrada inválida resulta em lista vazia.
 */
object DocxParser {

    private const val DOCUMENT_ENTRY = "word/document.xml"
    private val headingStyle = Regex("heading([1-9])", RegexOption.IGNORE_CASE)

    fun parse(docx: InputStream): List<DocxBlock> = try {
        readDocumentEntry(docx)?.let(::readBlocks).orEmpty()
    } catch (_: Exception) {
        emptyList()
    }

    private fun readDocumentEntry(docx: InputStream): ByteArray? =
        ZipInputStream(docx).use { zip ->
            generateSequence { zip.nextEntry }
                .firstOrNull { it.name == DOCUMENT_ENTRY }
                ?.let { zip.readBytes() }
        }

    private fun readBlocks(xml: ByteArray): List<DocxBlock> {
        val factory = XMLInputFactory.newInstance().apply {
            setPropertySafely(XMLInputFactory.IS_NAMESPACE_AWARE, true)
            setPropertySafely(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false)
            setPropertySafely(XMLInputFactory.SUPPORT_DTD, false)
        }
        val reader = factory.createXMLStreamReader(ByteArrayInputStream(xml))
        val blocks = mutableListOf<DocxBlock>()

        var inParagraph = false
        var style: String? = null
        var isListItem = false
        var runs = mutableListOf<DocxRun>()
        var currentRun: DocxRun? = null

        while (reader.hasNext()) {
            when (reader.next()) {
                XMLStreamConstants.START_ELEMENT -> when (reader.localName) {
                    "p" -> {
                        inParagraph = true
                        style = null
                        isListItem = false
                        runs = mutableListOf()
                        currentRun = null
                    }
                    "pStyle" -> style = attribute(reader, "val")
                    "numPr" -> isListItem = true
                    "r" -> currentRun = DocxRun(text = "")
                    "b" -> currentRun = currentRun?.copy(bold = true)
                    "i" -> currentRun = currentRun?.copy(italic = true)
                    "t" -> currentRun = currentRun?.let { it.copy(text = it.text + reader.elementText) }
                }

                XMLStreamConstants.END_ELEMENT -> when (reader.localName) {
                    "r" -> {
                        currentRun?.let { if (it.text.isNotEmpty()) runs += it }
                        currentRun = null
                    }
                    "p" -> {
                        if (inParagraph) buildBlock(style, runs, isListItem)?.let(blocks::add)
                        inParagraph = false
                    }
                }
            }
        }
        reader.close()
        return blocks
    }

    private fun buildBlock(style: String?, runs: List<DocxRun>, isListItem: Boolean): DocxBlock? {
        val text = runs.joinToString(separator = "") { it.text }
        val level = headingLevel(style)
        return when {
            level != null -> DocxBlock.Heading(level, text)
            isListItem -> DocxBlock.ListItem(text)
            text.isBlank() -> null
            else -> DocxBlock.Paragraph(runs)
        }
    }

    private fun headingLevel(style: String?): Int? = when {
        style == null -> null
        style.equals("Title", ignoreCase = true) -> 1
        else -> headingStyle.matchEntire(style.trim())?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun attribute(reader: XMLStreamReader, name: String): String? {
        for (index in 0 until reader.attributeCount) {
            if (reader.getAttributeLocalName(index) == name) return reader.getAttributeValue(index)
        }
        return null
    }

    private fun XMLInputFactory.setPropertySafely(name: String, value: Any) {
        try {
            setProperty(name, value)
        } catch (_: IllegalArgumentException) {
            // Implementação StAX não suporta a propriedade; segue com o padrão.
        }
    }
}
