package com.kitsuneo.bquick.feature.stopwatch

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitsuneo.bquick.timer.ActiveTimerSession
import com.kitsuneo.bquick.timer.TimerForegroundService
import com.kitsuneo.bquick.timer.TimerSessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class StopwatchRunningUiState(
    val elapsedMillis: Long = 0L,
    val startedAtMillis: Long? = null,
    val isRunning: Boolean = false,
    val hasActiveSession: Boolean = false
)

class StopwatchRunningViewModel : ViewModel() {
    private val initialState = StopwatchRunningUiState()

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<StopwatchRunningUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            TimerSessionStore.activeSession.collectLatest { session ->
                val stopwatchSession = session as? ActiveTimerSession.Stopwatch
                _state.value = stopwatchSession?.toUiState() ?: initialState
            }
        }
    }

    fun primaryAction(context: Context) {
        if (_state.value.hasActiveSession) {
            TimerForegroundService.sendAction(context, TimerForegroundService.ActionToggle)
        } else {
            TimerForegroundService.startStopwatch(context)
        }
    }

    fun reset(context: Context) {
        TimerForegroundService.sendAction(context, TimerForegroundService.ActionReset)
    }

    private fun ActiveTimerSession.Stopwatch.toUiState(): StopwatchRunningUiState {
        return StopwatchRunningUiState(
            elapsedMillis = elapsedMillis,
            startedAtMillis = startedAtMillis,
            isRunning = isRunning,
            hasActiveSession = true
        )
    }
}
