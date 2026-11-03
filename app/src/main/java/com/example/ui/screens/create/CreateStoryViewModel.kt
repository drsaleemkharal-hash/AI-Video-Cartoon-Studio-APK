package com.example.ui.screens.create

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GenerationResult
import com.example.data.model.AspectRatioFormat
import com.example.data.model.ScriptLanguage
import com.example.data.model.StoryGenre
import com.example.data.model.StudioProject
import com.example.data.model.VideoModel
import com.example.data.repository.StudioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CreateUiState {
    data object Idle : CreateUiState
    data class Generating(val message: String) : CreateUiState
    data class Success(val createdProject: StudioProject) : CreateUiState
    data class Error(val message: String, val canFallbackToLocal: Boolean = true) : CreateUiState
}

class CreateStoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StudioRepository(application)

    val title = MutableStateFlow("")
    val premise = MutableStateFlow("")
    val selectedGenre = MutableStateFlow(StoryGenre.CARTOON_3D)
    val selectedModel = MutableStateFlow(VideoModel.GOOGLE_VEO)
    val selectedAspectRatio = MutableStateFlow(AspectRatioFormat.PORTRAIT_9_16)
    val sceneDurationSeconds = MutableStateFlow(8) // Configurable 6 to 10
    val sceneCount = MutableStateFlow(3)
    val selectedLanguage = MutableStateFlow(ScriptLanguage.BILINGUAL_MULTILINGUAL)

    private val _uiState = MutableStateFlow<CreateUiState>(CreateUiState.Idle)
    val uiState: StateFlow<CreateUiState> = _uiState.asStateFlow()

    fun setPremise(text: String) {
        premise.value = text
    }

    fun setGenre(genre: StoryGenre) {
        selectedGenre.value = genre
    }

    fun setModel(model: VideoModel) {
        selectedModel.value = model
    }

    fun setAspectRatio(ratio: AspectRatioFormat) {
        selectedAspectRatio.value = ratio
    }

    fun setDuration(seconds: Int) {
        sceneDurationSeconds.value = seconds.coerceIn(6, 10)
    }

    fun setScenes(count: Int) {
        sceneCount.value = count.coerceIn(2, 8)
    }

    fun setLanguage(lang: ScriptLanguage) {
        selectedLanguage.value = lang
    }

    fun generateWithGemini() {
        if (premise.value.isBlank()) {
            _uiState.value = CreateUiState.Error("Please enter a story topic or premise.", canFallbackToLocal = false)
            return
        }

        viewModelScope.launch {
            _uiState.value = CreateUiState.Generating("Connecting to Gemini AI Logic...")
            val result = repository.generateStoryWithGemini(
                title = title.value.trim(),
                premise = premise.value.trim(),
                genre = selectedGenre.value,
                targetModel = selectedModel.value,
                aspectRatio = selectedAspectRatio.value,
                sceneCount = sceneCount.value,
                sceneDurationSeconds = sceneDurationSeconds.value,
                language = selectedLanguage.value
            )

            when (result) {
                is GenerationResult.Success -> {
                    repository.saveProject(result.project)
                    _uiState.value = CreateUiState.Success(result.project)
                }
                is GenerationResult.Failure -> {
                    _uiState.value = CreateUiState.Error(
                        message = result.message,
                        canFallbackToLocal = result.canFallback
                    )
                }
            }
        }
    }

    fun generateWithLocalEngine() {
        if (premise.value.isBlank()) {
            _uiState.value = CreateUiState.Error("Please enter a story topic or premise.", canFallbackToLocal = false)
            return
        }

        viewModelScope.launch {
            _uiState.value = CreateUiState.Generating("Generating with Local Studio Engine...")
            try {
                val project = repository.generateStoryLocally(
                    title = title.value.trim(),
                    premise = premise.value.trim(),
                    genre = selectedGenre.value,
                    targetModel = selectedModel.value,
                    aspectRatio = selectedAspectRatio.value,
                    sceneCount = sceneCount.value,
                    sceneDurationSeconds = sceneDurationSeconds.value,
                    language = selectedLanguage.value
                )
                repository.saveProject(project)
                _uiState.value = CreateUiState.Success(project)
            } catch (e: Exception) {
                _uiState.value = CreateUiState.Error("Local generation failed: ${e.message}", canFallbackToLocal = false)
            }
        }
    }

    fun resetState() {
        _uiState.value = CreateUiState.Idle
    }
}
