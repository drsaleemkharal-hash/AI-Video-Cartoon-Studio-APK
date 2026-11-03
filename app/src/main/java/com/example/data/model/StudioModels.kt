package com.example.data.model

import java.util.UUID

enum class VideoModel(
    val displayName: String,
    val vendor: String,
    val description: String,
    val recommendedPromptLength: String,
    val badgeColorHex: Long = 0xFF8B5CF6
) {
    GOOGLE_VEO(
        displayName = "Google Veo",
        vendor = "Google DeepMind",
        description = "High cinematic realism, camera direction & spatial consistency",
        recommendedPromptLength = "Medium-Long (detailed cinematic instructions)",
        badgeColorHex = 0xFF4285F4
    ),
    KLING_AI(
        displayName = "Kling AI",
        vendor = "Kuaishou",
        description = "Dynamic motion, physical realism & smooth character movements",
        recommendedPromptLength = "Long (movement + lighting + atmosphere)",
        badgeColorHex = 0xFFFF6D00
    ),
    SEEDANCE(
        displayName = "Seedance",
        vendor = "Seedance AI",
        description = "High-framerate 2D/3D cartoon animation & expressive anime styles",
        recommendedPromptLength = "Medium (animation style + character emotion)",
        badgeColorHex = 0xFF00E676
    ),
    RUNWAY_GEN3(
        displayName = "Runway Gen-3",
        vendor = "RunwayML",
        description = "Director-level camera controls, cinematic pacing & lighting",
        recommendedPromptLength = "Detailed (Director cues, camera pan/zoom)",
        badgeColorHex = 0xFF9C27B0
    ),
    PIXVERSE(
        displayName = "PixVerse",
        vendor = "PixVerse",
        description = "Pixar-grade 3D character consistency, vibrant colors & cartoons",
        recommendedPromptLength = "Medium (subsurface scattering, 3D character details)",
        badgeColorHex = 0xFFFF4081
    ),
    HAILUO_MINIMAX(
        displayName = "Hailuo AI",
        vendor = "MiniMax",
        description = "Complex narrative action, physical interactions & fluid timing",
        recommendedPromptLength = "Long (coherent physical sequence & camera angle)",
        badgeColorHex = 0xFF00B0FF
    )
}

enum class AspectRatioFormat(
    val label: String,
    val ratioString: String,
    val platformTarget: String
) {
    PORTRAIT_9_16("9:16 Vertical", "9:16", "Shorts, Reels, TikTok"),
    LANDSCAPE_16_9("16:9 Cinema", "16:9", "YouTube, TV, Presentations"),
    SQUARE_1_1("1:1 Square", "1:1", "Instagram Feed, Square Posts")
}

enum class StoryGenre(val displayName: String, val iconName: String) {
    CARTOON_3D("3D Cartoon / Animation", "animation"),
    ANIME("Anime / Manga Style", "brush"),
    CYBERPUNK("Sci-Fi Cyberpunk", "bolt"),
    CINEMATIC("Cinematic Realism", "movie"),
    CLAYMATION("Claymation / Stop-Motion", "palette"),
    WATERCOLOR("Watercolor Storybook", "water_drop"),
    PIXEL_ART("Retro Pixel Art", "grid_4x4"),
    DARK_FANTASY("Mythic Fantasy", "auto_awesome")
}

enum class ScriptLanguage(val displayName: String, val flag: String) {
    ENGLISH_ONLY("English Only", "🇺🇸"),
    ROMAN_URDU("Roman Urdu (Transliterated)", "🇵🇰"),
    URDU_NASTALIQ("Urdu (Nastaliq Script)", "🇵🇰"),
    BILINGUAL_MULTILINGUAL("Bilingual (English + Roman Urdu + Urdu)", "🌐")
}

enum class CameraMovement(val label: String, val promptModifier: String) {
    SLOW_ZOOM_IN("Slow Zoom In", "Slow cinematic zoom in towards the subject"),
    SLOW_PAN_RIGHT("Slow Pan Right", "Smooth horizontal camera pan from left to right"),
    DRONE_SWEEP("Drone Aerial Sweep", "Sweeping aerial drone shot descending gently"),
    LOW_ANGLE_ORBIT("Low Angle Orbit", "Low-angle dynamic orbital tracking shot"),
    STATIC_CLOSE_UP("Static Close-up", "Static locked-off intimate close-up shot with shallow depth of field"),
    WHIP_PAN("Dynamic Whip Pan", "Fast cinematic whip pan transition revealing the action")
}

data class CharacterProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val role: String = "Protagonist", // Protagonist, Antagonist, Sidekick, Mentor, Comic Relief
    val appearancePromptTag: String = "", // e.g. "Chubby brown bear wearing blue sailor cap and denim overalls"
    val personality: String = "Curious and brave",
    val voiceTone: String = "Warm, enthusiastic",
    val catchphraseEnglish: String = "",
    val catchphraseRomanUrdu: String = "",
    val catchphraseUrdu: String = "",
    val avatarColorHex: Long = 0xFF8B5CF6
)

data class DialogueLine(
    val id: String = UUID.randomUUID().toString(),
    val characterId: String = "",
    val characterName: String,
    val emotion: String = "Neutral", // Excited, Confused, Scared, Joyful, Mysterious
    val englishText: String = "",
    val romanUrduText: String = "",
    val urduText: String = ""
)

data class ScenePrompt(
    val id: String = UUID.randomUUID().toString(),
    val sceneNumber: Int,
    val title: String,
    val durationSeconds: Int = 8, // Strictly 6 to 10 seconds
    val settingDescription: String,
    val cameraMovement: CameraMovement = CameraMovement.SLOW_ZOOM_IN,
    val lightingAtmosphere: String = "Soft cinematic lighting with vibrant color contrast",
    val featuredCharacterIds: List<String> = emptyList(),
    val visualPrompt: String = "",
    val negativePrompt: String = "low quality, distorted limbs, extra fingers, text watermark, blurry, flickering, artifacting, abrupt cuts",
    val voiceOverEnglish: String = "",
    val voiceOverRomanUrdu: String = "",
    val voiceOverUrdu: String = "",
    val dialogues: List<DialogueLine> = emptyList(),
    val soundEffectsCue: String = ""
)

data class StudioProject(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val synopsis: String,
    val genre: StoryGenre = StoryGenre.CARTOON_3D,
    val targetModel: VideoModel = VideoModel.GOOGLE_VEO,
    val aspectRatio: AspectRatioFormat = AspectRatioFormat.PORTRAIT_9_16,
    val defaultSceneDurationSeconds: Int = 8,
    val language: ScriptLanguage = ScriptLanguage.BILINGUAL_MULTILINGUAL,
    val characters: List<CharacterProfile> = emptyList(),
    val scenes: List<ScenePrompt> = emptyList(),
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
