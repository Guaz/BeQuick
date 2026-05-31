package com.kitsuneo.bquick.feature.interval

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitsuneo.bquick.timer.ActiveTimerSession
import com.kitsuneo.bquick.timer.IntervalPhase
import com.kitsuneo.bquick.timer.TimerForegroundService
import com.kitsuneo.bquick.timer.TimerSessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class IntervalRunningUiState(
    val workSeconds: Int,
    val restSeconds: Int,
    val totalRounds: Int,
    val currentRound: Int = 1,
    val currentPhase: IntervalPhase = IntervalPhase.Work,
    val phaseDurationSeconds: Int = workSeconds,
    val remainingPhaseSeconds: Int = workSeconds,
    val isRunning: Boolean = true,
    val isComplete: Boolean = false
) {
    val totalDurationSeconds: Int
        get() = (workSeconds * totalRounds) + (restSeconds * (totalRounds - 1))

    val elapsedSeconds: Int
        get() = totalDurationSeconds - remainingProgramSeconds

    val remainingProgramSeconds: Int
        get() {
            if (isComplete) return 0

            val completedWorkRounds = when (currentPhase) {
                IntervalPhase.Work -> currentRound - 1
                IntervalPhase.Rest -> currentRound
                IntervalPhase.Complete -> totalRounds
            }
            val completedRestRounds = when (currentPhase) {
                IntervalPhase.Work -> (currentRound - 1).coerceAtLeast(0)
                IntervalPhase.Rest -> currentRound - 1
                IntervalPhase.Complete -> (totalRounds - 1).coerceAtLeast(0)
            }
            val completedSeconds =
                (completedWorkRounds * workSeconds) + (completedRestRounds * restSeconds)
            val remainingSegment = if (currentPhase == IntervalPhase.Complete) 0 else remainingPhaseSeconds
            return (totalDurationSeconds - completedSeconds - (phaseDurationSeconds - remainingSegment))
                .coerceAtLeast(0)
        }

    val phaseProgress: Float
        get() = if (phaseDurationSeconds == 0) 1f
        else ((phaseDurationSeconds - remainingPhaseSeconds).toFloat() / phaseDurationSeconds).coerceIn(0f, 1f)

    val sessionProgress: Float
        get() = if (totalDurationSeconds == 0) 1f
        else (elapsedSeconds.toFloat() / totalDurationSeconds).coerceIn(0f, 1f)
}

class IntervalRunningViewModel(
) : ViewModel() {
    private val initialState = IntervalRunningUiState(
        workSeconds = 40,
        restSeconds = 20,
        totalRounds = 8,
        isRunning = false
    )

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<IntervalRunningUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            TimerSessionStore.activeSession.collectLatest { session ->
                val intervalSession = session as? ActiveTimerSession.Interval
                _state.value = intervalSession?.toUiState() ?: initialState
            }
        }
    }

    fun toggleRunning(context: android.content.Context) {
        TimerForegroundService.sendAction(context, TimerForegroundService.ActionToggle)
    }

    fun reset(context: android.content.Context) {
        TimerForegroundService.sendAction(context, TimerForegroundService.ActionReset)
    }

    fun stop(context: android.content.Context) {
        TimerForegroundService.sendAction(context, TimerForegroundService.ActionStop)
    }

    private fun ActiveTimerSession.Interval.toUiState(): IntervalRunningUiState {
        return IntervalRunningUiState(
            workSeconds = workSeconds,
            restSeconds = restSeconds,
            totalRounds = totalRounds,
            currentRound = currentRound,
            currentPhase = currentPhase,
            phaseDurationSeconds = phaseDurationSeconds,
            remainingPhaseSeconds = remainingPhaseSeconds,
            isRunning = isRunning,
            isComplete = isComplete
        )
    }
}
