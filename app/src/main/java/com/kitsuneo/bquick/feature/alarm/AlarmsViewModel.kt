package com.kitsuneo.bquick.feature.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitsuneo.bquick.alarm.AlarmEntry
import com.kitsuneo.bquick.alarm.AlarmRepository
import com.kitsuneo.bquick.alarm.AlarmScheduler
import com.kitsuneo.bquick.alarm.AlarmTriggerDelta
import com.kitsuneo.bquick.alarm.AlarmWeekday
import com.kitsuneo.bquick.alarm.nextTriggerDelta
import com.kitsuneo.bquick.settings.AlarmTimeFormat
import com.kitsuneo.bquick.settings.BuiltInSound
import com.kitsuneo.bquick.settings.SoundSelection
import com.kitsuneo.bquick.settings.SoundSettingsRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class AlarmDraftUiState(
    val editingAlarmId: Int? = null,
    val hour: Int = 7,
    val minute: Int = 0,
    val repeatDays: Set<AlarmWeekday> = emptySet(),
    val soundSelection: SoundSelection = SoundSelection.BuiltIn(BuiltInSound.Bell),
    val volumePercent: Int = 100,
    val fadeUpEnabled: Boolean = false,
    val vibrateEnabled: Boolean = true,
    val snoozeEnabled: Boolean = true,
    val name: String = ""
) {
    val timeText: String
        get() = "%02d:%02d".format(hour, minute)

    fun triggerDelta(nowMillis: Long = System.currentTimeMillis()): AlarmTriggerDelta {
        return AlarmEntry(
            id = -1,
            name = name,
            hour = hour,
            minute = minute,
            repeatDays = repeatDays,
            enabled = true,
            soundSelection = soundSelection,
            volumePercent = volumePercent,
            fadeUpEnabled = fadeUpEnabled,
            vibrateEnabled = vibrateEnabled,
            snoozeEnabled = snoozeEnabled
        ).nextTriggerDelta(nowMillis)
    }
}

data class AlarmsUiState(
    val alarms: List<AlarmEntry> = emptyList(),
    val alarmTimeFormat: AlarmTimeFormat = AlarmTimeFormat.Hours24,
    val draft: AlarmDraftUiState = AlarmDraftUiState()
) {
    val enabledCount: Int
        get() = alarms.count { it.enabled }
}

