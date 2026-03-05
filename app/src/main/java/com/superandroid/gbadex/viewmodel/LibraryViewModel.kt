package com.superandroid.gbadex.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.superandroid.gbadex.data.model.Game
import com.superandroid.gbadex.data.repository.RomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryUiState(
    val games: List<Game> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val viewMode: ViewMode = ViewMode.GRID,
    val sortMode: SortMode = SortMode.ALPHABETICAL,
)

enum class ViewMode { GRID, LIST }
enum class SortMode { ALPHABETICAL, LAST_PLAYED, FAVORITES }

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RomRepository(application)

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    /** Called when user picks a ROM folder via SAF folder picker */
    fun onFolderSelected(folderUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val games = repository.scanFolder(folderUri)
                _uiState.update { it.copy(games = games, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Failed to scan folder: ${e.message}")
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onViewModeChanged(mode: ViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun onSortModeChanged(mode: SortMode) {
        _uiState.update { state ->
            val sorted = when (mode) {
                SortMode.ALPHABETICAL -> state.games.sortedBy { it.title }
                SortMode.LAST_PLAYED -> state.games.sortedByDescending { it.lastPlayed ?: 0L }
                SortMode.FAVORITES -> state.games.sortedByDescending { it.isFavorite }
            }
            state.copy(games = sorted, sortMode = mode)
        }
    }

    fun toggleFavorite(game: Game) {
        _uiState.update { state ->
            val updated = state.games.map {
                if (it.id == game.id) it.copy(isFavorite = !it.isFavorite) else it
            }
            state.copy(games = updated)
        }
    }

    /** Filtered games based on current search query */
    fun filteredGames(): List<Game> {
        val query = _uiState.value.searchQuery.trim()
        return if (query.isEmpty()) _uiState.value.games
        else _uiState.value.games.filter {
            it.title.contains(query, ignoreCase = true)
        }
    }
}
