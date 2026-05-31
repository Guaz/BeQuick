package com.kitsuneo.bquick.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitsuneo.bquick.alarm.AlarmRepository
import com.kitsuneo.bquick.settings.AlarmTimeFormat
import com.kitsuneo.bquick.settings.AppLanguage
import com.kitsuneo.bquick.settings.BuiltInSound
import com.kitsuneo.bquick.settings.SoundSelection
import com.kitsuneo.bquick.settings.SoundSettingsRepository
import com.kitsuneo.bquick.settings.SoundTarget
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class HomeDestination {
    Interval,
    RandomSound,
    Alarms
}

data class HomeUiState(
    val modeSwitchSound: SoundSelection = SoundSelection.BuiltIn(BuiltInSound.Pulse),
    val reactionSound: SoundSelection = SoundSelection.BuiltIn(BuiltInSound.Bell),
    val alarmTimeFormat: AlarmTimeFormat = AlarmTimeFormat.Hours24,
    val appLanguage: AppLanguage = AppLanguage.English,
    val enabledAlarmCount: Int = 0,
    val totalAlarmCount: Int = 0
)

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                SoundSettingsRepository.settings,
                AlarmRepository.alarms
            ) { settings, alarms ->
                HomeUiState(
                    modeSwitchSound = settings.modeSwitch,
                    reactionSound = settings.reaction,
                    alarmTimeFormat = settings.alarmTimeFormat,
                    appLanguage = settings.appLanguage,
                    enabledAlarmCount = alarms.count { it.enabled },
                    totalAlarmCount = alarms.size
                )
            }.collect { updatedState ->
                _state.value = updatedState
            }
        }
    }

    fun selectBuiltInSound(target: SoundTarget, sound: BuiltInSound) {
        SoundSettingsRepository.updateBuiltIn(target, sound)
    }

    fun selectCustomSound(target: SoundTarget, uri: String, label: String) {
        SoundSettingsRepository.updateCustom(target, uri, label)
    }

    fun updateAlarmTimeFormat(format: AlarmTimeFormat) {
        SoundSettingsRepository.updateAlarmTimeFormat(format)
    }

    fun updateAppLanguage(language: AppLanguage) {
        SoundSettingsRepository.updateAppLanguage(language)
    }
}
