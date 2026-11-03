package com.example.data.ai

import com.example.data.model.AspectRatioFormat
import com.example.data.model.CameraMovement
import com.example.data.model.CharacterProfile
import com.example.data.model.DialogueLine
import com.example.data.model.ScenePrompt
import com.example.data.model.ScriptLanguage
import com.example.data.model.StoryGenre
import com.example.data.model.StudioProject
import com.example.data.model.VideoModel
import java.util.UUID

object LocalStoryGenerator {

    /**
     * Generates a complete project with character profiles, scene-by-scene prompts,
     * dialogues (English, Roman Urdu, Urdu), and voice-over scripts based on the user's premise.
     */
    fun generateStory(
        title: String,
        premise: String,
        genre: StoryGenre,
        targetModel: VideoModel,
        aspectRatio: AspectRatioFormat,
        sceneCount: Int,
        sceneDurationSeconds: Int,
        language: ScriptLanguage
    ): StudioProject {
        val clampedDuration = sceneDurationSeconds.coerceIn(6, 10)
        val clampedSceneCount = sceneCount.coerceIn(2, 8)

        // Generate consistent character profiles suited to the premise and genre
        val characters = generateCharactersForPremise(premise, genre)

        // Generate scenes with dialogue and voice-overs
        val scenes = (1..clampedSceneCount).map { index ->
            generateScene(
                index = index,
                totalScenes = clampedSceneCount,
                premise = premise,
                genre = genre,
                targetModel = targetModel,
                aspectRatio = aspectRatio,
                durationSeconds = clampedDuration,
                characters = characters,
                language = language
            )
        }

        return StudioProject(
            id = UUID.randomUUID().toString(),
            title = title.ifBlank { generateDefaultTitle(premise, genre) },
            synopsis = premise.ifBlank { "An epic adventure crafted in ${genre.displayName} for ${targetModel.displayName}." },
            genre = genre,
            targetModel = targetModel,
            aspectRatio = aspectRatio,
            defaultSceneDurationSeconds = clampedDuration,
            language = language,
            characters = characters,
            scenes = scenes,
            tags = listOf(genre.displayName, targetModel.displayName, aspectRatio.ratioString),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun generateDefaultTitle(premise: String, genre: StoryGenre): String {
        val words = premise.split(" ").filter { it.isNotBlank() }
        return if (words.size >= 2) {
            words.take(4).joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
        } else {
            "Chronicles of ${genre.displayName}"
        }
    }

    private fun generateCharactersForPremise(premise: String, genre: StoryGenre): List<CharacterProfile> {
        val isSciFi = genre == StoryGenre.CYBERPUNK || premise.contains("space", true) || premise.contains("robot", true)
        val isCartoon = genre == StoryGenre.CARTOON_3D || genre == StoryGenre.CLAYMATION

        return if (isCartoon) {
            listOf(
                CharacterProfile(
                    id = "char_1",
                    name = "Milo",
                    role = "Protagonist",
                    appearancePromptTag = "chubby playful animated brown raccoon with expressive amber eyes, wearing a turquoise hoodie and miniature aviator goggles",
                    personality = "Curious, resourceful, slightly mischievous",
                    voiceTone = "High-energy, playful, quick-witted",
                    catchphraseEnglish = "Hold on to your acorns!",
                    catchphraseRomanUrdu = "Dekho ab mera kamal!",
                    catchphraseUrdu = "دیکھو اب میرا کمال!",
                    avatarColorHex = 0xFF38BDF8
                ),
                CharacterProfile(
                    id = "char_2",
                    name = "Professor Babban",
                    role = "Mentor & Comic Relief",
                    appearancePromptTag = "wise elderly white owl with spectacles perched on beak, wearing a maroon knitted scarf and velvet waistcoat",
                    personality = "Wise yet absent-minded and humorous",
                    voiceTone = "Deep, velvety with sudden comical squeaks",
                    catchphraseEnglish = "By the ancient feathers!",
                    catchphraseRomanUrdu = "Kamaal karte ho barkhurdar!",
                    catchphraseUrdu = "کمال کرتے ہو برخوردار!",
                    avatarColorHex = 0xFFA855F7
                )
            )
        } else if (isSciFi) {
            listOf(
                CharacterProfile(
                    id = "char_1",
                    name = "Zane",
                    role = "Protagonist",
                    appearancePromptTag = "cybernetic bounty hunter with sleek matte black tactical armor, glowing cyan visor, messy dark hair, and robotic left arm",
                    personality = "Stoic, tactical, protective",
                    voiceTone = "Low, raspy, determined",
                    catchphraseEnglish = "Target locked in sight.",
                    catchphraseRomanUrdu = "Mission shuru ho chuka hai.",
                    catchphraseUrdu = "مشن شروع ہو چکا ہے۔",
                    avatarColorHex = 0xFF00E5FF
                ),
                CharacterProfile(
                    id = "char_2",
                    name = "Aria",
                    role = "Tech Specialist",
                    appearancePromptTag = "sharp tech hacker with iridescent purple braided hair, neural link interface on temple, and holographic forearm emitter",
                    personality = "Analytical, witty, fearless",
                    voiceTone = "Crisp, fast-paced, confident",
                    catchphraseEnglish = "Firewall breached in three, two, one.",
                    catchphraseRomanUrdu = "System ab hamare control mein hai.",
                    catchphraseUrdu = "سسٹم اب ہمارے کنٹرول میں ہے۔",
                    avatarColorHex = 0xFFEC4899
                )
            )
        } else {
            // General / Anime / Fantasy / Folk tale
            listOf(
                CharacterProfile(
                    id = "char_1",
                    name = "Rayan",
                    role = "Heroic Explorer",
                    appearancePromptTag = "young brave traveler with windswept dark hair, crimson traveler cloak, embroidered brass bracers, and ancient compass necklace",
                    personality = "Courageous, honorable, adventurous",
                    voiceTone = "Warm, steady, inspiring",
                    catchphraseEnglish = "The horizon never lies.",
                    catchphraseRomanUrdu = "Manzil door nahi hai!",
                    catchphraseUrdu = "منزل دور نہیں ہے!",
                    avatarColorHex = 0xFFF59E0B
                ),
                CharacterProfile(
                    id = "char_2",
                    name = "Zara",
                    role = "Mystic Navigator",
                    appearancePromptTag = "clever scholar with flowing emerald scarf, dark eyes, antique brass astrolabe, and leather travel satchel",
                    personality = "Insightful, sharp, mysterious",
                    voiceTone = "Calm, melodic, intriguing",
                    catchphraseEnglish = "Read the stars, not the shadows.",
                    catchphraseRomanUrdu = "Raasta khud humein pukar raha hai.",
                    catchphraseUrdu = "راستہ خود ہمیں پکار رہا ہے۔",
                    avatarColorHex = 0xFF10B981
                )
            )
        }
    }

    private fun generateScene(
        index: Int,
        totalScenes: Int,
        premise: String,
        genre: StoryGenre,
        targetModel: VideoModel,
        aspectRatio: AspectRatioFormat,
        durationSeconds: Int,
        characters: List<CharacterProfile>,
        language: ScriptLanguage
    ): ScenePrompt {
        val p1 = characters.getOrNull(0) ?: CharacterProfile(name = "Hero", appearancePromptTag = "Hero")
        val p2 = characters.getOrNull(1) ?: CharacterProfile(name = "Ally", appearancePromptTag = "Ally")

        val (sceneTitle, camera, setting, voEng, voRoman, voUrdu, dialogues) = when (index) {
            1 -> {
                // Intro / Establishing
                val title = "The Mysterious Discovery"
                val cam = CameraMovement.DRONE_SWEEP
                val set = "Sprawling enchanted environment under dramatic twilight skies with floating luminous particles"
                val eng = "In a realm beyond ordinary imagination, an ancient secret stirs from its deep slumber."
                val roman = "Ek aisi pur-asrar dunya mein, jahan har kona kisi nayi daastan ka pata deta hai."
                val urdu = "ایک ایسی پر اسرار دنیا میں، جہاں ہر کونا کسی نئی داستان کا پتہ دیتا ہے۔"
                val dia = listOf(
                    DialogueLine(
                        characterId = p1.id,
                        characterName = p1.name,
                        emotion = "Awestruck",
                        englishText = "Look at this glow... something incredible is about to happen!",
                        romanUrduText = "Zara dekho yeh roshni... kuch bohot ajeeb hone wala hai!",
                        urduText = "ذرا دیکھو یہ روشنی... کچھ بہت عجیب ہونے والا ہے!"
                    )
                )
                SceneData(title, cam, set, eng, roman, urdu, dia)
            }
            2 -> {
                // Rising Action
                val title = "Deciphering the Clues"
                val cam = CameraMovement.SLOW_ZOOM_IN
                val set = "Intricate ancient workshop or stone chamber filled with glowing relics and ancient scrolls"
                val eng = "Every clue brings them closer to the heart of the puzzle, but time is slipping away."
                val roman = "Har nishan ek naye raaste ki taraf ishara kar raha tha, lekin waqt bohot kam tha."
                val urdu = "ہر نشان ایک نئے راستے کی طرف اشارہ کر رہا تھا، لیکن وقت بہت کم تھا۔"
                val dia = listOf(
                    DialogueLine(
                        characterId = p2.id,
                        characterName = p2.name,
                        emotion = "Focused",
                        englishText = "Careful! One wrong move could trigger the ancient mechanism.",
                        romanUrduText = "Sambhal kar! Ek ghalat qadam aur saara nizaam ult ho sakta hai.",
                        urduText = "سنبھل کر! ایک غلط قدم اور سارا نظام الٹ ہو سکتا ہے۔"
                    ),
                    DialogueLine(
                        characterId = p1.id,
                        characterName = p1.name,
                        emotion = "Determined",
                        englishText = "Trust me, I know exactly what to do.",
                        romanUrduText = "Mujh par bharosa rakho, mujhe theek pata hai kya karna hai.",
                        urduText = "مجھ پر بھروسہ رکھو، مجھے ٹھیک پتہ ہے کیا کرنا ہے۔"
                    )
                )
                SceneData(title, cam, set, eng, roman, urdu, dia)
            }
            3 -> {
                // Climax or Turn
                val title = "The Great Awakening"
                val cam = CameraMovement.LOW_ANGLE_ORBIT
                val set = "Vast crystalline cavern with swirling energy vortex and dazzling atmospheric lightning"
                val eng = "The hidden power awakens with breathtaking radiance, transforming everything in its path."
                val roman = "Chhupi hui quwwat achanak jaag uthi, aur charon taraf roshni ka toofan phail gaya!"
                val urdu = "چھپی ہوئی قوت اچانک جاگ اٹھی، اور چاروں طرف روشنی کا طوفان پھیل گیا!"
                val dia = listOf(
                    DialogueLine(
                        characterId = p1.id,
                        characterName = p1.name,
                        emotion = "Joyful Excitement",
                        englishText = p1.catchphraseEnglish.ifBlank { "We did it! It really worked!" },
                        romanUrduText = p1.catchphraseRomanUrdu.ifBlank { "Yeh to kamaal ho gaya dosto!" },
                        urduText = p1.catchphraseUrdu.ifBlank { "یہ تو کمال ہو گیا دوستو!" }
                    )
                )
                SceneData(title, cam, set, eng, roman, urdu, dia)
            }
            else -> {
                // Resolution / Climax 2
                val title = "The Triumph and Journey Forward"
                val cam = CameraMovement.STATIC_CLOSE_UP
                val set = "Panoramic golden sunrise vista with rolling clouds and glittering magical dust"
                val eng = "A new legend is written into the stars, as the true journey has only just begun."
                val roman = "Ek nayi dastan shuru hui thi, aur aage ka rasta ab roshan tha."
                val urdu = "ایک نئی داستان شروع ہوئی تھی، اور آگے کا راستہ اب روشن تھا۔"
                val dia = listOf(
                    DialogueLine(
                        characterId = p2.id,
                        characterName = p2.name,
                        emotion = "Inspiring",
                        englishText = "Where shall our next adventure take us?",
                        romanUrduText = "Ab batao, agli manzil kahan hogi?",
                        urduText = "اب بتاؤ، اگلی منزل کہاں ہوگی؟"
                    )
                )
                SceneData(title, cam, set, eng, roman, urdu, dia)
            }
        }

        val baseScene = ScenePrompt(
            id = UUID.randomUUID().toString(),
            sceneNumber = index,
            title = sceneTitle,
            durationSeconds = durationSeconds,
            settingDescription = setting,
            cameraMovement = camera,
            lightingAtmosphere = "Cinematic lighting, vivid highlights, high volumetric detail",
            featuredCharacterIds = characters.map { it.id },
            negativePrompt = ModelPromptSynthesizer.getOptimizedNegativePrompt(genre),
            voiceOverEnglish = voEng,
            voiceOverRomanUrdu = voRoman,
            voiceOverUrdu = voUrdu,
            dialogues = dialogues,
            soundEffectsCue = when (index) {
                1 -> "[Ambient mystical hum, distant wind chime]"
                2 -> "[Mechanical clockwork ticks, soft stone sliding]"
                3 -> "[Thunderous energy surge, harmonic bell chime]"
                else -> "[Gentle breeze, triumphant orchestral crescendo]"
            }
        )

        // Synthesize the model prompt
        val synthesizedPrompt = ModelPromptSynthesizer.synthesizePrompt(
            model = targetModel,
            genre = genre,
            aspectRatio = aspectRatio,
            scene = baseScene,
            characters = characters
        )

        return baseScene.copy(visualPrompt = synthesizedPrompt)
    }

    private data class SceneData(
        val title: String,
        val camera: CameraMovement,
        val setting: String,
        val voEnglish: String,
        val voRomanUrdu: String,
        val voUrdu: String,
        val dialogues: List<DialogueLine>
    )

    /**
     * Preset templates for quick inspiration
     */
    fun getStarterPresets(): List<StudioProject> {
        return listOf(
            generateStory(
                title = "The Cyberpunk Detective",
                premise = "In Neo-Karachi 2099, a neon-lit bounty hunter unravels a sentient AI conspiracy in bustling alleyways.",
                genre = StoryGenre.CYBERPUNK,
                targetModel = VideoModel.GOOGLE_VEO,
                aspectRatio = AspectRatioFormat.PORTRAIT_9_16,
                sceneCount = 3,
                sceneDurationSeconds = 8,
                language = ScriptLanguage.BILINGUAL_MULTILINGUAL
            ),
            generateStory(
                title = "Jungle Friends Adventure",
                premise = "Milo the energetic raccoon and wise Professor Babban uncover an ancient magical fruit in the enchanted forest.",
                genre = StoryGenre.CARTOON_3D,
                targetModel = VideoModel.PIXVERSE,
                aspectRatio = AspectRatioFormat.PORTRAIT_9_16,
                sceneCount = 3,
                sceneDurationSeconds = 8,
                language = ScriptLanguage.ROMAN_URDU
            ),
            generateStory(
                title = "Dastan-e-Kohsar (The Mountain Legend)",
                premise = "An intrepid explorer traverses misty Himalayan peaks in search of the glowing ruby of lore.",
                genre = StoryGenre.CINEMATIC,
                targetModel = VideoModel.KLING_AI,
                aspectRatio = AspectRatioFormat.LANDSCAPE_16_9,
                sceneCount = 4,
                sceneDurationSeconds = 7,
                language = ScriptLanguage.URDU_NASTALIQ
            ),
            generateStory(
                title = "Runway Gen-3 Mech Warriors",
                premise = "Giant stylized mechs duel against a sunset skyline in an explosive cinematic showdown.",
                genre = StoryGenre.ANIME,
                targetModel = VideoModel.RUNWAY_GEN3,
                aspectRatio = AspectRatioFormat.LANDSCAPE_16_9,
                sceneCount = 3,
                sceneDurationSeconds = 8,
                language = ScriptLanguage.ENGLISH_ONLY
            )
        )
    }
}
