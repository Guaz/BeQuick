package com.kitsuneo.bquick.feature.timer

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

data class TimerRunningUiState(
    val durationSeconds: Int = 5 * 60,
    val remainingSeconds: Int = durationSeconds,
    val isRunning: Boolean = false,
    val isComplete: Boolean = false,
    val hasActiveSession: Boolean = false
) {
    val elapsedSeconds: Int
        get() = (durationSeconds - remainingSeconds).coerceAtLeast(0)

    val progress: Float
        get() = if (durationSeconds == 0) 1f
        else (elapsedSeconds.toFloat() / durationSeconds).coerceIn(0f, 1f)
}

class TimerRunningViewModel : ViewModel() {
    private val initialState = TimerRunningUiState()

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<TimerRunningUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            TimerSessionStore.activeSession.collectLatest { session ->
                val countdownSession = session as? ActiveTimerSession.Countdown
                _state.value = countdownSession?.toUiState() ?: initialState
            }
        }
    }

    fun primaryAction(context: Context) {
        val state = _state.value
        if (state.hasActiveSession) {
            TimerForegroundService.sendAction(context, TimerForegroundService.ActionToggle)
        } else {
            TimerForegroundService.startCountdown(context, state.durationSeconds)
        }
    }

    fun reset(context: Context) {
        TimerForegroundService.sendAction(context, TimerForegroundService.ActionReset)
    }

    private fun ActiveTimerSession.Countdown.toUiState(): TimerRunningUiState {
        return TimerRunningUiState(
            durationSeconds = durationSeconds,
            remainingSeconds = remainingSeconds,
            isRunning = isRunning,
            isComplete = isComplete,
            hasActiveSession = true
        )
    }
}
