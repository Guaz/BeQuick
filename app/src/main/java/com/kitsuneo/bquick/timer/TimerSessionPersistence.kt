package com.kitsuneo.bquick.timer

import android.content.Context
import androidx.core.content.edit

class TimerSessionPersistence(context: Context) {
    private val prefs = context.getSharedPreferences("timer_session", Context.MODE_PRIVATE)

    fun save(session: ActiveTimerSession?) {
        if (session == null) {
            prefs.edit { clear() }
            return
        }

        prefs.edit {
            when (session) {
                is ActiveTimerSession.Interval -> {
                    putString("type", "interval")
                    putInt("workSeconds", session.workSeconds)
                    putInt("restSeconds", session.restSeconds)
                    putInt("totalRounds", session.totalRounds)
                    putInt("currentRound", session.currentRound)
                    putString("currentPhase", session.currentPhase.name)
                    putInt("phaseDurationSeconds", session.phaseDurationSeconds)
                    putInt("remainingPhaseSeconds", session.remainingPhaseSeconds)
                    putBoolean("isRunning", session.isRunning)
                    putBoolean("isComplete", session.isComplete)
                }

                is ActiveTimerSession.Reaction -> {
                    putString("type", "reaction")
                    putInt("durationSeconds", session.durationSeconds)
                    putInt("minGapSeconds", session.minGapSeconds)
                    putInt("maxGapSeconds", session.maxGapSeconds)
                    putInt("remainingSessionSeconds", session.remainingSessionSeconds)
                    putInt("nextCueInSeconds", session.nextCueInSeconds)
                    putInt("cueCount", session.cueCount)
                    putInt("soundEventId", session.soundEventId)
                    putBoolean("isRunning", session.isRunning)
                    putBoolean("isComplete", session.isComplete)
                }
            }
            putLong("updatedAtMillis", System.currentTimeMillis())
        }
    }

    fun load(): PersistedTimerSession? {
        val type = prefs.getString("type", null) ?: return null
        val updatedAtMillis = prefs.getLong("updatedAtMillis", System.currentTimeMillis())
        val session = when (type) {
            "interval" -> ActiveTimerSession.Interval(
                workSeconds = prefs.getInt("workSeconds", 40),
                restSeconds = prefs.getInt("restSeconds", 20),
                totalRounds = prefs.getInt("totalRounds", 8),
                currentRound = prefs.getInt("currentRound", 1),
                currentPhase = IntervalPhase.valueOf(
                    prefs.getString("currentPhase", IntervalPhase.Work.name) ?: IntervalPhase.Work.name
                ),
                phaseDurationSeconds = prefs.getInt("phaseDurationSeconds", prefs.getInt("workSeconds", 40)),
                remainingPhaseSeconds = prefs.getInt("remainingPhaseSeconds", prefs.getInt("workSeconds", 40)),
                isRunning = prefs.getBoolean("isRunning", true),
                isComplete = prefs.getBoolean("isComplete", false)
            )

            "reaction" -> ActiveTimerSession.Reaction(
                durationSeconds = prefs.getInt("durationSeconds", 5 * 60),
                minGapSeconds = prefs.getInt("minGapSeconds", 15),
                maxGapSeconds = prefs.getInt("maxGapSeconds", 45),
                remainingSessionSeconds = prefs.getInt("remainingSessionSeconds", prefs.getInt("durationSeconds", 5 * 60)),
                nextCueInSeconds = prefs.getInt("nextCueInSeconds", 15),
                cueCount = prefs.getInt("cueCount", 0),
                soundEventId = prefs.getInt("soundEventId", 0),
                isRunning = prefs.getBoolean("isRunning", true),
                isComplete = prefs.getBoolean("isComplete", false)
            )

            else -> null
        } ?: return null

        return PersistedTimerSession(session = session, updatedAtMillis = updatedAtMillis)
    }
}

data class PersistedTimerSession(
    val session: ActiveTimerSession,
    val updatedAtMillis: Long
)
