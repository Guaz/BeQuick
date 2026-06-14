package com.kitsuneo.bquick.feature.interval

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class IntervalSetupUiState(
    val preparationSeconds: Int = 10,
    val workSeconds: Int = 40,
    val restSeconds: Int = 20,
    val rounds: Int = 8
) {
    val totalDurationSeconds: Int
        get() = preparationSeconds + (workSeconds * rounds) + (restSeconds * (rounds - 1))
}

class IntervalSetupViewModel : ViewModel() {
    private val _state = MutableStateFlow(IntervalSetupUiState())
    val state: StateFlow<IntervalSetupUiState> = _state.asStateFlow()

    fun updatePreparationSeconds(value: Int) {
        _state.update { it.copy(preparationSeconds = value.coerceIn(0, 120)) }
    }

    fun updateWorkSeconds(value: Int) {
        _state.update { it.copy(workSeconds = value.coerceIn(0, 180)) }
    }

    fun updateRestSeconds(value: Int) {
        _state.update { it.copy(restSeconds = value.coerceIn(0, 120)) }
    }

    fun updateRounds(value: Int) {
        _state.update { it.copy(rounds = value.coerceIn(1, 20)) }
    }
}
