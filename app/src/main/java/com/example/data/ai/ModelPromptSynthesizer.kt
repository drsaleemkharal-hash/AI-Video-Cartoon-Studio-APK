package com.example.data.ai

import com.example.data.model.AspectRatioFormat
import com.example.data.model.CharacterProfile
import com.example.data.model.ScenePrompt
import com.example.data.model.StoryGenre
import com.example.data.model.VideoModel

object ModelPromptSynthesizer {

    /**
     * Synthesizes an optimized, model-specific visual prompt for AI video generators.
     * Incorporates consistent character physical tags, camera motion, lighting, and platform tags.
     */
    fun synthesizePrompt(
        model: VideoModel,
        genre: StoryGenre,
        aspectRatio: AspectRatioFormat,
        scene: ScenePrompt,
        characters: List<CharacterProfile>
    ): String {
        // Collect appearance prompts for characters present in this scene
        val featuredChars = characters.filter { it.id in scene.featuredCharacterIds }
        val charTags = if (featuredChars.isNotEmpty()) {
            featuredChars.joinToString(separator = ". ") { char ->
                "${char.name} (${char.appearancePromptTag})"
            }
        } else {
            ""
        }

        val cameraTag = scene.cameraMovement.promptModifier
        val lighting = scene.lightingAtmosphere.ifBlank { "Cinematic studio lighting" }
        val setting = scene.settingDescription.ifBlank { "Stylized background environment" }
        val durationTag = "${scene.durationSeconds}s clip"
        val ratioTag = "--ar ${aspectRatio.ratioString}"

        val genreStyleTag = when (genre) {
            StoryGenre.CARTOON_3D -> "Pixar and Disney 3D animation style, vibrant stylized rendering, expressive facial animations, soft subsurface scattering"
            StoryGenre.ANIME -> "Modern high-end Kyoto Animation / Makoto Shinkai anime aesthetic, cel-shaded, crisp line art, beautiful atmospheric lighting"
            StoryGenre.CYBERPUNK -> "Cyberpunk cinematic sci-fi, neon-drenched reflections, holographic glows, volumetric fog, Unreal Engine 5 render"
            StoryGenre.CINEMATIC -> "Hyper-realistic 8K cinematic footage, 35mm anamorphic lens, natural film grain, shallow depth of field, photorealistic"
            StoryGenre.CLAYMATION -> "Detailed stop-motion claymation, handmade Aardman style, tactile plasticine textures, subtle fingerprint imperfections, charming physical animation"
            StoryGenre.WATERCOLOR -> "Hand-painted watercolor animation, dynamic paper texture, bleeding color gradients, whimsical storybook illustration"
            StoryGenre.PIXEL_ART -> "Detailed 32-bit pixel art animation, dynamic lighting shaders, rich palette, fluid sprite animation"
            StoryGenre.DARK_FANTASY -> "Dark fantasy cinematic atmosphere, gothic architectural grandeur, dramatic chiaroscuro lighting, mystical particles"
        }

        return when (model) {
            VideoModel.GOOGLE_VEO -> {
                // Google Veo excels at cinematic direction, lenses, and natural physical consistency
                buildString {
                    append("Cinematic Scene: $setting. ")
                    if (charTags.isNotBlank()) append("Subjects: $charTags. ")
                    append("Action: ${scene.title}. ")
                    append("Visual Style: $genreStyleTag. ")
                    append("Camera: $cameraTag, 35mm master prime lens. ")
                    append("Lighting & Environment: $lighting. ")
                    append("Technical Parameters: $durationTag, photorealistic motion coherence, 4K resolution, $ratioTag.")
                }
            }

            VideoModel.KLING_AI -> {
                // Kling AI emphasizes dynamic fluid motion, realistic physics, and expressive motion paths
                buildString {
                    append("$setting, ${scene.title}. ")
                    if (charTags.isNotBlank()) append("Featuring $charTags. ")
                    append("Style: $genreStyleTag. ")
                    append("Motion: Dynamic motion, fluid character physics, $cameraTag. ")
                    append("Atmosphere: $lighting. ")
                    append("Ultra-high definition, rich details, continuous smooth frame pacing, $durationTag, $ratioTag.")
                }
            }

            VideoModel.SEEDANCE -> {
                // Seedance focuses on high frame rate cartoon animation and expressive keyframes
                buildString {
                    append("High-framerate 60fps animation: $setting. ")
                    if (charTags.isNotBlank()) append("Character model: $charTags. ")
                    append("Scene action: ${scene.title}. ")
                    append("Art style: $genreStyleTag. ")
                    append("Camera movement: $cameraTag. ")
                    append("Mood & color grading: $lighting. ")
                    append("Perfect keyframe animation, fluid squash and stretch, $ratioTag.")
                }
            }

            VideoModel.RUNWAY_GEN3 -> {
                // Runway Gen-3 Alpha benefits from Director Mode structured command cues
                buildString {
                    append("Cinematic Master Shot: $setting. ")
                    if (charTags.isNotBlank()) append("[Characters: $charTags] ")
                    append("[Action: ${scene.title}] ")
                    append("[Director Camera: $cameraTag] ")
                    append("[Aesthetic: $genreStyleTag] ")
                    append("[Lighting: $lighting] ")
                    append("[Temporal Quality: Ultra-smooth temporal coherence, no jitter, $ratioTag, $durationTag]")
                }
            }

            VideoModel.PIXVERSE -> {
                // PixVerse thrives on 3D character consistency, subsurface skin rendering, and cartoon lighting
                buildString {
                    append("Stylized 3D Feature Film: ${scene.title} in $setting. ")
                    if (charTags.isNotBlank()) append("Main character design: $charTags. ")
                    append("Animation aesthetic: $genreStyleTag. ")
                    append("Cinematography: $cameraTag with shallow depth of field. ")
                    append("Lighting setup: $lighting with rim highlights. ")
                    append("Character consistency lock, rich textures, vibrant grading, $ratioTag.")
                }
            }

            VideoModel.HAILUO_MINIMAX -> {
                // Hailuo (MiniMax) excels in complex multi-subject interactions and physical realism
                buildString {
                    append("Realistic sequence: $setting. ")
                    if (charTags.isNotBlank()) append("Cast: $charTags. ")
                    append("Physical interaction: ${scene.title}. ")
                    append("Atmosphere and style: $genreStyleTag, $lighting. ")
                    append("Camera choreography: $cameraTag. ")
                    append("Physics simulation: Natural weight and momentum, continuous narrative fluidity, $durationTag, $ratioTag.")
                }
            }
        }
    }

    /**
     * Synthesizes formatted Negative Prompts tailored to prevent artifacts common in AI video rendering.
     */
    fun getOptimizedNegativePrompt(genre: StoryGenre): String {
        val base = "flickering, morphing artifacts, distorted limbs, extra limbs, merged characters, abrupt cuts, low resolution, blurry faces, distorted eyes, text watermark, logo, frame stutter"
        return when (genre) {
            StoryGenre.CARTOON_3D, StoryGenre.CLAYMATION ->
                "$base, bad topology, broken polygons, plastic uncanny skin, messy hair clipping"
            StoryGenre.ANIME ->
                "$base, 3D CGI look, western cartoon style, messy lines, oversaturated colors"
            StoryGenre.CINEMATIC ->
                "$base, cartoon rendering, painted illustration, oversaturated, amateur footage, blown-out highlights"
            else -> base
        }
    }
}
