package com.kitsuneo.bquick.alarm

import android.content.Context
import com.kitsuneo.bquick.settings.BuiltInSound
import com.kitsuneo.bquick.settings.SoundSelection
import com.kitsuneo.bquick.settings.SoundSelectionCodec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

object AlarmRepository {
    private const val PrefName = "alarm_repository"
    private const val AlarmsKey = "alarms"

    private lateinit var appContext: Context
    private val _alarms = MutableStateFlow<List<AlarmEntry>>(emptyList())
    val alarms: StateFlow<List<AlarmEntry>> = _alarms.asStateFlow()

    fun initialize(context: Context) {
        appContext = context.applicationContext
        _alarms.value = load()
        AlarmScheduler.sync(appContext, _alarms.value)
    }

    fun createAlarm(
        name: String,
        hour: Int,
        minute: Int,
        repeatDays: Set<AlarmWeekday>,
        soundSelection: SoundSelection,
        volumePercent: Int,
        fadeUpEnabled: Boolean,
        vibrateEnabled: Boolean,
        snoozeEnabled: Boolean
    ) {
        val nextId = (_alarms.value.maxOfOrNull { it.id } ?: 0) + 1
        val alarm = AlarmEntry(
            id = nextId,
            name = name.trim(),
            hour = hour.coerceIn(0, 23),
            minute = minute.coerceIn(0, 59),
            repeatDays = repeatDays,
            enabled = true,
            soundSelection = soundSelection,
            volumePercent = volumePercent.coerceIn(0, 100),
            fadeUpEnabled = fadeUpEnabled,
            vibrateEnabled = vibrateEnabled,
            snoozeEnabled = snoozeEnabled
        )
        persist(_alarms.value + alarm)
    }

    fun updateAlarm(updatedAlarm: AlarmEntry) {
        persist(
            _alarms.value.map { alarm ->
                if (alarm.id == updatedAlarm.id) {
                    updatedAlarm.copy(
                        hour = updatedAlarm.hour.coerceIn(0, 23),
                        minute = updatedAlarm.minute.coerceIn(0, 59),
                        volumePercent = updatedAlarm.volumePercent.coerceIn(0, 100)
                    )
                } else {
                    alarm
                }
            }
        )
    }

    fun toggleAlarm(id: Int) {
        persist(
            _alarms.value.map { alarm ->
                if (alarm.id == id) alarm.copy(enabled = !alarm.enabled) else alarm
            }
        )
    }

    fun disableAlarm(id: Int) {
        persist(
            _alarms.value.map { alarm ->
                if (alarm.id == id) alarm.copy(enabled = false) else alarm
            }
        )
    }

    fun deleteAlarm(id: Int) {
        if (!::appContext.isInitialized) return
        AlarmScheduler.cancel(appContext, id)
        persist(_alarms.value.filterNot { it.id == id })
    }

    fun findAlarm(id: Int): AlarmEntry? {
        if (_alarms.value.isEmpty() && ::appContext.isInitialized) {
            _alarms.value = load()
        }
        return _alarms.value.firstOrNull { it.id == id }
    }

    private fun persist(alarms: List<AlarmEntry>) {
        if (!::appContext.isInitialized) return
        _alarms.value = alarms.sortedWith(compareBy(AlarmEntry::hour, AlarmEntry::minute, AlarmEntry::id))
        val encoded = JSONArray().apply {
            _alarms.value.forEach { alarm ->
                put(
                    JSONObject().apply {
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
                    }
                )
            }
        }.toString()
        appContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE)
            .edit()
            .putString(AlarmsKey, encoded)
            .apply()
        AlarmScheduler.sync(appContext, _alarms.value)
    }

    private fun load(): List<AlarmEntry> {
        if (!::appContext.isInitialized) return emptyList()
        val encoded = appContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE)
            .getString(AlarmsKey, null)
            ?: return emptyList()
        val json = JSONArray(encoded)
        return buildList {
            for (index in 0 until json.length()) {
                val item = json.getJSONObject(index)
                val repeatDays = buildSet {
                    val repeatJson = item.optJSONArray("repeatDays") ?: JSONArray()
                    for (dayIndex in 0 until repeatJson.length()) {
                        AlarmWeekday.entries.firstOrNull { it.name == repeatJson.getString(dayIndex) }?.let(::add)
                    }
                }
                add(
                    AlarmEntry(
                        id = item.getInt("id"),
                        name = item.optString("name", ""),
                        hour = item.getInt("hour"),
                        minute = item.getInt("minute"),
                        repeatDays = repeatDays,
                        enabled = item.getBoolean("enabled"),
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
                )
            }
        }.sortedWith(compareBy(AlarmEntry::hour, AlarmEntry::minute, AlarmEntry::id))
    }

    private fun encodeSoundSelection(selection: SoundSelection): String = SoundSelectionCodec.encode(selection)
}
