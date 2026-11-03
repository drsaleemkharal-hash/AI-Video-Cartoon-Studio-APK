package com.example.ui.screens.editor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.ModelPromptSynthesizer
import com.example.data.model.AspectRatioFormat
import com.example.data.model.CameraMovement
import com.example.data.model.CharacterProfile
import com.example.data.model.DialogueLine
import com.example.data.model.ScenePrompt
import com.example.data.model.StudioProject
import com.example.data.model.VideoModel
import com.example.data.repository.StudioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface EditorUiState {
    data object Loading : EditorUiState
    data class Ready(val project: StudioProject) : EditorUiState
    data class Error(val message: String) : EditorUiState
}

class ProjectEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StudioRepository(application)

    private val _uiState = MutableStateFlow<EditorUiState>(EditorUiState.Loading)
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    fun loadProject(projectId: String) {
        viewModelScope.launch {
            _uiState.value = EditorUiState.Loading
            val project = repository.getProjectById(projectId)
            if (project != null) {
                _uiState.value = EditorUiState.Ready(project)
            } else {
                _uiState.value = EditorUiState.Error("Project not found.")
            }
        }
    }

    fun updateProjectTitle(newTitle: String) {
        val current = currentProject() ?: return
        updateProject(current.copy(title = newTitle))
    }

    fun updateProjectModel(newModel: VideoModel) {
        val current = currentProject() ?: return
        // Automatically re-synthesize scene prompts for the newly chosen video model
        val updatedScenes = current.scenes.map { scene ->
            val newPrompt = ModelPromptSynthesizer.synthesizePrompt(
                model = newModel,
                genre = current.genre,
                aspectRatio = current.aspectRatio,
                scene = scene,
                characters = current.characters
            )
            scene.copy(visualPrompt = newPrompt)
        }
        updateProject(current.copy(targetModel = newModel, scenes = updatedScenes))
        _feedbackMessage.value = "Model updated to ${newModel.displayName} & prompts refreshed!"
    }

    fun updateAspectRatio(newRatio: AspectRatioFormat) {
        val current = currentProject() ?: return
        val updatedScenes = current.scenes.map { scene ->
            val newPrompt = ModelPromptSynthesizer.synthesizePrompt(
                model = current.targetModel,
                genre = current.genre,
                aspectRatio = newRatio,
                scene = scene,
                characters = current.characters
            )
            scene.copy(visualPrompt = newPrompt)
        }
        updateProject(current.copy(aspectRatio = newRatio, scenes = updatedScenes))
        _feedbackMessage.value = "Aspect ratio set to ${newRatio.ratioString}!"
    }

    fun updateScene(updatedScene: ScenePrompt) {
        val current = currentProject() ?: return
        val clampedScene = updatedScene.copy(durationSeconds = updatedScene.durationSeconds.coerceIn(6, 10))
        val newScenes = current.scenes.map { if (it.id == clampedScene.id) clampedScene else it }
        updateProject(current.copy(scenes = newScenes))
    }

    fun addScene() {
        val current = currentProject() ?: return
        val newSceneNumber = current.scenes.size + 1
        val newScene = ScenePrompt(
            id = UUID.randomUUID().toString(),
            sceneNumber = newSceneNumber,
            title = "Scene $newSceneNumber",
            durationSeconds = current.defaultSceneDurationSeconds.coerceIn(6, 10),
            settingDescription = "Continuation scene in ${current.title}",
            cameraMovement = CameraMovement.SLOW_PAN_RIGHT,
            lightingAtmosphere = "Cinematic lighting matching overall tone",
            featuredCharacterIds = current.characters.map { it.id },
            dialogues = emptyList()
        )
        val synthesized = newScene.copy(
            visualPrompt = ModelPromptSynthesizer.synthesizePrompt(
                model = current.targetModel,
                genre = current.genre,
                aspectRatio = current.aspectRatio,
                scene = newScene,
                characters = current.characters
            )
        )
        updateProject(current.copy(scenes = current.scenes + synthesized))
        _feedbackMessage.value = "Scene $newSceneNumber added!"
    }

    fun deleteScene(sceneId: String) {
        val current = currentProject() ?: return
        if (current.scenes.size <= 1) {
            _feedbackMessage.value = "Projects must contain at least 1 scene."
            return
        }
        val remaining = current.scenes.filter { it.id != sceneId }
        val renumbered = remaining.mapIndexed { idx, s -> s.copy(sceneNumber = idx + 1) }
        updateProject(current.copy(scenes = renumbered))
        _feedbackMessage.value = "Scene deleted."
    }

    fun saveCharacter(character: CharacterProfile) {
        val current = currentProject() ?: return
        val existingIndex = current.characters.indexOfFirst { it.id == character.id }
        val updatedCharacters = if (existingIndex >= 0) {
            current.characters.map { if (it.id == character.id) character else it }
        } else {
            current.characters + character
        }
        // Update character appearance consistency in scenes
        val refreshedScenes = current.scenes.map { scene ->
            scene.copy(
                visualPrompt = ModelPromptSynthesizer.synthesizePrompt(
                    model = current.targetModel,
                    genre = current.genre,
                    aspectRatio = current.aspectRatio,
                    scene = scene,
                    characters = updatedCharacters
                )
            )
        }
        updateProject(current.copy(characters = updatedCharacters, scenes = refreshedScenes))
        _feedbackMessage.value = "Character \"${character.name}\" saved!"
    }

    fun deleteCharacter(characterId: String) {
        val current = currentProject() ?: return
        val remaining = current.characters.filter { it.id != characterId }
        updateProject(current.copy(characters = remaining))
        _feedbackMessage.value = "Character removed."
    }

    private fun updateProject(updated: StudioProject) {
        viewModelScope.launch {
            repository.saveProject(updated)
            _uiState.value = EditorUiState.Ready(updated)
        }
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    private fun currentProject(): StudioProject? {
        return (uiState.value as? EditorUiState.Ready)?.project
    }
}
