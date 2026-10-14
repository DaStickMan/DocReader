package com.docreader.app

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.docreader.app.render.MarkdownBody
import com.docreader.core.domain.Document
import com.docreader.core.markdown.MarkdownParser
import com.docreader.core.markdown.SaveMode
import kotlinx.coroutines.launch

@Composable
fun MarkdownEditorScreen(document: Document, container: AppContainer) {
    var text by remember { mutableStateOf("") }
    var previewing by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(document.uri) {
        text = runCatching { container.documentContent.readText(document) }.getOrDefault("")
    }

    val copyLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/markdown"),
    ) { target ->
        if (target != null) {
            scope.launch {
                runCatching { container.documentContent.saveText(target.toString(), text) }
                    .onSuccess { status = "Cópia salva." }
                    .onFailure { status = "Falha ao salvar cópia: ${it.message}" }
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(onClick = { previewing = false }, enabled = previewing) { Text("Editar") }
            TextButton(onClick = { previewing = true }, enabled = !previewing) { Text("Pré-visualizar") }
            Spacer(Modifier.weight(1f))
            Button(onClick = {
                scope.launch {
                    runCatching { container.documentContent.saveText(document.uri, text) }
                        .onSuccess { status = "Salvo." }
                        .onFailure { status = "Falha ao salvar: ${it.message}" }
                }
            }) { Text("Salvar") }
            OutlinedButton(onClick = {
                val plan = container.planMarkdownSave(document, SaveMode.COPY)
                copyLauncher.launch(plan.suggestedName)
            }) { Text("Salvar cópia") }
        }

        status?.let {
            Text(
                text = it,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.labelMedium,
            )
        }

        if (previewing) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                MarkdownBody(MarkdownParser.parse(text))
            }
        } else {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxSize(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            )
        }
    }
}
