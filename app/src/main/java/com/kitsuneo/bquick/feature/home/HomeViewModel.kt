package com.kitsuneo.bquick.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitsuneo.bquick.settings.BuiltInSound
import com.kitsuneo.bquick.settings.SoundSettingsRepository
import com.kitsuneo.bquick.settings.SoundTarget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeFeatureCard(
    val title: String,
    val eyebrow: String,
    val description: String,
    val primaryAction: String
)

data class HomeUiState(
    val headline: String = "Fast focus tools",
    val subheadline: String = "A Compose rewrite of the BFast concept with two training modes.",
    val modeSwitchSoundLabel: String = "Pulse",
    val reactionSoundLabel: String = "Bell",
    val features: List<HomeFeatureCard> = listOf(
        HomeFeatureCard(
            title = "Interval",
            eyebrow = "Structured timer",
            description = "Build work and rest rounds, then run the session with a live countdown.",
            primaryAction = "Open interval"
        ),
        HomeFeatureCard(
            title = "Random Sound Generator",
            eyebrow = "Cue drill",
            description = "Trigger short random beeps inside a timed session for reaction practice.",
            primaryAction = "Open random sound"
        )
    )
)

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            SoundSettingsRepository.settings.collect { settings ->
                _state.value = _state.value.copy(
                    modeSwitchSoundLabel = settings.modeSwitch.label,
                    reactionSoundLabel = settings.reaction.label
                )
            }
        }
    }

    fun selectBuiltInSound(target: SoundTarget, sound: BuiltInSound) {
        SoundSettingsRepository.updateBuiltIn(target, sound)
    }

    fun selectCustomSound(target: SoundTarget, uri: String, label: String) {
        SoundSettingsRepository.updateCustom(target, uri, label)
    }
}
