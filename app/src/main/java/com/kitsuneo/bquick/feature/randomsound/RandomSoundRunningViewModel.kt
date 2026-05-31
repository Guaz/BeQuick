package com.kitsuneo.bquick.feature.randomsound

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

data class RandomSoundRunningUiState(
    val durationSeconds: Int,
    val minGapSeconds: Int,
    val maxGapSeconds: Int,
    val remainingSessionSeconds: Int = durationSeconds,
    val nextCueInSeconds: Int,
    val cueCount: Int = 0,
    val soundEventId: Int = 0,
    val isRunning: Boolean = true,
    val isComplete: Boolean = false
) {
    val totalSessionSeconds: Int
        get() = durationSeconds

    val progress: Float
        get() = if (totalSessionSeconds == 0) 1f
        else ((totalSessionSeconds - remainingSessionSeconds).toFloat() / totalSessionSeconds).coerceIn(0f, 1f)
}

class RandomSoundRunningViewModel(
) : ViewModel() {
    private val initialState = RandomSoundRunningUiState(
        durationSeconds = 5 * 60,
        minGapSeconds = 15,
        maxGapSeconds = 45,
        remainingSessionSeconds = 5 * 60,
        nextCueInSeconds = 15,
        isRunning = false
    )

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<RandomSoundRunningUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            TimerSessionStore.activeSession.collectLatest { session ->
                val reactionSession = session as? ActiveTimerSession.Reaction
                _state.value = reactionSession?.toUiState() ?: initialState
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

    private fun ActiveTimerSession.Reaction.toUiState(): RandomSoundRunningUiState {
        return RandomSoundRunningUiState(
            durationSeconds = durationSeconds,
            minGapSeconds = minGapSeconds,
            maxGapSeconds = maxGapSeconds,
            remainingSessionSeconds = remainingSessionSeconds,
            nextCueInSeconds = nextCueInSeconds,
            cueCount = cueCount,
            soundEventId = soundEventId,
            isRunning = isRunning,
            isComplete = isComplete
        )
    }
}
