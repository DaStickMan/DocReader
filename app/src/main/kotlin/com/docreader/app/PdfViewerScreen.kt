package com.docreader.app

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.docreader.core.data.PdfPageRenderer
import com.docreader.core.data.PdfPageRendererFactory
import com.docreader.core.domain.Document
import com.docreader.core.pdf.PdfViewerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val RENDER_WIDTH = 1200

@Composable
fun PdfViewerScreen(document: Document, factory: PdfPageRendererFactory) {
    var renderer by remember { mutableStateOf<PdfPageRenderer?>(null) }
    var viewerState by remember { mutableStateOf<PdfViewerState?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(document.uri) {
        val opened = factory.open(Uri.parse(document.uri))
        if (opened == null) {
            errorMessage = "Não foi possível abrir este PDF."
        } else {
            renderer = opened
            viewerState = PdfViewerState(pageCount = opened.pageCount)
        }
    }

    DisposableEffect(Unit) {
        onDispose { renderer?.close() }
    }

    val currentPage = viewerState?.currentPage
    LaunchedEffect(currentPage, renderer) {
        val active = renderer ?: return@LaunchedEffect
        val page = currentPage ?: return@LaunchedEffect
        if (active.pageCount == 0) return@LaunchedEffect
        bitmap = withContext(Dispatchers.IO) { active.renderPage(page, RENDER_WIDTH) }
    }

    val state = viewerState
    when {
        errorMessage != null -> CenteredMessage(errorMessage.orEmpty(), isError = true)

        state == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        state.pageCount == 0 -> CenteredMessage("PDF sem páginas.")

        else -> PdfPager(
            state = state,
            bitmap = bitmap,
            onPrevious = { viewerState = state.previous() },
            onNext = { viewerState = state.next() },
            onZoomIn = { viewerState = state.zoomBy(1.25f) },
            onZoomOut = { viewerState = state.zoomBy(0.8f) },
        )
    }
}

@Composable
private fun PdfPager(
    state: PdfViewerState,
    bitmap: Bitmap?,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF202020)),
            contentAlignment = Alignment.Center,
        ) {
            val image = bitmap?.asImageBitmap()
            if (image == null) {
                CircularProgressIndicator()
            } else {
                Image(
                    bitmap = image,
                    contentDescription = "Página ${state.currentPage + 1}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(scaleX = state.zoom, scaleY = state.zoom),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onPrevious, enabled = !state.isFirstPage) { Text("Anterior") }

            Text("${state.currentPage + 1} / ${state.pageCount}")

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onZoomOut) { Text("−") }
                TextButton(onClick = onZoomIn) { Text("+") }
                Button(onClick = onNext, enabled = !state.isLastPage) { Text("Próxima") }
            }
        }
    }
}

@Composable
private fun CenteredMessage(message: String, isError: Boolean = false) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        )
    }
}
