package com.kitsuneo.bquick.feature.interval

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class IntervalPhase {
    Work,
    Rest,
    Complete
}

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
    workSeconds: Int,
    restSeconds: Int,
    rounds: Int
) : ViewModel() {
    private val initialState = IntervalRunningUiState(
        workSeconds = workSeconds.coerceIn(10, 180),
        restSeconds = restSeconds.coerceIn(5, 120),
        totalRounds = rounds.coerceIn(1, 20)
    )

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<IntervalRunningUiState> = _state.asStateFlow()

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
        _state.value = initialState
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
        if (current.remainingPhaseSeconds > 1) {
            _state.update { it.copy(remainingPhaseSeconds = it.remainingPhaseSeconds - 1) }
            return
        }

        when (current.currentPhase) {
            IntervalPhase.Work -> {
                if (current.currentRound >= current.totalRounds) {
                    _state.update {
                        it.copy(
                            currentPhase = IntervalPhase.Complete,
                            remainingPhaseSeconds = 0,
                            phaseDurationSeconds = 0,
                            isRunning = false,
                            isComplete = true
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            currentPhase = IntervalPhase.Rest,
                            phaseDurationSeconds = it.restSeconds,
                            remainingPhaseSeconds = it.restSeconds
                        )
                    }
                }
            }

            IntervalPhase.Rest -> {
                _state.update {
                    it.copy(
                        currentRound = it.currentRound + 1,
                        currentPhase = IntervalPhase.Work,
                        phaseDurationSeconds = it.workSeconds,
                        remainingPhaseSeconds = it.workSeconds
                    )
                }
            }

            IntervalPhase.Complete -> Unit
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }

    companion object {
        fun factory(workSeconds: Int, restSeconds: Int, rounds: Int): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    IntervalRunningViewModel(
                        workSeconds = workSeconds,
                        restSeconds = restSeconds,
                        rounds = rounds
                    )
                }
            }
    }
}
