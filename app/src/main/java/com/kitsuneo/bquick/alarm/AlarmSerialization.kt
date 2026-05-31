package com.kitsuneo.bquick.alarm

import com.kitsuneo.bquick.settings.BuiltInSound
import com.kitsuneo.bquick.settings.SoundSelection
import org.json.JSONArray
import org.json.JSONObject

object AlarmSerialization {
    fun toJson(alarm: AlarmEntry): String {
        return JSONObject().apply {
            put("id", alarm.id)
            put("name", alarm.name)
            put("hour", alarm.hour)
            put("minute", alarm.minute)
            put("enabled", alarm.enabled)
            put("repeatDays", JSONArray(alarm.repeatDays.map { it.name }))
            put("soundSelection", encodeSoundSelection(alarm.soundSelection))
            put("soundLabel", alarm.soundSelection.label)
            put("volumePercent", alarm.volumePercent)
            put("fadeUpEnabled", alarm.fadeUpEnabled)
            put("vibrateEnabled", alarm.vibrateEnabled)
            put("snoozeEnabled", alarm.snoozeEnabled)
        }.toString()
    }

    fun fromJson(json: String): AlarmEntry {
        val item = JSONObject(json)
        val repeatDays = buildSet {
            val repeatJson = item.optJSONArray("repeatDays") ?: JSONArray()
            for (dayIndex in 0 until repeatJson.length()) {
                AlarmWeekday.entries.firstOrNull { it.name == repeatJson.getString(dayIndex) }?.let(::add)
            }
        }
        return AlarmEntry(
            id = item.getInt("id"),
            name = item.optString("name", ""),
            hour = item.getInt("hour"),
            minute = item.getInt("minute"),
            repeatDays = repeatDays,
            enabled = item.optBoolean("enabled", true),
            soundSelection = decodeSoundSelection(
                item.optString("soundSelection"),
                item.optString("soundLabel", "Bell")
            ),
            volumePercent = item.optInt("volumePercent", 100),
            fadeUpEnabled = item.optBoolean("fadeUpEnabled", false),
            vibrateEnabled = item.optBoolean("vibrateEnabled", true),
            snoozeEnabled = item.optBoolean("snoozeEnabled", true)
        )
    }

    private fun encodeSoundSelection(selection: SoundSelection): String = when (selection) {
        is SoundSelection.BuiltIn -> "builtin:${selection.sound.id}"
        is SoundSelection.Custom -> "custom:${selection.uri}"
    }

    private fun decodeSoundSelection(encoded: String?, label: String): SoundSelection {
        if (encoded.isNullOrBlank()) return SoundSelection.BuiltIn(BuiltInSound.Bell)
        return when {
            encoded.startsWith("builtin:") -> {
                val id = encoded.substringAfter(':')
                val sound = BuiltInSound.entries.firstOrNull { it.id == id } ?: BuiltInSound.Bell
                SoundSelection.BuiltIn(sound)
            }

            encoded.startsWith("custom:") -> SoundSelection.Custom(encoded.substringAfter(':'), label)
            else -> SoundSelection.BuiltIn(BuiltInSound.Bell)
        }
    }
}
