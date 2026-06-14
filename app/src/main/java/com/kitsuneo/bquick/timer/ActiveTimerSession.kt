package com.kitsuneo.bquick.timer

sealed interface ActiveTimerSession {
    val isRunning: Boolean
    val isComplete: Boolean

    data class Interval(
        val preparationSeconds: Int = 10,
        val workSeconds: Int,
        val restSeconds: Int,
        val totalRounds: Int,
        val currentRound: Int = 1,
        val currentPhase: IntervalPhase = if (preparationSeconds > 0) IntervalPhase.Preparation else IntervalPhase.Work,
        val phaseDurationSeconds: Int = if (preparationSeconds > 0) preparationSeconds else workSeconds,
        val remainingPhaseSeconds: Int = phaseDurationSeconds,
        override val isRunning: Boolean = true,
        override val isComplete: Boolean = false
    ) : ActiveTimerSession {
        val totalDurationSeconds: Int
            get() = preparationSeconds + (workSeconds * totalRounds) + (restSeconds * (totalRounds - 1))

        val remainingProgramSeconds: Int
            get() {
                if (isComplete) return 0

                val completedSecondsBeforePhase = when (currentPhase) {
                    IntervalPhase.Preparation -> 0
                    IntervalPhase.Work -> {
                        preparationSeconds +
                            ((currentRound - 1) * workSeconds) +
                            ((currentRound - 1).coerceAtLeast(0) * restSeconds)
                    }
                    IntervalPhase.Rest -> {
                        preparationSeconds +
                            (currentRound * workSeconds) +
                            ((currentRound - 1).coerceAtLeast(0) * restSeconds)
                    }
                    IntervalPhase.Complete -> totalDurationSeconds
                }
                val progressedInCurrentPhase = if (currentPhase == IntervalPhase.Complete) {
                    0
                } else {
                    phaseDurationSeconds - remainingPhaseSeconds
                }
                return (totalDurationSeconds - completedSecondsBeforePhase - progressedInCurrentPhase)
                    .coerceAtLeast(0)
            }
    }

    data class Reaction(
        val preparationSeconds: Int = 10,
        val durationSeconds: Int,
        val minGapSeconds: Int,
        val maxGapSeconds: Int,
        val remainingPreparationSeconds: Int = preparationSeconds,
        val remainingSessionSeconds: Int = durationSeconds,
        val nextCueInSeconds: Int,
        val cueCount: Int = 0,
        val soundEventId: Int = 0,
        override val isRunning: Boolean = true,
        override val isComplete: Boolean = false
    ) : ActiveTimerSession {
        val totalSessionSeconds: Int
            get() = preparationSeconds + durationSeconds

        val remainingTotalSeconds: Int
            get() = remainingPreparationSeconds + remainingSessionSeconds

        val isPreparing: Boolean
            get() = remainingPreparationSeconds > 0 && !isComplete
    }

    data class Countdown(
        val durationSeconds: Int,
        val remainingSeconds: Int = durationSeconds,
        override val isRunning: Boolean = true,
        override val isComplete: Boolean = false
    ) : ActiveTimerSession {
        val elapsedSeconds: Int
            get() = (durationSeconds - remainingSeconds).coerceAtLeast(0)
    }

    data class Stopwatch(
        val elapsedMillis: Long = 0L,
        val startedAtMillis: Long? = null,
        override val isRunning: Boolean = false,
        override val isComplete: Boolean = false
    ) : ActiveTimerSession
}

enum class IntervalPhase {
    Preparation,
    Work,
    Rest,
    Complete
}
