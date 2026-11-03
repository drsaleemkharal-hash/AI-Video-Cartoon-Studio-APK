package com.example.data.ai

import android.content.Context
import android.util.Log
import com.example.data.model.AspectRatioFormat
import com.example.data.model.CameraMovement
import com.example.data.model.CharacterProfile
import com.example.data.model.DialogueLine
import com.example.data.model.ScenePrompt
import com.example.data.model.ScriptLanguage
import com.example.data.model.StoryGenre
import com.example.data.model.StudioProject
import com.example.data.model.VideoModel
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.util.UUID

private const val TAG = "GeminiAiService"

sealed interface GenerationResult {
    data class Success(val project: StudioProject, val isCloudAi: Boolean) : GenerationResult
    data class Failure(val message: String, val canFallback: Boolean) : GenerationResult
}

class GeminiAiService(private val context: Context) {

    // Lazy initialization to avoid early startup evaluation
    private val model by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            com.google.firebase.Firebase.ai.generativeModel(
                modelName = "gemini-3.8-flash",
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                    responseSchema = storySchema
                }
            )
        } catch (e: Exception) {
            Log.w(TAG, "Firebase.ai initialization deferred: ${e.message}")
            null
        }
    }

    private val storySchema by lazy {
        Schema.obj(
            properties = mapOf(
                "title" to Schema.string(description = "Catchy cinematic or animation title"),
                "synopsis" to Schema.string(description = "Short storyline overview"),
                "characters" to Schema.array(
                    items = Schema.obj(
                        properties = mapOf(
                            "name" to Schema.string(),
                            "role" to Schema.string(),
                            "appearanceTag" to Schema.string(description = "Detailed physical visual description for visual consistency"),
                            "personality" to Schema.string(),
                            "voiceTone" to Schema.string(),
                            "catchphraseEnglish" to Schema.string(),
                            "catchphraseRomanUrdu" to Schema.string(),
                            "catchphraseUrdu" to Schema.string()
                        )
                    )
                ),
                "scenes" to Schema.array(
                    items = Schema.obj(
                        properties = mapOf(
                            "sceneNumber" to Schema.integer(),
                            "title" to Schema.string(),
                            "durationSeconds" to Schema.integer(description = "Duration between 6 and 10 seconds"),
                            "settingDescription" to Schema.string(),
                            "cameraMovement" to Schema.string(description = "Camera direction"),
                            "lightingAtmosphere" to Schema.string(),
                            "visualPrompt" to Schema.string(description = "Detailed video generation prompt for the target model"),
                            "voiceOverEnglish" to Schema.string(),
                            "voiceOverRomanUrdu" to Schema.string(),
                            "voiceOverUrdu" to Schema.string(),
                            "soundEffects" to Schema.string(),
                            "dialogues" to Schema.array(
                                items = Schema.obj(
                                    properties = mapOf(
                                        "speakerName" to Schema.string(),
                                        "emotion" to Schema.string(),
                                        "englishText" to Schema.string(),
                                        "romanUrduText" to Schema.string(),
                                        "urduText" to Schema.string()
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    /**
     * Attempts to generate a story using Firebase AI Logic (Gemini 3.8 Flash).
     * If Firebase App Check is unprovisioned or network fails, returns Failure with clear details,
     * allowing the ViewModel to either show the exact error or fallback to the local engine as desired.
     */
    suspend fun generateStory(
        title: String,
        premise: String,
        genre: StoryGenre,
        targetModel: VideoModel,
        aspectRatio: AspectRatioFormat,
        sceneCount: Int,
        sceneDurationSeconds: Int,
        language: ScriptLanguage
    ): GenerationResult {
        val generativeModel = model
        if (generativeModel == null) {
            return GenerationResult.Failure(
                message = "Firebase AI Logic not initialized. Local Studio Engine is available.",
                canFallback = true
            )
        }

        val clampedDuration = sceneDurationSeconds.coerceIn(6, 10)
        val clampedScenes = sceneCount.coerceIn(2, 8)

        val prompt = buildString {
            appendLine("You are an expert director and prompt engineer for AI video generation tools like ${targetModel.displayName}.")
            appendLine("Task: Create a $clampedScenes-scene video/cartoon script for the topic: \"$premise\".")
            if (title.isNotBlank()) appendLine("Title hint: $title")
            appendLine("Genre/Style: ${genre.displayName}")
            appendLine("Target Video AI Model: ${targetModel.displayName} (${targetModel.description})")
            appendLine("Aspect Ratio: ${aspectRatio.ratioString}")
            appendLine("Per-Scene Duration: strictly $clampedDuration seconds each (between 6 and 10 seconds).")
            appendLine("Language Mode: ${language.displayName}.")
            appendLine("Requirements:")
            appendLine("1. Include 1 to 3 main characters with highly specific, consistent visual appearance tags (clothing, colors, face, build) that must be maintained in every scene prompt.")
            appendLine("2. For each scene, write: scene title, setting description, camera movement, lighting, a comprehensive video prompt optimized for ${targetModel.displayName}, English voice-over, Roman Urdu voice-over, Urdu script voice-over, sound effect cues, and dialogue lines.")
            appendLine("3. Every dialogue line must include: English text, Roman Urdu transliterated text, and Urdu script (Nastaliq).")
            appendLine("4. Ensure prompt conforms to the JSON schema.")
        }

        return try {
            val response = withTimeout(60_000L) {
                generativeModel.generateContent(prompt)
            }
            val jsonText = response.text
            if (jsonText.isNullOrBlank()) {
                GenerationResult.Failure("Gemini returned an empty response.", canFallback = true)
            } else {
                val project = parseJsonToProject(
                    jsonText = jsonText,
                    genre = genre,
                    targetModel = targetModel,
                    aspectRatio = aspectRatio,
                    language = language,
                    fallbackDuration = clampedDuration
                )
                GenerationResult.Success(project, isCloudAi = true)
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini generation timed out", e)
            GenerationResult.Failure("AI generation timed out after 60 seconds.", canFallback = true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini generation error: ${e.message}", e)
            GenerationResult.Failure(
                message = e.localizedMessage ?: "AI generation failed.",
                canFallback = true
            )
        }
    }

    private fun parseJsonToProject(
        jsonText: String,
        genre: StoryGenre,
        targetModel: VideoModel,
        aspectRatio: AspectRatioFormat,
        language: ScriptLanguage,
        fallbackDuration: Int
    ): StudioProject {
        val root = JSONObject(jsonText)
        val title = root.optString("title", "AI Studio Creation")
        val synopsis = root.optString("synopsis", "")

        val charList = mutableListOf<CharacterProfile>()
        val charactersJson = root.optJSONArray("characters")
        if (charactersJson != null) {
            for (i in 0 until charactersJson.length()) {
                val cObj = charactersJson.getJSONObject(i)
                val charId = "char_${i + 1}"
                charList.add(
                    CharacterProfile(
                        id = charId,
                        name = cObj.optString("name", "Character ${i + 1}"),
                        role = cObj.optString("role", "Protagonist"),
                        appearancePromptTag = cObj.optString("appearanceTag", "Stylized 3D character"),
                        personality = cObj.optString("personality", "Expressive"),
                        voiceTone = cObj.optString("voiceTone", "Clear"),
                        catchphraseEnglish = cObj.optString("catchphraseEnglish", ""),
                        catchphraseRomanUrdu = cObj.optString("catchphraseRomanUrdu", ""),
                        catchphraseUrdu = cObj.optString("catchphraseUrdu", "")
                    )
                )
            }
        }

        val sceneList = mutableListOf<ScenePrompt>()
        val scenesJson = root.optJSONArray("scenes")
        if (scenesJson != null) {
            for (i in 0 until scenesJson.length()) {
                val sObj = scenesJson.getJSONObject(i)
                val duration = sObj.optInt("durationSeconds", fallbackDuration).coerceIn(6, 10)
                val sceneNum = sObj.optInt("sceneNumber", i + 1)
                val sTitle = sObj.optString("title", "Scene $sceneNum")
                val setting = sObj.optString("settingDescription", "")
                val lighting = sObj.optString("lightingAtmosphere", "Cinematic lighting")
                val visualPrompt = sObj.optString("visualPrompt", "")
                val voEng = sObj.optString("voiceOverEnglish", "")
                val voRoman = sObj.optString("voiceOverRomanUrdu", "")
                val voUrdu = sObj.optString("voiceOverUrdu", "")
                val sfx = sObj.optString("soundEffects", "")

                val dialogues = mutableListOf<DialogueLine>()
                val diaJson = sObj.optJSONArray("dialogues")
                if (diaJson != null) {
                    for (d in 0 until diaJson.length()) {
                        val dObj = diaJson.getJSONObject(d)
                        dialogues.add(
                            DialogueLine(
                                characterName = dObj.optString("speakerName", "Speaker"),
                                emotion = dObj.optString("emotion", "Neutral"),
                                englishText = dObj.optString("englishText", ""),
                                romanUrduText = dObj.optString("romanUrduText", ""),
                                urduText = dObj.optString("urduText", "")
                            )
                        )
                    }
                }

                val scene = ScenePrompt(
                    id = UUID.randomUUID().toString(),
                    sceneNumber = sceneNum,
                    title = sTitle,
                    durationSeconds = duration,
                    settingDescription = setting,
                    cameraMovement = CameraMovement.SLOW_ZOOM_IN,
                    lightingAtmosphere = lighting,
                    featuredCharacterIds = charList.map { it.id },
                    visualPrompt = visualPrompt.ifBlank {
                        ModelPromptSynthesizer.synthesizePrompt(
                            model = targetModel,
                            genre = genre,
                            aspectRatio = aspectRatio,
                            scene = ScenePrompt(
                                sceneNumber = sceneNum,
                                title = sTitle,
                                durationSeconds = duration,
                                settingDescription = setting,
                                lightingAtmosphere = lighting
                            ),
                            characters = charList
                        )
                    },
                    voiceOverEnglish = voEng,
                    voiceOverRomanUrdu = voRoman,
                    voiceOverUrdu = voUrdu,
                    dialogues = dialogues,
                    soundEffectsCue = sfx
                )
                sceneList.add(scene)
            }
        }

        return StudioProject(
            id = UUID.randomUUID().toString(),
            title = title,
            synopsis = synopsis,
            genre = genre,
            targetModel = targetModel,
            aspectRatio = aspectRatio,
            defaultSceneDurationSeconds = fallbackDuration,
            language = language,
            characters = charList,
            scenes = sceneList,
            tags = listOf(genre.displayName, targetModel.displayName),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}
