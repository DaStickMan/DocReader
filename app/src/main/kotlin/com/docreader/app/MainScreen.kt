package com.docreader.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.docreader.core.domain.Document

@Composable
fun MainScreen(container: AppContainer) {
    val viewModel: RecentFilesViewModel = viewModel(
        factory = RecentFilesViewModel.factory(container),
    )
    var opened by remember { mutableStateOf<Document?>(null) }

    val current = opened
    if (current == null) {
        RecentFilesScreen(
            viewModel = viewModel,
            onOpenDocument = { opened = it },
        )
    } else {
        ViewerScreen(
            document = current,
            container = container,
            onBack = {
                opened = null
                viewModel.refresh()
            },
        )
    }
}
