package com.kitsuneo.bquick.feature.timer

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TimerSetupUiState(
    val durationSeconds: Int = 5 * 60
)

class TimerSetupViewModel : ViewModel() {
    private val _state = MutableStateFlow(TimerSetupUiState())
    val state: StateFlow<TimerSetupUiState> = _state.asStateFlow()

    fun updateDurationSeconds(value: Int) {
        _state.update { it.copy(durationSeconds = value.coerceIn(1, 59 * 60 + 59)) }
    }
}
