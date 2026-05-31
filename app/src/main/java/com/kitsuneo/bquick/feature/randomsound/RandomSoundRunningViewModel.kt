package com.kitsuneo.bquick.feature.randomsound

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class RandomSoundRunningUiState(
    val durationMinutes: Int,
    val minGapSeconds: Int,
    val maxGapSeconds: Int,
    val remainingSessionSeconds: Int = durationMinutes * 60,
    val nextCueInSeconds: Int,
    val cueCount: Int = 0,
    val soundEventId: Int = 0,
    val isRunning: Boolean = true,
    val isComplete: Boolean = false
) {
    val totalSessionSeconds: Int
        get() = durationMinutes * 60

    val progress: Float
        get() = if (totalSessionSeconds == 0) 1f
        else ((totalSessionSeconds - remainingSessionSeconds).toFloat() / totalSessionSeconds).coerceIn(0f, 1f)
}

class RandomSoundRunningViewModel(
    durationMinutes: Int,
    minGapSeconds: Int,
    maxGapSeconds: Int
) : ViewModel() {
    private val normalizedDuration = durationMinutes.coerceIn(1, 30)
    private val normalizedMinGap = minGapSeconds.coerceIn(3, 120)
    private val normalizedMaxGap = maxGapSeconds.coerceIn(normalizedMinGap, 180)

    private fun createInitialState(): RandomSoundRunningUiState {
        val totalSessionSeconds = normalizedDuration * 60
        return RandomSoundRunningUiState(
            durationMinutes = normalizedDuration,
            minGapSeconds = normalizedMinGap,
            maxGapSeconds = normalizedMaxGap,
            remainingSessionSeconds = totalSessionSeconds,
            nextCueInSeconds = pickNextCue(totalSessionSeconds)
        )
    }

    private val _state = MutableStateFlow(createInitialState())
    val state: StateFlow<RandomSoundRunningUiState> = _state.asStateFlow()

    private var timerJob: Job? = null

    init {
        startTicker()
    }

    fun toggleRunning() {
        if (_state.value.isComplete) return
        _state.update { it.copy(isRunning = !it.isRunning) }
        startTicker()
    }

    fun reset() {
        _state.value = createInitialState()
        startTicker()
    }

    private fun startTicker() {
        if (timerJob?.isActive == true) return
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                val current = _state.value
                if (!current.isRunning || current.isComplete) continue
                tick()
            }
        }
    }

    private fun tick() {
        val current = _state.value
        val remainingSessionSeconds = current.remainingSessionSeconds - 1
        if (remainingSessionSeconds <= 0) {
            _state.update {
                it.copy(
                    remainingSessionSeconds = 0,
                    nextCueInSeconds = 0,
                    isRunning = false,
                    isComplete = true
                )
            }
            return
        }

        val nextCueInSeconds = current.nextCueInSeconds - 1
        if (nextCueInSeconds <= 0) {
            _state.update {
                it.copy(
                    remainingSessionSeconds = remainingSessionSeconds,
                    nextCueInSeconds = pickNextCue(remainingSessionSeconds),
                    cueCount = it.cueCount + 1,
                    soundEventId = it.soundEventId + 1
                )
            }
        } else {
            _state.update {
                it.copy(
                    remainingSessionSeconds = remainingSessionSeconds,
                    nextCueInSeconds = nextCueInSeconds
                )
            }
        }
    }

    private fun pickNextCue(remainingSessionSeconds: Int): Int {
        val upperBound = normalizedMaxGap.coerceAtMost(remainingSessionSeconds)
        val lowerBound = normalizedMinGap.coerceAtMost(upperBound)
        if (lowerBound == upperBound) return lowerBound
        return Random.nextInt(lowerBound, upperBound + 1)
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }

    companion object {
        fun factory(
            durationMinutes: Int,
            minGapSeconds: Int,
            maxGapSeconds: Int
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RandomSoundRunningViewModel(
                    durationMinutes = durationMinutes,
                    minGapSeconds = minGapSeconds,
                    maxGapSeconds = maxGapSeconds
                )
            }
        }
    }
}
