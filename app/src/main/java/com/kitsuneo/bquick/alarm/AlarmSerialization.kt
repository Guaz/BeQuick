package com.kitsuneo.bquick.alarm

import com.kitsuneo.bquick.settings.BuiltInSound
import com.kitsuneo.bquick.settings.SoundSelection
import com.kitsuneo.bquick.settings.SoundSelectionCodec
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
            soundSelection = SoundSelectionCodec.decode(
                encoded = item.optString("soundSelection"),
                customLabel = item.optString("soundLabel", "Wake Up Anthem"),
                fallback = SoundSelection.BuiltIn(BuiltInSound.WakeUpAnthem)
            ),
            volumePercent = item.optInt("volumePercent", 100),
            fadeUpEnabled = item.optBoolean("fadeUpEnabled", false),
            vibrateEnabled = item.optBoolean("vibrateEnabled", true),
            snoozeEnabled = item.optBoolean("snoozeEnabled", true)
        )
    }

    private fun encodeSoundSelection(selection: SoundSelection): String = SoundSelectionCodec.encode(selection)
}
