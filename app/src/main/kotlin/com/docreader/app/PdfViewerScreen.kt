package com.docreader.app

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.docreader.core.data.PdfPageRenderer
import com.docreader.core.data.PdfPageRendererFactory
import com.docreader.core.domain.Document
import com.docreader.core.pdf.Annotation
import com.docreader.core.pdf.AnnotationTool
import com.docreader.core.pdf.Freehand
import com.docreader.core.pdf.Highlight
import com.docreader.core.pdf.PdfAnnotationSession
import com.docreader.core.pdf.PdfPoint
import com.docreader.core.pdf.PdfViewerState
import com.docreader.core.pdf.Rect
import com.docreader.core.pdf.StickyNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

private const val RENDER_WIDTH = 1200

private val PALETTE_COLORS = listOf(
    0xFFFFEB3B.toInt(), // Amarelo
    0xFF81C784.toInt(), // Verde
    0xFF64B5F6.toInt(), // Azul
    0xFFE57373.toInt(), // Vermelho
    0xFFBA68C8.toInt(), // Roxo
)

@Composable
fun PdfViewerScreen(
    document: Document,
    factory: PdfPageRendererFactory,
    annotationStore: com.docreader.core.ports.AnnotationStore? = null,
) {
    var renderer by remember { mutableStateOf<PdfPageRenderer?>(null) }
    var viewerState by remember { mutableStateOf<PdfViewerState?>(null) }
    var annotationSession by remember { mutableStateOf<PdfAnnotationSession?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var stickyNoteDraftPoint by remember { mutableStateOf<PdfPoint?>(null) }
    var selectedAnnotationDetails by remember { mutableStateOf<Annotation?>(null) }

    LaunchedEffect(document.uri) {
        val opened = factory.open(Uri.parse(document.uri))
        if (opened == null) {
            errorMessage = "Não foi possível abrir este PDF."
        } else {
            renderer = opened
            viewerState = PdfViewerState(pageCount = opened.pageCount)
            val saved = annotationStore?.getAnnotations(document.uri).orEmpty()
            annotationSession = PdfAnnotationSession(pageCount = opened.pageCount, annotations = saved)
        }
    }

    val currentAnnotations = annotationSession?.annotations
    LaunchedEffect(currentAnnotations) {
        val annotations = currentAnnotations ?: return@LaunchedEffect
        annotationStore?.saveAnnotations(document.uri, annotations)
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
    val session = annotationSession
    when {
        errorMessage != null -> CenteredMessage(errorMessage.orEmpty(), isError = true)

        state == null || session == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        state.pageCount == 0 -> CenteredMessage("PDF sem páginas.")

        else -> {
            PdfPager(
                state = state,
                session = session,
                bitmap = bitmap,
                onPrevious = { viewerState = state.previous() },
                onNext = { viewerState = state.next() },
                onZoomIn = { viewerState = state.zoomBy(1.25f) },
                onZoomOut = { viewerState = state.zoomBy(0.8f) },
                onSessionChange = { annotationSession = it },
                onRequestStickyNote = { stickyNoteDraftPoint = it },
                onAnnotationClicked = { selectedAnnotationDetails = it },
            )

            stickyNoteDraftPoint?.let { anchor ->
                NewStickyNoteDialog(
                    onDismiss = { stickyNoteDraftPoint = null },
                    onConfirm = { text ->
                        val note = StickyNote(
                            id = UUID.randomUUID().toString(),
                            page = state.currentPage,
                            anchor = anchor,
                            text = text,
                        )
                        session.add(note).onSuccess { updated ->
                            annotationSession = updated
                        }
                        stickyNoteDraftPoint = null
                    },
                )
            }

            selectedAnnotationDetails?.let { annotation ->
                AnnotationDetailsDialog(
                    annotation = annotation,
                    onDismiss = { selectedAnnotationDetails = null },
                    onDelete = {
                        annotationSession = session.remove(annotation.id)
                        selectedAnnotationDetails = null
                    },
                )
            }
        }
    }
}

@Composable
private fun PdfPager(
    state: PdfViewerState,
    session: PdfAnnotationSession,
    bitmap: Bitmap?,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onSessionChange: (PdfAnnotationSession) -> Unit,
    onRequestStickyNote: (PdfPoint) -> Unit,
    onAnnotationClicked: (Annotation) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        AnnotationToolbar(
            session = session,
            currentPage = state.currentPage,
            onSessionChange = onSessionChange,
        )

        HorizontalDivider()

        BoxWithConstraints(
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
                val bmpWidth = image.width.toFloat()
                val bmpHeight = image.height.toFloat()
                val containerWidth = maxWidth.value
                val containerHeight = maxHeight.value

                val widthScale = containerWidth / bmpWidth
                val heightScale = containerHeight / bmpHeight
                val fitScale = minOf(widthScale, heightScale)

                val fittedWidthDp = (bmpWidth * fitScale).dp
                val fittedHeightDp = (bmpHeight * fitScale).dp

                Box(
                    modifier = Modifier
                        .size(fittedWidthDp, fittedHeightDp)
                        .graphicsLayer(scaleX = state.zoom, scaleY = state.zoom),
                ) {
                    Image(
                        bitmap = image,
                        contentDescription = "Página ${state.currentPage + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                    )

                    PdfAnnotationCanvas(
                        session = session,
                        currentPage = state.currentPage,
                        pageWidth = bmpWidth,
                        pageHeight = bmpHeight,
                        onAddAnnotation = { annotation ->
                            session.add(annotation).onSuccess(onSessionChange)
                        },
                        onRequestStickyNote = onRequestStickyNote,
                        onAnnotationClicked = onAnnotationClicked,
                    )
                }
            }
        }

        HorizontalDivider()

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
private fun AnnotationToolbar(
    session: PdfAnnotationSession,
    currentPage: Int,
    onSessionChange: (PdfAnnotationSession) -> Unit,
) {
    val pageAnnotationCount = session.annotationsFor(currentPage).size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(
                selected = session.activeTool == AnnotationTool.VIEW,
                onClick = { onSessionChange(session.setTool(AnnotationTool.VIEW)) },
                label = { Text("Visualizar") },
            )
            FilterChip(
                selected = session.activeTool == AnnotationTool.FREEHAND,
                onClick = { onSessionChange(session.setTool(AnnotationTool.FREEHAND)) },
                label = { Text("Caneta") },
            )
            FilterChip(
                selected = session.activeTool == AnnotationTool.HIGHLIGHT,
                onClick = { onSessionChange(session.setTool(AnnotationTool.HIGHLIGHT)) },
                label = { Text("Destaque") },
            )
            FilterChip(
                selected = session.activeTool == AnnotationTool.STICKY_NOTE,
                onClick = { onSessionChange(session.setTool(AnnotationTool.STICKY_NOTE)) },
                label = { Text("Nota") },
            )

            Spacer(Modifier.weight(1f))

            if (pageAnnotationCount > 0) {
                Text(
                    text = "$pageAnnotationCount anotaç${if (pageAnnotationCount > 1) "ões" else "ão"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (session.activeTool != AnnotationTool.VIEW) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PALETTE_COLORS.forEach { colorArgb ->
                        val isSelected = session.selectedColorArgb == colorArgb
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(colorArgb), CircleShape)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Gray,
                                    shape = CircleShape,
                                )
                                .clickable { onSessionChange(session.setColor(colorArgb)) },
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = { onSessionChange(session.undoLast(page = currentPage)) },
                        enabled = pageAnnotationCount > 0,
                    ) {
                        Text("Desfazer")
                    }

                    TextButton(
                        onClick = { onSessionChange(session.clearPage(page = currentPage)) },
                        enabled = pageAnnotationCount > 0,
                    ) {
                        Text("Limpar")
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfAnnotationCanvas(
    session: PdfAnnotationSession,
    currentPage: Int,
    pageWidth: Float,
    pageHeight: Float,
    onAddAnnotation: (Annotation) -> Unit,
    onRequestStickyNote: (PdfPoint) -> Unit,
    onAnnotationClicked: (Annotation) -> Unit,
) {
    var draftFreehandPoints by remember { mutableStateOf<List<PdfPoint>>(emptyList()) }
    var draftHighlightStart by remember { mutableStateOf<PdfPoint?>(null) }
    var draftHighlightCurrent by remember { mutableStateOf<PdfPoint?>(null) }

    val pageAnnotations = session.annotationsFor(currentPage)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(session.activeTool, currentPage, pageWidth, pageHeight) {
                when (session.activeTool) {
                    AnnotationTool.VIEW -> {
                        detectTapGestures(
                            onTap = { offset ->
                                val pt = PdfPoint(
                                    x = offset.x * (pageWidth / size.width),
                                    y = offset.y * (pageHeight / size.height),
                                )
                                val hit = session.findAnnotationAt(currentPage, pt, tolerance = 30f)
                                if (hit != null) {
                                    onAnnotationClicked(hit)
                                }
                            },
                        )
                    }

                    AnnotationTool.STICKY_NOTE -> {
                        detectTapGestures(
                            onTap = { offset ->
                                val pt = PdfPoint(
                                    x = offset.x * (pageWidth / size.width),
                                    y = offset.y * (pageHeight / size.height),
                                )
                                onRequestStickyNote(pt)
                            },
                        )
                    }

                    AnnotationTool.FREEHAND -> {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val pt = PdfPoint(
                                    x = offset.x * (pageWidth / size.width),
                                    y = offset.y * (pageHeight / size.height),
                                )
                                draftFreehandPoints = listOf(pt)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val pt = PdfPoint(
                                    x = change.position.x * (pageWidth / size.width),
                                    y = change.position.y * (pageHeight / size.height),
                                )
                                draftFreehandPoints = draftFreehandPoints + pt
                            },
                            onDragEnd = {
                                if (draftFreehandPoints.size >= 2) {
                                    onAddAnnotation(
                                        Freehand(
                                            id = UUID.randomUUID().toString(),
                                            page = currentPage,
                                            points = draftFreehandPoints,
                                            colorArgb = session.selectedColorArgb,
                                            width = session.strokeWidth,
                                        ),
                                    )
                                }
                                draftFreehandPoints = emptyList()
                            },
                            onDragCancel = {
                                draftFreehandPoints = emptyList()
                            },
                        )
                    }

                    AnnotationTool.HIGHLIGHT -> {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val pt = PdfPoint(
                                    x = offset.x * (pageWidth / size.width),
                                    y = offset.y * (pageHeight / size.height),
                                )
                                draftHighlightStart = pt
                                draftHighlightCurrent = pt
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val pt = PdfPoint(
                                    x = change.position.x * (pageWidth / size.width),
                                    y = change.position.y * (pageHeight / size.height),
                                )
                                draftHighlightCurrent = pt
                            },
                            onDragEnd = {
                                val start = draftHighlightStart
                                val current = draftHighlightCurrent
                                if (start != null && current != null) {
                                    val left = minOf(start.x, current.x)
                                    val top = minOf(start.y, current.y)
                                    val right = maxOf(start.x, current.x)
                                    val bottom = maxOf(start.y, current.y)
                                    if (right - left > 5f && bottom - top > 5f) {
                                        onAddAnnotation(
                                            Highlight(
                                                id = UUID.randomUUID().toString(),
                                                page = currentPage,
                                                rect = Rect(left, top, right, bottom),
                                                colorArgb = session.selectedColorArgb,
                                            ),
                                        )
                                    }
                                }
                                draftHighlightStart = null
                                draftHighlightCurrent = null
                            },
                            onDragCancel = {
                                draftHighlightStart = null
                                draftHighlightCurrent = null
                            },
                        )
                    }
                }
            },
    ) {
        val scaleX = size.width / pageWidth
        val scaleY = size.height / pageHeight

        // Renderiza as anotações existentes
        for (annotation in pageAnnotations) {
            when (annotation) {
                is Highlight -> {
                    val rect = annotation.rect
                    val left = rect.left * scaleX
                    val top = rect.top * scaleY
                    val width = rect.width * scaleX
                    val height = rect.height * scaleY
                    val color = Color(annotation.colorArgb)

                    drawRect(
                        color = color.copy(alpha = 0.35f),
                        topLeft = Offset(left, top),
                        size = Size(width, height),
                    )
                    drawRect(
                        color = color.copy(alpha = 0.8f),
                        topLeft = Offset(left, top),
                        size = Size(width, height),
                        style = Stroke(width = 1.5.dp.toPx()),
                    )
                }

                is Freehand -> {
                    val points = annotation.points
                    if (points.size >= 2) {
                        val path = Path().apply {
                            moveTo(points[0].x * scaleX, points[0].y * scaleY)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x * scaleX, points[i].y * scaleY)
                            }
                        }
                        drawPath(
                            path = path,
                            color = Color(annotation.colorArgb),
                            style = Stroke(
                                width = annotation.width * scaleX,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                            ),
                        )
                    }
                }

                is StickyNote -> {
                    val anchorX = annotation.anchor.x * scaleX
                    val anchorY = annotation.anchor.y * scaleY
                    val radius = 12.dp.toPx()

                    drawCircle(
                        color = Color(0xFFFFC107),
                        radius = radius,
                        center = Offset(anchorX, anchorY),
                    )
                    drawCircle(
                        color = Color(0xFF5D4037),
                        radius = radius,
                        center = Offset(anchorX, anchorY),
                        style = Stroke(width = 1.5.dp.toPx()),
                    )
                    // Desenha linhas indicativas de texto na nota
                    val lineLength = radius * 0.8f
                    drawLine(
                        color = Color(0xFF5D4037),
                        start = Offset(anchorX - lineLength / 2, anchorY - radius * 0.3f),
                        end = Offset(anchorX + lineLength / 2, anchorY - radius * 0.3f),
                        strokeWidth = 1.5.dp.toPx(),
                    )
                    drawLine(
                        color = Color(0xFF5D4037),
                        start = Offset(anchorX - lineLength / 2, anchorY + radius * 0.2f),
                        end = Offset(anchorX + lineLength / 2, anchorY + radius * 0.2f),
                        strokeWidth = 1.5.dp.toPx(),
                    )
                }
            }
        }

        // Renderiza rascunho em tempo real de freehand
        if (draftFreehandPoints.size >= 2) {
            val draftPath = Path().apply {
                moveTo(draftFreehandPoints[0].x * scaleX, draftFreehandPoints[0].y * scaleY)
                for (i in 1 until draftFreehandPoints.size) {
                    lineTo(draftFreehandPoints[i].x * scaleX, draftFreehandPoints[i].y * scaleY)
                }
            }
            drawPath(
                path = draftPath,
                color = Color(session.selectedColorArgb),
                style = Stroke(
                    width = session.strokeWidth * scaleX,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        }

        // Renderiza rascunho em tempo real de destaque
        val dStart = draftHighlightStart
        val dCurrent = draftHighlightCurrent
        if (dStart != null && dCurrent != null) {
            val left = minOf(dStart.x, dCurrent.x) * scaleX
            val top = minOf(dStart.y, dCurrent.y) * scaleY
            val width = (maxOf(dStart.x, dCurrent.x) - minOf(dStart.x, dCurrent.x)) * scaleX
            val height = (maxOf(dStart.y, dCurrent.y) - minOf(dStart.y, dCurrent.y)) * scaleY
            val color = Color(session.selectedColorArgb)

            drawRect(
                color = color.copy(alpha = 0.25f),
                topLeft = Offset(left, top),
                size = Size(width, height),
            )
            drawRect(
                color = color.copy(alpha = 0.6f),
                topLeft = Offset(left, top),
                size = Size(width, height),
                style = Stroke(width = 1.5.dp.toPx()),
            )
        }
    }
}

@Composable
private fun NewStickyNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova nota adesiva") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Texto da nota") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text.trim()) },
                enabled = text.isNotBlank(),
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@Composable
private fun AnnotationDetailsDialog(
    annotation: Annotation,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    val title = when (annotation) {
        is StickyNote -> "Nota adesiva"
        is Highlight -> "Destaque"
        is Freehand -> "Desenho livre"
    }

    val contentText = when (annotation) {
        is StickyNote -> annotation.text
        is Highlight -> annotation.note ?: "Destaque selecionado"
        is Freehand -> "Traço com ${annotation.points.size} pontos"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(contentText) },
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text("Excluir", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        },
    )
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
