package com.example.data.repository

import android.content.Context
import com.example.data.ai.GeminiAiService
import com.example.data.ai.GenerationResult
import com.example.data.ai.LocalStoryGenerator
import com.example.data.local.AppDatabase
import com.example.data.local.ProjectConverters
import com.example.data.local.ProjectEntity
import com.example.data.model.AspectRatioFormat
import com.example.data.model.ScriptLanguage
import com.example.data.model.StoryGenre
import com.example.data.model.StudioProject
import com.example.data.model.VideoModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class StudioRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.projectDao()
    private val converters = ProjectConverters()
    private val geminiService = GeminiAiService(context)

    val projectsFlow: Flow<List<StudioProject>> = dao.getAllProjects().map { entities ->
        entities.map { entity ->
            val characters = converters.toCharactersList(entity.charactersJson)
            val scenes = converters.toScenesList(entity.scenesJson)
            val tags = converters.toTagsList(entity.tagsJson)
            entity.toDomain(characters, scenes, tags)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getProjectById(id: String): StudioProject? = withContext(Dispatchers.IO) {
        val entity = dao.getProjectByIdOnce(id) ?: return@withContext null
        val characters = converters.toCharactersList(entity.charactersJson)
        val scenes = converters.toScenesList(entity.scenesJson)
        val tags = converters.toTagsList(entity.tagsJson)
        entity.toDomain(characters, scenes, tags)
    }

    suspend fun saveProject(project: StudioProject) = withContext(Dispatchers.IO) {
        val updatedProject = project.copy(updatedAt = System.currentTimeMillis())
        val entity = ProjectEntity.fromDomain(
            project = updatedProject,
            charactersJson = converters.fromCharactersList(updatedProject.characters),
            scenesJson = converters.fromScenesList(updatedProject.scenes),
            tagsJson = converters.fromTagsList(updatedProject.tags)
        )
        dao.insertProject(entity)
    }

    suspend fun duplicateProject(original: StudioProject): StudioProject = withContext(Dispatchers.IO) {
        val duplicated = original.copy(
            id = UUID.randomUUID().toString(),
            title = "${original.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        saveProject(duplicated)
        duplicated
    }

    suspend fun deleteProject(id: String) = withContext(Dispatchers.IO) {
        dao.deleteProjectById(id)
    }

    suspend fun seedPresetsIfEmpty() = withContext(Dispatchers.IO) {
        if (dao.getProjectCount() == 0) {
            val presets = LocalStoryGenerator.getStarterPresets()
            for (p in presets) {
                val entity = ProjectEntity.fromDomain(
                    project = p,
                    charactersJson = converters.fromCharactersList(p.characters),
                    scenesJson = converters.fromScenesList(p.scenes),
                    tagsJson = converters.fromTagsList(p.tags)
                )
                dao.insertProject(entity)
            }
        }
    }

    suspend fun generateStoryWithGemini(
        title: String,
        premise: String,
        genre: StoryGenre,
        targetModel: VideoModel,
        aspectRatio: AspectRatioFormat,
        sceneCount: Int,
        sceneDurationSeconds: Int,
        language: ScriptLanguage
    ): GenerationResult {
        return geminiService.generateStory(
            title = title,
            premise = premise,
            genre = genre,
            targetModel = targetModel,
            aspectRatio = aspectRatio,
            sceneCount = sceneCount,
            sceneDurationSeconds = sceneDurationSeconds,
            language = language
        )
    }

    fun generateStoryLocally(
        title: String,
        premise: String,
        genre: StoryGenre,
        targetModel: VideoModel,
        aspectRatio: AspectRatioFormat,
        sceneCount: Int,
        sceneDurationSeconds: Int,
        language: ScriptLanguage
    ): StudioProject {
        return LocalStoryGenerator.generateStory(
            title = title,
            premise = premise,
            genre = genre,
            targetModel = targetModel,
            aspectRatio = aspectRatio,
            sceneCount = sceneCount,
            sceneDurationSeconds = sceneDurationSeconds,
            language = language
        )
    }
}
