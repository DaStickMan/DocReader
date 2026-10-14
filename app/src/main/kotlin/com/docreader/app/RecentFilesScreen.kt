package com.docreader.app

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.docreader.core.domain.Document
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentFilesScreen(
    viewModel: RecentFilesViewModel,
    onOpenDocument: (Document) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lastOpened by viewModel.lastOpened.collectAsStateWithLifecycle()

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let(viewModel::onDocumentPicked) }

    LaunchedEffect(Unit) { viewModel.refresh() }

    LaunchedEffect(lastOpened) {
        lastOpened?.let {
            onOpenDocument(it)
            viewModel.consumeLastOpened()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Abrir arquivo") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { picker.launch(arrayOf("*/*")) },
            ) {
                Text("Abrir arquivo")
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            val documents = state.documents
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                state.errorMessage != null -> Text(
                    text = state.errorMessage.orEmpty(),
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error,
                )

                documents.isEmpty() -> Text(
                    text = "Nenhum documento recente encontrado.\nUse \"Abrir arquivo\" para escolher um.",
                    modifier = Modifier.align(Alignment.Center),
                )

                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(items = documents, key = { it.uri }) { document ->
                        DocumentRow(document = document, onClick = { onOpenDocument(document) })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentRow(document: Document, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = document.displayName, fontWeight = FontWeight.Medium)
        Text(
            text = "${document.type.name} · ${messageFor(document)}",
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

private fun messageFor(document: Document): String {
    val stamp = document.openedAt ?: document.lastModified
    return if (stamp == Instant.EPOCH) "data desconhecida" else dateFormat.format(stamp)
}
