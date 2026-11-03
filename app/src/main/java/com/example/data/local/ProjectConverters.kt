package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.CameraMovement
import com.example.data.model.CharacterProfile
import com.example.data.model.DialogueLine
import com.example.data.model.ScenePrompt
import org.json.JSONArray
import org.json.JSONObject

class ProjectConverters {

    @TypeConverter
    fun fromCharactersList(characters: List<CharacterProfile>?): String {
        if (characters.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        for (c in characters) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("role", c.role)
            obj.put("appearancePromptTag", c.appearancePromptTag)
            obj.put("personality", c.personality)
            obj.put("voiceTone", c.voiceTone)
            obj.put("catchphraseEnglish", c.catchphraseEnglish)
            obj.put("catchphraseRomanUrdu", c.catchphraseRomanUrdu)
            obj.put("catchphraseUrdu", c.catchphraseUrdu)
            obj.put("avatarColorHex", c.avatarColorHex)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toCharactersList(json: String?): List<CharacterProfile> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<CharacterProfile>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CharacterProfile(
                        id = obj.optString("id"),
                        name = obj.optString("name"),
                        role = obj.optString("role", "Protagonist"),
                        appearancePromptTag = obj.optString("appearancePromptTag"),
                        personality = obj.optString("personality"),
                        voiceTone = obj.optString("voiceTone"),
                        catchphraseEnglish = obj.optString("catchphraseEnglish"),
                        catchphraseRomanUrdu = obj.optString("catchphraseRomanUrdu"),
                        catchphraseUrdu = obj.optString("catchphraseUrdu"),
                        avatarColorHex = obj.optLong("avatarColorHex", 0xFF8B5CF6)
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    @TypeConverter
    fun fromScenesList(scenes: List<ScenePrompt>?): String {
        if (scenes.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        for (s in scenes) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("sceneNumber", s.sceneNumber)
            obj.put("title", s.title)
            obj.put("durationSeconds", s.durationSeconds)
            obj.put("settingDescription", s.settingDescription)
            obj.put("cameraMovement", s.cameraMovement.name)
            obj.put("lightingAtmosphere", s.lightingAtmosphere)
            obj.put("visualPrompt", s.visualPrompt)
            obj.put("negativePrompt", s.negativePrompt)
            obj.put("voiceOverEnglish", s.voiceOverEnglish)
            obj.put("voiceOverRomanUrdu", s.voiceOverRomanUrdu)
            obj.put("voiceOverUrdu", s.voiceOverUrdu)
            obj.put("soundEffectsCue", s.soundEffectsCue)

            val featChars = JSONArray()
            s.featuredCharacterIds.forEach { featChars.put(it) }
            obj.put("featuredCharacterIds", featChars)

            val dialoguesArray = JSONArray()
            for (d in s.dialogues) {
                val dObj = JSONObject()
                dObj.put("id", d.id)
                dObj.put("characterId", d.characterId)
                dObj.put("characterName", d.characterName)
                dObj.put("emotion", d.emotion)
                dObj.put("englishText", d.englishText)
                dObj.put("romanUrduText", d.romanUrduText)
                dObj.put("urduText", d.urduText)
                dialoguesArray.put(dObj)
            }
            obj.put("dialogues", dialoguesArray)

            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toScenesList(json: String?): List<ScenePrompt> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<ScenePrompt>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val featChars = mutableListOf<String>()
                val featJson = obj.optJSONArray("featuredCharacterIds")
                if (featJson != null) {
                    for (k in 0 until featJson.length()) {
                        featChars.add(featJson.getString(k))
                    }
                }

                val dialogues = mutableListOf<DialogueLine>()
                val diaJson = obj.optJSONArray("dialogues")
                if (diaJson != null) {
                    for (k in 0 until diaJson.length()) {
                        val dObj = diaJson.getJSONObject(k)
                        dialogues.add(
                            DialogueLine(
                                id = dObj.optString("id"),
                                characterId = dObj.optString("characterId"),
                                characterName = dObj.optString("characterName"),
                                emotion = dObj.optString("emotion", "Neutral"),
                                englishText = dObj.optString("englishText"),
                                romanUrduText = dObj.optString("romanUrduText"),
                                urduText = dObj.optString("urduText")
                            )
                        )
                    }
                }

                val camName = obj.optString("cameraMovement", CameraMovement.SLOW_ZOOM_IN.name)
                val camMovement = try { CameraMovement.valueOf(camName) } catch (_: Exception) { CameraMovement.SLOW_ZOOM_IN }

                list.add(
                    ScenePrompt(
                        id = obj.optString("id"),
                        sceneNumber = obj.optInt("sceneNumber", i + 1),
                        title = obj.optString("title"),
                        durationSeconds = obj.optInt("durationSeconds", 8).coerceIn(6, 10),
                        settingDescription = obj.optString("settingDescription"),
                        cameraMovement = camMovement,
                        lightingAtmosphere = obj.optString("lightingAtmosphere"),
                        featuredCharacterIds = featChars,
                        visualPrompt = obj.optString("visualPrompt"),
                        negativePrompt = obj.optString("negativePrompt"),
                        voiceOverEnglish = obj.optString("voiceOverEnglish"),
                        voiceOverRomanUrdu = obj.optString("voiceOverRomanUrdu"),
                        voiceOverUrdu = obj.optString("voiceOverUrdu"),
                        dialogues = dialogues,
                        soundEffectsCue = obj.optString("soundEffectsCue")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    @TypeConverter
    fun fromTagsList(tags: List<String>?): String {
        if (tags.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        tags.forEach { array.put(it) }
        return array.toString()
    }

    @TypeConverter
    fun toTagsList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<String>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
        } catch (_: Exception) {}
        return list
    }
}
