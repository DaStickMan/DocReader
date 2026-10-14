package com.docreader.core.ooxml

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Gera bytes de `.docx` mínimos em memória para os testes. */
object DocxFixtures {

    private const val W_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"

    fun docx(bodyXml: String): ByteArray {
        val document = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <w:document xmlns:w="$W_NS"><w:body>$bodyXml</w:body></w:document>
        """.trimIndent()
        return zipOf("word/document.xml" to document.toByteArray(Charsets.UTF_8))
    }

    fun zipOf(vararg entries: Pair<String, ByteArray>): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            entries.forEach { (name, bytes) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        return out.toByteArray()
    }

    fun paragraph(vararg runs: String): String = "<w:p>${runs.joinToString("")}</w:p>"

    fun run(text: String, bold: Boolean = false, italic: Boolean = false): String = buildString {
        append("<w:r><w:rPr>")
        if (bold) append("<w:b/>")
        if (italic) append("<w:i/>")
        append("</w:rPr>")
        append("<w:t xml:space=\"preserve\">").append(text).append("</w:t></w:r>")
    }

    fun heading(level: Int, text: String): String =
        "<w:p><w:pPr><w:pStyle w:val=\"Heading$level\"/></w:pPr>${run(text)}</w:p>"

    fun listItem(text: String): String =
        "<w:p><w:pPr><w:numPr><w:ilvl w:val=\"0\"/></w:numPr></w:pPr>${run(text)}</w:p>"
}
