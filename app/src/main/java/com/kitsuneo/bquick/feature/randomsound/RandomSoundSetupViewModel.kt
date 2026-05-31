package com.kitsuneo.bquick.feature.randomsound

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RandomSoundSetupUiState(
    val durationMinutes: Int = 5,
    val minGapSeconds: Int = 15,
    val maxGapSeconds: Int = 45
) {
    val totalSessionSeconds: Int
        get() = durationMinutes * 60
}

class RandomSoundSetupViewModel : ViewModel() {
    private val _state = MutableStateFlow(RandomSoundSetupUiState())
    val state: StateFlow<RandomSoundSetupUiState> = _state.asStateFlow()

    fun updateDurationMinutes(value: Int) {
        _state.update { it.copy(durationMinutes = value.coerceIn(1, 30)) }
    }

    fun updateMinGapSeconds(value: Int) {
        _state.update { current ->
            val minGapSeconds = value.coerceIn(3, 120)
            current.copy(
                minGapSeconds = minGapSeconds,
                maxGapSeconds = current.maxGapSeconds.coerceAtLeast(minGapSeconds)
            )
        }
    }

    fun updateMaxGapSeconds(value: Int) {
        _state.update { current ->
            val maxGapSeconds = value.coerceIn(3, 180).coerceAtLeast(current.minGapSeconds)
            current.copy(maxGapSeconds = maxGapSeconds)
        }
    }
}
