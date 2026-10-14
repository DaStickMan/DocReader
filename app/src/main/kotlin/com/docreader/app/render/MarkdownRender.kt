package com.docreader.app.render

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.docreader.core.markdown.MarkdownBlock
import com.docreader.core.markdown.MarkdownInline

/** Renderiza uma lista de blocos Markdown em uma coluna. */
@Composable
fun MarkdownBody(
    blocks: List<MarkdownBlock>,
    modifier: Modifier = Modifier,
    contentPadding: Int = 16,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(contentPadding.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        blocks.forEach { block -> MarkdownBlockView(block) }
    }
}

@Composable
private fun MarkdownBlockView(block: MarkdownBlock) {
    when (block) {
        is MarkdownBlock.Heading -> Text(
            text = block.text,
            style = when (block.level) {
                1 -> MaterialTheme.typography.headlineMedium
                2 -> MaterialTheme.typography.headlineSmall
                3 -> MaterialTheme.typography.titleLarge
                else -> MaterialTheme.typography.titleMedium
            },
            fontWeight = FontWeight.Bold,
        )

        is MarkdownBlock.Paragraph -> SelectionContainer {
            Text(text = inline(block.text), style = MaterialTheme.typography.bodyLarge)
        }

        is MarkdownBlock.BulletItem -> Text(
            text = "•  ${block.text}",
            style = MaterialTheme.typography.bodyLarge,
        )

        is MarkdownBlock.OrderedItem -> Text(
            text = "${block.number}.  ${block.text}",
            style = MaterialTheme.typography.bodyLarge,
        )

        is MarkdownBlock.Quote -> Text(
            text = inline(block.text),
            style = MaterialTheme.typography.bodyLarge,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        is MarkdownBlock.CodeBlock -> Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = block.code,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(12.dp),
            )
        }

        MarkdownBlock.Divider -> HorizontalDivider()
    }
}

private fun inline(text: String): AnnotatedString = buildAnnotatedString {
    MarkdownInline.parse(text).forEach { span ->
        withStyle(
            SpanStyle(
                fontWeight = if (span.bold) FontWeight.Bold else null,
                fontStyle = if (span.italic) FontStyle.Italic else null,
                fontFamily = if (span.code) FontFamily.Monospace else null,
            ),
        ) {
            append(span.text)
        }
    }
}
