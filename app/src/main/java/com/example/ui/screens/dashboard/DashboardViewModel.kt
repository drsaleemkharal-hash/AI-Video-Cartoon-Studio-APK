package com.example.ui.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.StoryGenre
import com.example.data.model.StudioProject
import com.example.data.model.VideoModel
import com.example.data.repository.StudioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val projects: List<StudioProject> = emptyList(),
    val searchQuery: String = "",
    val selectedGenreFilter: StoryGenre? = null,
    val selectedModelFilter: VideoModel? = null,
    val isLoading: Boolean = false,
    val message: String? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StudioRepository(application)

    private val _searchQuery = MutableStateFlow("")
    private val _selectedGenre = MutableStateFlow<StoryGenre?>(null)
    private val _selectedModel = MutableStateFlow<VideoModel?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _message = MutableStateFlow<String?>(null)

    private data class FilterCriteria(
        val query: String,
        val genre: StoryGenre?,
        val model: VideoModel?
    )

    private val _filterCriteria = combine(
        _searchQuery,
        _selectedGenre,
        _selectedModel
    ) { query, genre, model ->
        FilterCriteria(query, genre, model)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.projectsFlow,
        _filterCriteria,
        _isLoading,
        _message
    ) { projects, filter, loading, msg ->
        val filtered = projects.filter { p ->
            val matchesQuery = filter.query.isBlank() ||
                p.title.contains(filter.query, ignoreCase = true) ||
                p.synopsis.contains(filter.query, ignoreCase = true) ||
                p.characters.any { it.name.contains(filter.query, ignoreCase = true) }
            val matchesGenre = filter.genre == null || p.genre == filter.genre
            val matchesModel = filter.model == null || p.targetModel == filter.model
            matchesQuery && matchesGenre && matchesModel
        }
        DashboardUiState(
            projects = filtered,
            searchQuery = filter.query,
            selectedGenreFilter = filter.genre,
            selectedModelFilter = filter.model,
            isLoading = loading,
            message = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    init {
        viewModelScope.launch {
            repository.seedPresetsIfEmpty()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onGenreFilterSelected(genre: StoryGenre?) {
        _selectedGenre.value = if (_selectedGenre.value == genre) null else genre
    }

    fun onModelFilterSelected(model: VideoModel?) {
        _selectedModel.value = if (_selectedModel.value == model) null else model
    }

    fun duplicateProject(project: StudioProject) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.duplicateProject(project)
                _message.value = "Project duplicated successfully!"
            } catch (e: Exception) {
                _message.value = "Failed to duplicate: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            _message.value = "Project deleted."
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
