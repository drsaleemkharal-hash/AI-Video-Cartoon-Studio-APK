package com.example

import com.example.data.ai.LocalStoryGenerator
import com.example.data.ai.ModelPromptSynthesizer
import com.example.data.model.AspectRatioFormat
import com.example.data.model.ScriptLanguage
import com.example.data.model.StoryGenre
import com.example.data.model.VideoModel
import com.example.util.ExportHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `test story generation duration constraint 6 to 10 seconds`() {
        val project = LocalStoryGenerator.generateStory(
            title = "Test Story",
            premise = "A cyber raccoon solving mysteries in space",
            genre = StoryGenre.CARTOON_3D,
            targetModel = VideoModel.GOOGLE_VEO,
            aspectRatio = AspectRatioFormat.PORTRAIT_9_16,
            sceneCount = 4,
            sceneDurationSeconds = 8,
            language = ScriptLanguage.BILINGUAL_MULTILINGUAL
        )

        assertNotNull(project)
        assertEquals(4, project.scenes.size)
        project.scenes.forEach { scene ->
            assertTrue("Scene duration must be between 6 and 10s", scene.durationSeconds in 6..10)
            assertTrue("Visual prompt must not be blank", scene.visualPrompt.isNotBlank())
        }
        assertTrue("Characters must be created", project.characters.isNotEmpty())
    }

    @Test
    fun `test model prompt synthesizer produces tailored outputs for all models`() {
        val project = LocalStoryGenerator.generateStory(
            title = "Prompt Test",
            premise = "Neon dragon soaring over modern highrises",
            genre = StoryGenre.ANIME,
            targetModel = VideoModel.KLING_AI,
            aspectRatio = AspectRatioFormat.LANDSCAPE_16_9,
            sceneCount = 2,
            sceneDurationSeconds = 7,
            language = ScriptLanguage.ROMAN_URDU
        )

        val scene = project.scenes.first()

        VideoModel.values().forEach { model ->
            val prompt = ModelPromptSynthesizer.synthesizePrompt(
                model = model,
                genre = project.genre,
                aspectRatio = project.aspectRatio,
                scene = scene,
                characters = project.characters
            )
            assertNotNull(prompt)
            assertTrue("Prompt for ${model.displayName} should not be empty", prompt.length > 20)
        }
    }

    @Test
    fun `test export helper generates screenplay and json`() {
        val project = LocalStoryGenerator.generateStory(
            title = "Dastan-e-Jungle",
            premise = "Animals discovering friendship in Pakistan hills",
            genre = StoryGenre.CARTOON_3D,
            targetModel = VideoModel.PIXVERSE,
            aspectRatio = AspectRatioFormat.PORTRAIT_9_16,
            sceneCount = 3,
            sceneDurationSeconds = 8,
            language = ScriptLanguage.BILINGUAL_MULTILINGUAL
        )

        val txt = ExportHelper.generateProductionScriptTxt(project)
        assertTrue(txt.contains("AI VIDEO & CARTOON STUDIO - PRODUCTION SCRIPT"))
        assertTrue(txt.contains("Dastan-e-Jungle"))

        val json = ExportHelper.generateProjectJson(project)
        assertTrue(json.contains("\"title\": \"Dastan-e-Jungle\""))
        assertTrue(json.contains("\"scenes\""))
    }
}
