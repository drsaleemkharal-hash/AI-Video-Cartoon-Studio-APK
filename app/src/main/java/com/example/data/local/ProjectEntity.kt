package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AspectRatioFormat
import com.example.data.model.CharacterProfile
import com.example.data.model.ScenePrompt
import com.example.data.model.ScriptLanguage
import com.example.data.model.StoryGenre
import com.example.data.model.StudioProject
import com.example.data.model.VideoModel

@Entity(tableName = "studio_projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val synopsis: String,
    val genre: String,
    val targetModel: String,
    val aspectRatio: String,
    val defaultSceneDurationSeconds: Int,
    val language: String,
    val charactersJson: String,
    val scenesJson: String,
    val tagsJson: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(
        characters: List<CharacterProfile>,
        scenes: List<ScenePrompt>,
        tags: List<String>
    ): StudioProject {
        return StudioProject(
            id = id,
            title = title,
            synopsis = synopsis,
            genre = try { StoryGenre.valueOf(genre) } catch (_: Exception) { StoryGenre.CARTOON_3D },
            targetModel = try { VideoModel.valueOf(targetModel) } catch (_: Exception) { VideoModel.GOOGLE_VEO },
            aspectRatio = try { AspectRatioFormat.valueOf(aspectRatio) } catch (_: Exception) { AspectRatioFormat.PORTRAIT_9_16 },
            defaultSceneDurationSeconds = defaultSceneDurationSeconds,
            language = try { ScriptLanguage.valueOf(language) } catch (_: Exception) { ScriptLanguage.BILINGUAL_MULTILINGUAL },
            characters = characters,
            scenes = scenes,
            tags = tags,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(
            project: StudioProject,
            charactersJson: String,
            scenesJson: String,
            tagsJson: String
        ): ProjectEntity {
            return ProjectEntity(
                id = project.id,
                title = project.title,
                synopsis = project.synopsis,
                genre = project.genre.name,
                targetModel = project.targetModel.name,
                aspectRatio = project.aspectRatio.name,
                defaultSceneDurationSeconds = project.defaultSceneDurationSeconds,
                language = project.language.name,
                charactersJson = charactersJson,
                scenesJson = scenesJson,
                tagsJson = tagsJson,
                createdAt = project.createdAt,
                updatedAt = project.updatedAt
            )
        }
    }
}
