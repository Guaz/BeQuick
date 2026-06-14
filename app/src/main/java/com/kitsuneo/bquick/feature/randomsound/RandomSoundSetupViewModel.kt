package com.kitsuneo.bquick.feature.randomsound

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RandomSoundSetupUiState(
    val preparationSeconds: Int = 10,
    val durationSeconds: Int = 5 * 60,
    val minGapSeconds: Int = 15,
    val maxGapSeconds: Int = 45
) {
    val totalSessionSeconds: Int
        get() = preparationSeconds + durationSeconds
}

class RandomSoundSetupViewModel : ViewModel() {
    private val _state = MutableStateFlow(RandomSoundSetupUiState())
    val state: StateFlow<RandomSoundSetupUiState> = _state.asStateFlow()

    fun updatePreparationSeconds(value: Int) {
        _state.update { it.copy(preparationSeconds = value.coerceIn(0, 120)) }
    }

    fun updateDurationSeconds(value: Int) {
        _state.update { it.copy(durationSeconds = value.coerceIn(10, 59 * 60 + 59)) }
    }

    fun updateMinGapSeconds(value: Int) {
        _state.update { current ->
            val minGapSeconds = value.coerceIn(1, 59 * 60 + 59)
            current.copy(
                minGapSeconds = minGapSeconds,
                maxGapSeconds = current.maxGapSeconds.coerceAtLeast(minGapSeconds)
            )
        }
    }

    fun updateMaxGapSeconds(value: Int) {
        _state.update { current ->
            val maxGapSeconds = value.coerceIn(1, 59 * 60 + 59).coerceAtLeast(current.minGapSeconds)
            current.copy(maxGapSeconds = maxGapSeconds)
        }
    }
}
