package com.kitsuneo.bquick.timer

sealed interface ActiveTimerSession {
    val isRunning: Boolean
    val isComplete: Boolean

    data class Interval(
        val workSeconds: Int,
        val restSeconds: Int,
        val totalRounds: Int,
        val currentRound: Int = 1,
        val currentPhase: IntervalPhase = IntervalPhase.Work,
        val phaseDurationSeconds: Int = workSeconds,
        val remainingPhaseSeconds: Int = workSeconds,
        override val isRunning: Boolean = true,
        override val isComplete: Boolean = false
    ) : ActiveTimerSession {
        val totalDurationSeconds: Int
            get() = (workSeconds * totalRounds) + (restSeconds * (totalRounds - 1))

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
    }

    data class Reaction(
        val durationSeconds: Int,
        val minGapSeconds: Int,
        val maxGapSeconds: Int,
        val remainingSessionSeconds: Int = durationSeconds,
        val nextCueInSeconds: Int,
        val cueCount: Int = 0,
        val soundEventId: Int = 0,
        override val isRunning: Boolean = true,
        override val isComplete: Boolean = false
    ) : ActiveTimerSession
}

enum class IntervalPhase {
    Work,
    Rest,
    Complete
}