class AlarmsViewModel : ViewModel() {
    private val _state = MutableStateFlow(AlarmsUiState())
    val state: StateFlow<AlarmsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                AlarmRepository.alarms,
                SoundSettingsRepository.settings
            ) { alarms, settings ->
                alarms to settings.alarmTimeFormat
            }.collectLatest { (alarms, alarmTimeFormat) ->
                _state.value = _state.value.copy(
                    alarms = alarms,
                    alarmTimeFormat = alarmTimeFormat
                )
            }
        }
    }

    fun updateDraftHour(value: Int) {
        _state.value = _state.value.copy(
            draft = _state.value.draft.copy(hour = value.coerceIn(0, 23))
        )
    }

    fun updateDraftMinute(value: Int) {
        _state.value = _state.value.copy(
            draft = _state.value.draft.copy(minute = value.coerceIn(0, 59))
        )
    }

    fun toggleDraftWeekday(day: AlarmWeekday) {
        val repeatDays = _state.value.draft.repeatDays.toMutableSet().apply {
            if (!add(day)) remove(day)
        }
        _state.value = _state.value.copy(
            draft = _state.value.draft.copy(repeatDays = repeatDays)
        )
    }

    fun selectBuiltInSound(sound: BuiltInSound) {
        _state.value = _state.value.copy(
            draft = _state.value.draft.copy(soundSelection = SoundSelection.BuiltIn(sound))
        )
    }

    fun selectCustomSound(uri: String, label: String) {
        _state.value = _state.value.copy(
            draft = _state.value.draft.copy(soundSelection = SoundSelection.Custom(uri, label))
        )
    }

    fun updateVolumePercent(value: Int) {
        _state.value = _state.value.copy(
            draft = _state.value.draft.copy(volumePercent = value.coerceIn(0, 100))
        )
    }

    fun updateFadeUpEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(
            draft = _state.value.draft.copy(fadeUpEnabled = enabled)
        )
    }

    fun updateVibrateEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(
            draft = _state.value.draft.copy(vibrateEnabled = enabled)
        )
    }

    fun updateSnoozeEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(
            draft = _state.value.draft.copy(snoozeEnabled = enabled)
        )
    }

    fun updateName(name: String) {
        _state.value = _state.value.copy(
            draft = _state.value.draft.copy(name = name.take(40))
        )
    }

    fun loadAlarmForEdit(alarmId: Int) {
        val alarm = AlarmRepository.findAlarm(alarmId) ?: return
        if (_state.value.draft.editingAlarmId == alarmId) return
        _state.value = _state.value.copy(
            draft = AlarmDraftUiState(
                editingAlarmId = alarm.id,
                hour = alarm.hour,
                minute = alarm.minute,
                repeatDays = alarm.repeatDays,
                soundSelection = alarm.soundSelection,
                volumePercent = alarm.volumePercent,
                fadeUpEnabled = alarm.fadeUpEnabled,
                vibrateEnabled = alarm.vibrateEnabled,
                snoozeEnabled = alarm.snoozeEnabled,
                name = alarm.name
            )
        )
    }

    fun resetDraftForNewAlarm() {
        if (_state.value.draft.editingAlarmId == null) return
        resetDraft()
    }

    fun saveAlarm(): AlarmEntry? {
        val draft = _state.value.draft
        val editingAlarmId = draft.editingAlarmId
        val alarmForToast: AlarmEntry
        if (editingAlarmId == null) {
            alarmForToast = AlarmEntry(
                id = -1,
                name = draft.name.trim(),
                hour = draft.hour,
                minute = draft.minute,
                repeatDays = draft.repeatDays,
                enabled = true,
                soundSelection = draft.soundSelection,
                volumePercent = draft.volumePercent,
                fadeUpEnabled = draft.fadeUpEnabled,
                vibrateEnabled = draft.vibrateEnabled,
                snoozeEnabled = draft.snoozeEnabled
            )
            AlarmRepository.createAlarm(
                name = draft.name,
                hour = draft.hour,
                minute = draft.minute,
                repeatDays = draft.repeatDays,
                soundSelection = draft.soundSelection,
                volumePercent = draft.volumePercent,
                fadeUpEnabled = draft.fadeUpEnabled,
                vibrateEnabled = draft.vibrateEnabled,
                snoozeEnabled = draft.snoozeEnabled
            )
        } else {
            val existingAlarm = AlarmRepository.findAlarm(editingAlarmId) ?: return null
            alarmForToast = existingAlarm.copy(
                name = draft.name.trim(),
                hour = draft.hour,
                minute = draft.minute,
                repeatDays = draft.repeatDays,
                soundSelection = draft.soundSelection,
                volumePercent = draft.volumePercent,
                fadeUpEnabled = draft.fadeUpEnabled,
                vibrateEnabled = draft.vibrateEnabled,
                snoozeEnabled = draft.snoozeEnabled
            )
            AlarmRepository.updateAlarm(alarmForToast)
        }
        resetDraft()
        return alarmForToast.takeIf { it.enabled }
    }

    fun toggleAlarm(id: Int): AlarmEntry? {
        val alarm = AlarmRepository.findAlarm(id) ?: return null
        val toggledAlarm = alarm.copy(enabled = !alarm.enabled)
        AlarmRepository.toggleAlarm(id)
        return toggledAlarm.takeIf { it.enabled }
    }

    fun deleteAlarm() {
        val editingAlarmId = _state.value.draft.editingAlarmId ?: return
        AlarmRepository.deleteAlarm(editingAlarmId)
        resetDraft()
    }

    private fun resetDraft() {
        _state.value = _state.value.copy(draft = AlarmDraftUiState())
    }
}
