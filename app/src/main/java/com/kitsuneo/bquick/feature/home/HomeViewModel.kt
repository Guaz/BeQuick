package com.kitsuneo.bquick.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitsuneo.bquick.alarm.AlarmRepository
import com.kitsuneo.bquick.settings.AlarmTimeFormat
import com.kitsuneo.bquick.settings.AppLanguage
import com.kitsuneo.bquick.settings.BuiltInSound
import com.kitsuneo.bquick.settings.SoundSelection
import com.kitsuneo.bquick.settings.SoundSettingsRepository
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
    val modeSwitchSound: SoundSelection = SoundSelection.BuiltIn(BuiltInSound.SmoothMorning),
    val reactionSound: SoundSelection = SoundSelection.BuiltIn(BuiltInSound.TimeToShine),
    val homeOrder: List<HomeDestination> = HomeDestination.entries,
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
                    homeOrder = settings.homeOrder,
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

    fun updateAlarmTimeFormat(format: AlarmTimeFormat) {
        SoundSettingsRepository.updateAlarmTimeFormat(format)
    }

    fun updateHomeOrder(order: List<HomeDestination>) {
        SoundSettingsRepository.updateHomeOrder(order)
    }

    fun updateAppLanguage(language: AppLanguage) {
        SoundSettingsRepository.updateAppLanguage(language)
    }
}
