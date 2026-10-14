package com.docreader.core.application

import com.docreader.core.domain.Document
import com.docreader.core.domain.RecentDocuments
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Estado da tela "Abrir arquivo". */
data class RecentFilesUiState(
    val isLoading: Boolean = false,
    val documents: List<Document> = emptyList(),
    val errorMessage: String? = null,
)

/** Mantém o estado da lista de recentes, independente da UI. */
class RecentFilesStateHolder(
    private val getRecentDocuments: GetRecentDocuments,
) {
    private val _state = MutableStateFlow(RecentFilesUiState())
    val state: StateFlow<RecentFilesUiState> = _state.asStateFlow()

    suspend fun refresh(limit: Int = RecentDocuments.DEFAULT_LIMIT) {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        _state.value = try {
            RecentFilesUiState(documents = getRecentDocuments(limit))
        } catch (error: Exception) {
            RecentFilesUiState(errorMessage = error.message ?: "Falha ao listar os arquivos recentes")
        }
    }
}
