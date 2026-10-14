package com.docreader.app

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.docreader.core.application.RecentFilesUiState
import com.docreader.core.domain.Document
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecentFilesViewModel(private val container: AppContainer) : ViewModel() {

    val state: StateFlow<RecentFilesUiState> = container.recentFilesStateHolder.state

    private val _lastOpened = MutableStateFlow<Document?>(null)

    /** Documento recém-aberto pelo seletor; consumido pela UI para navegar até o viewer. */
    val lastOpened: StateFlow<Document?> = _lastOpened.asStateFlow()

    fun refresh() {
        viewModelScope.launch { container.recentFilesStateHolder.refresh() }
    }

    fun onDocumentPicked(uri: Uri) {
        viewModelScope.launch {
            val loaded = container.documentLoader.load(uri)
            if (loaded != null) {
                container.openDocument(loaded)
                _lastOpened.value = loaded
            }
            container.recentFilesStateHolder.refresh()
        }
    }

    fun consumeLastOpened() {
        _lastOpened.value = null
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { RecentFilesViewModel(container) }
        }
    }
}
