package com.docreader.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.docreader.app.render.DocxBody
import com.docreader.core.application.DocumentContent
import com.docreader.core.domain.Document
import com.docreader.core.ooxml.DocxBlock
import com.docreader.core.ooxml.DocxParser
import java.io.ByteArrayInputStream

@Composable
fun DocxViewerScreen(document: Document, content: DocumentContent) {
    var blocks by remember { mutableStateOf<List<DocxBlock>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(document.uri) {
        runCatching {
            DocxParser.parse(ByteArrayInputStream(content.readBytes(document)))
        }.onSuccess { blocks = it }
            .onFailure { errorMessage = "Não foi possível ler o documento: ${it.message}" }
    }

    val loaded = blocks
    when {
        errorMessage != null -> CenteredText(errorMessage.orEmpty(), isError = true)

        loaded == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        loaded.isEmpty() -> CenteredText("Documento vazio ou sem conteúdo textual legível.")

        else -> Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            DocxBody(loaded)
        }
    }
}

@Composable
private fun CenteredText(message: String, isError: Boolean = false) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        )
    }
}
