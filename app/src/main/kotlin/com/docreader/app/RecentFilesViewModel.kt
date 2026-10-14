package com.docreader.app

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.docreader.core.application.RecentFilesUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecentFilesViewModel(private val container: AppContainer) : ViewModel() {

    val state: StateFlow<RecentFilesUiState> = container.recentFilesStateHolder.state

    fun refresh() {
        viewModelScope.launch { container.recentFilesStateHolder.refresh() }
    }

    fun onDocumentPicked(uri: Uri) {
        viewModelScope.launch {
            container.documentLoader.load(uri)?.let { container.openDocument(it) }
            container.recentFilesStateHolder.refresh()
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { RecentFilesViewModel(container) }
        }
    }
}
