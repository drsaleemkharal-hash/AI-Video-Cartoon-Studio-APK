package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.local.ProjectConverters
import com.example.data.model.StudioProject
import org.json.JSONObject

object ExportHelper {

    /**
     * Formats the project into a comprehensive screenplay production script in TXT format,
     * including timestamps, camera directions, English/Roman Urdu/Urdu dialogues, and video model prompts.
     */
    fun generateProductionScriptTxt(project: StudioProject): String {
        return buildString {
            appendLine("================================================================================")
            appendLine("                   AI VIDEO & CARTOON STUDIO - PRODUCTION SCRIPT                ")
            appendLine("================================================================================")
            appendLine("TITLE:           ${project.title}")
            appendLine("GENRE:           ${project.genre.displayName}")
            appendLine("TARGET MODEL:    ${project.targetModel.displayName} (${project.targetModel.vendor})")
            appendLine("ASPECT RATIO:    ${project.aspectRatio.ratioString} (${project.aspectRatio.platformTarget})")
            appendLine("TOTAL SCENES:    ${project.scenes.size} scenes (~${project.scenes.sumOf { it.durationSeconds }} seconds total)")
            appendLine("SCRIPT LANGUAGE: ${project.language.displayName}")
            appendLine("================================================================================")
            appendLine()
            appendLine("SYNOPSIS:")
            appendLine(project.synopsis)
            appendLine()
            appendLine("--------------------------------------------------------------------------------")
            appendLine("CHARACTERS & VISUAL CONSISTENCY ROSTER:")
            appendLine("--------------------------------------------------------------------------------")
            if (project.characters.isEmpty()) {
                appendLine("(No characters registered)")
            } else {
                project.characters.forEachIndexed { idx, char ->
                    appendLine("[Character ${idx + 1}] ${char.name.uppercase()} (${char.role})")
                    appendLine("  Visual Tag:   ${char.appearancePromptTag}")
                    appendLine("  Personality:  ${char.personality}")
                    appendLine("  Voice Tone:   ${char.voiceTone}")
                    if (char.catchphraseEnglish.isNotBlank()) {
                        appendLine("  English Line: \"${char.catchphraseEnglish}\"")
                    }
                    if (char.catchphraseRomanUrdu.isNotBlank()) {
                        appendLine("  Roman Urdu:   \"${char.catchphraseRomanUrdu}\"")
                    }
                    if (char.catchphraseUrdu.isNotBlank()) {
                        appendLine("  Urdu Script:  \"${char.catchphraseUrdu}\"")
                    }
                    appendLine()
                }
            }

            appendLine("================================================================================")
            appendLine("SCENE-BY-SCENE PRODUCTION BLUEPRINTS:")
            appendLine("================================================================================")

            project.scenes.forEach { scene ->
                appendLine(">>> SCENE ${scene.sceneNumber}: ${scene.title.uppercase()} [Duration: ${scene.durationSeconds}s | ${scene.cameraMovement.label}]")
                appendLine("SETTING:   ${scene.settingDescription}")
                appendLine("LIGHTING:  ${scene.lightingAtmosphere}")
                if (scene.soundEffectsCue.isNotBlank()) {
                    appendLine("SFX CUE:   ${scene.soundEffectsCue}")
                }
                appendLine()

                appendLine("--- AI VIDEO MODEL PROMPT (${project.targetModel.displayName}) ---")
                appendLine(scene.visualPrompt)
                appendLine()

                if (scene.negativePrompt.isNotBlank()) {
                    appendLine("--- NEGATIVE PROMPT ---")
                    appendLine(scene.negativePrompt)
                    appendLine()
                }

                if (scene.voiceOverEnglish.isNotBlank() || scene.voiceOverRomanUrdu.isNotBlank() || scene.voiceOverUrdu.isNotBlank()) {
                    appendLine("--- VOICE-OVER NARRATION ---")
                    if (scene.voiceOverEnglish.isNotBlank()) {
                        appendLine("[VO English]    ${scene.voiceOverEnglish}")
                    }
                    if (scene.voiceOverRomanUrdu.isNotBlank()) {
                        appendLine("[VO Roman Urdu] ${scene.voiceOverRomanUrdu}")
                    }
                    if (scene.voiceOverUrdu.isNotBlank()) {
                        appendLine("[VO Urdu]       ${scene.voiceOverUrdu}")
                    }
                    appendLine()
                }

                if (scene.dialogues.isNotEmpty()) {
                    appendLine("--- DIALOGUE ---")
                    scene.dialogues.forEach { d ->
                        appendLine("${d.characterName.uppercase()} (${d.emotion}):")
                        if (d.englishText.isNotBlank()) {
                            appendLine("  English:    ${d.englishText}")
                        }
                        if (d.romanUrduText.isNotBlank()) {
                            appendLine("  Roman Urdu: ${d.romanUrduText}")
                        }
                        if (d.urduText.isNotBlank()) {
                            appendLine("  Urdu:       ${d.urduText}")
                        }
                    }
                    appendLine()
                }

                appendLine("--------------------------------------------------------------------------------")
            }

            appendLine("Generated by AI Video & Cartoon Studio")
            appendLine("Ready for copy-paste into Google Veo, Kling AI, Seedance, Runway, PixVerse & Hailuo.")
        }
    }

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    /**
     * Serializes the project into a formatted JSON string.
     */
    fun generateProjectJson(project: StudioProject): String {
        val charEntries = project.characters.joinToString(",\n    ") { c ->
            """{"id":"${c.id}","name":"${escapeJson(c.name)}","role":"${escapeJson(c.role)}","appearanceTag":"${escapeJson(c.appearancePromptTag)}","personality":"${escapeJson(c.personality)}","voiceTone":"${escapeJson(c.voiceTone)}","catchphraseEnglish":"${escapeJson(c.catchphraseEnglish)}","catchphraseRomanUrdu":"${escapeJson(c.catchphraseRomanUrdu)}","catchphraseUrdu":"${escapeJson(c.catchphraseUrdu)}"}"""
        }
        val sceneEntries = project.scenes.joinToString(",\n    ") { s ->
            val dialogues = s.dialogues.joinToString(",") { d ->
                """{"speaker":"${escapeJson(d.characterName)}","emotion":"${escapeJson(d.emotion)}","english":"${escapeJson(d.englishText)}","romanUrdu":"${escapeJson(d.romanUrduText)}","urdu":"${escapeJson(d.urduText)}"}"""
            }
            """{"sceneNumber":${s.sceneNumber},"title":"${escapeJson(s.title)}","durationSeconds":${s.durationSeconds},"setting":"${escapeJson(s.settingDescription)}","cameraMovement":"${s.cameraMovement.name}","visualPrompt":"${escapeJson(s.visualPrompt)}","negativePrompt":"${escapeJson(s.negativePrompt)}","voEnglish":"${escapeJson(s.voiceOverEnglish)}","voRomanUrdu":"${escapeJson(s.voiceOverRomanUrdu)}","voUrdu":"${escapeJson(s.voiceOverUrdu)}","dialogues":[$dialogues]}"""
        }
        return """{
  "id": "${project.id}",
  "title": "${escapeJson(project.title)}",
  "synopsis": "${escapeJson(project.synopsis)}",
  "genre": "${project.genre.name}",
  "targetModel": "${project.targetModel.name}",
  "aspectRatio": "${project.aspectRatio.ratioString}",
  "defaultSceneDurationSeconds": ${project.defaultSceneDurationSeconds},
  "language": "${project.language.name}",
  "createdAt": ${project.createdAt},
  "updatedAt": ${project.updatedAt},
  "characters": [
    $charEntries
  ],
  "scenes": [
    $sceneEntries
  ]
}"""
    }

    /**
     * Extracts all visual prompts into a concise, numbered list ready for sequential generation.
     */
    fun generateAllVisualPrompts(project: StudioProject): String {
        return buildString {
            appendLine("# ${project.title} - Visual Prompts for ${project.targetModel.displayName}")
            appendLine("# Aspect Ratio: ${project.aspectRatio.ratioString}")
            appendLine()
            project.scenes.forEach { scene ->
                appendLine("=== SCENE ${scene.sceneNumber}: ${scene.title} (${scene.durationSeconds}s) ===")
                appendLine(scene.visualPrompt)
                appendLine()
            }
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Prompt") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    fun shareText(context: Context, text: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, "Share $title")
        context.startActivity(chooser)
    }
}
