package com.docreader.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.docreader.core.domain.Document
import com.docreader.core.domain.DocumentType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(document: Document, container: AppContainer, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(document.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Voltar") }
                },
            )
        },
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            when (document.type) {
                DocumentType.PDF -> PdfViewerScreen(
                    document = document,
                    factory = container.pdfPageRendererFactory,
                    annotationStore = container.annotationStore,
                )

                DocumentType.DOCX -> DocxViewerScreen(document, container.documentContent)

                DocumentType.MARKDOWN -> MarkdownEditorScreen(document, container)

                DocumentType.OTHER -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Formato não suportado.")
                }
            }
        }
    }
}
