package com.docreader.app.render

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.docreader.core.ooxml.DocxBlock

/** Renderiza uma lista de blocos DOCX em uma coluna. */
@Composable
fun DocxBody(blocks: List<DocxBlock>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        blocks.forEach { block ->
            when (block) {
                is DocxBlock.Heading -> Text(
                    text = block.text,
                    style = when (block.level) {
                        1 -> MaterialTheme.typography.headlineMedium
                        2 -> MaterialTheme.typography.headlineSmall
                        else -> MaterialTheme.typography.titleLarge
                    },
                    fontWeight = FontWeight.Bold,
                )

                is DocxBlock.Paragraph -> Text(
                    text = runsToAnnotated(block.runs),
                    style = MaterialTheme.typography.bodyLarge,
                )

                is DocxBlock.ListItem -> Text(
                    text = "•  ${block.text}",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

private fun runsToAnnotated(runs: List<com.docreader.core.ooxml.DocxRun>): AnnotatedString =
    buildAnnotatedString {
        runs.forEach { run ->
            withStyle(
                SpanStyle(
                    fontWeight = if (run.bold) FontWeight.Bold else null,
                    fontStyle = if (run.italic) FontStyle.Italic else null,
                ),
            ) {
                append(run.text)
            }
        }
    }
