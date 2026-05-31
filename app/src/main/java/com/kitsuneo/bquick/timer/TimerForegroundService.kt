package com.kitsuneo.bquick.timer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.kitsuneo.bquick.audio.AppSoundPlayer
import com.kitsuneo.bquick.R
import com.kitsuneo.bquick.notification.TimerNotificationManager
import com.kitsuneo.bquick.notification.TimerNotificationState
import com.kitsuneo.bquick.settings.SoundSettingsRepository
import com.kitsuneo.bquick.ui.util.asClock
import kotlin.math.max
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerForegroundService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var persistence: TimerSessionPersistence
    private var tickerJob: Job? = null
    private var currentSession: ActiveTimerSession? = null

    override fun onCreate() {
        super.onCreate()
        SoundSettingsRepository.initialize(applicationContext)
        persistence = TimerSessionPersistence(applicationContext)
        restorePersistedSession()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ActionStartInterval -> {
                val session = ActiveTimerSession.Interval(
                    workSeconds = intent.getIntExtra(ExtraWorkSeconds, 40).coerceIn(0, 180),
                    restSeconds = intent.getIntExtra(ExtraRestSeconds, 20).coerceIn(0, 120),
                    totalRounds = intent.getIntExtra(ExtraRounds, 8).coerceIn(1, 20)
                )
                startSession(session)
            }

            ActionStartReaction -> {
                val durationSeconds = intent.getIntExtra(ExtraDurationSeconds, 5 * 60).coerceIn(10, 59 * 60 + 59)
                val minGapSeconds = intent.getIntExtra(ExtraMinGapSeconds, 15).coerceIn(1, 59 * 60 + 59)
                val maxGapSeconds = intent.getIntExtra(ExtraMaxGapSeconds, 45).coerceIn(minGapSeconds, 59 * 60 + 59)
                val session = ActiveTimerSession.Reaction(
                    durationSeconds = durationSeconds,
                    minGapSeconds = minGapSeconds,
                    maxGapSeconds = maxGapSeconds,
                    nextCueInSeconds = pickNextCue(
                        minGapSeconds = minGapSeconds,
                        maxGapSeconds = maxGapSeconds,
                        remainingSessionSeconds = durationSeconds
                    )
                )
                startSession(session)
            }

            ActionToggle -> toggleRunning()
            ActionReset -> resetCurrentSession()
            ActionStop -> clearSession()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        tickerJob?.cancel()
        AppSoundPlayer.release()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startSession(session: ActiveTimerSession) {
        val normalized = normalizeSession(session, playSounds = false)
        currentSession = normalized
        publish(normalized)
        startTicker()
    }

    private fun toggleRunning() {
        val session = currentSession ?: return
        val updated = when (session) {
            is ActiveTimerSession.Interval -> session.copy(isRunning = !session.isRunning)
            is ActiveTimerSession.Reaction -> session.copy(isRunning = !session.isRunning)
        }
        currentSession = updated
        publish(updated)
        startTicker()
    }

    private fun resetCurrentSession() {
        val session = currentSession ?: return
        val reset = when (session) {
            is ActiveTimerSession.Interval -> ActiveTimerSession.Interval(
                workSeconds = session.workSeconds,
                restSeconds = session.restSeconds,
                totalRounds = session.totalRounds
            )

            is ActiveTimerSession.Reaction -> ActiveTimerSession.Reaction(
                durationSeconds = session.durationSeconds,
                minGapSeconds = session.minGapSeconds,
                maxGapSeconds = session.maxGapSeconds,
                nextCueInSeconds = pickNextCue(
                    minGapSeconds = session.minGapSeconds,
                    maxGapSeconds = session.maxGapSeconds,
                    remainingSessionSeconds = session.durationSeconds
                )
            )
        }
        val normalized = normalizeSession(reset, playSounds = false)
        currentSession = normalized
        publish(normalized)
        startTicker()
    }

    private fun clearSession() {
        tickerJob?.cancel()
        tickerJob = null
        currentSession = null
        persistence.save(null)
        TimerSessionStore.update(null)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun restorePersistedSession() {
        val persisted = persistence.load() ?: return
        val elapsedSeconds = max(0, ((System.currentTimeMillis() - persisted.updatedAtMillis) / 1000L).toInt())
        val restored = if (persisted.session.isRunning && !persisted.session.isComplete && elapsedSeconds > 0) {
            fastForward(persisted.session, elapsedSeconds, playSounds = false)
        } else {
            normalizeSession(persisted.session, playSounds = false)
        }

        if (restored.isComplete) {
            currentSession = restored
            TimerSessionStore.update(restored)
        } else {
            currentSession = restored
            publish(restored)
            startTicker()
        }
    }

    private fun startTicker() {
        if (tickerJob?.isActive == true) return
        tickerJob = serviceScope.launch {
            while (isActive) {
                delay(1_000)
                val session = currentSession ?: continue
                if (!session.isRunning || session.isComplete) continue
                val updated = fastForward(session, 1, playSounds = true)
                currentSession = updated
                publish(updated)
                if (updated.isComplete) {
                    tickerJob?.cancel()
                    tickerJob = null
                    stopForeground(STOP_FOREGROUND_REMOVE)
                }
            }
        }
    }

    private fun publish(session: ActiveTimerSession) {
        TimerSessionStore.update(session)
        persistence.save(session)
        if (session.isComplete) {
            TimerNotificationManager.cancel(applicationContext)
        } else {
            startForeground(
                TimerNotificationManager.notificationId,
                TimerNotificationManager.build(applicationContext, session.toNotificationState())
            )
        }
    }

    private fun fastForward(
        session: ActiveTimerSession,
        elapsedSeconds: Int,
        playSounds: Boolean
    ): ActiveTimerSession {
        var updated = session
        repeat(elapsedSeconds) {
            updated = when (updated) {
                is ActiveTimerSession.Interval -> advanceInterval(updated, playSounds)
                is ActiveTimerSession.Reaction -> advanceReaction(updated, playSounds)
            }
            if (updated.isComplete) return updated
        }
        return updated
    }

    private fun normalizeSession(
        session: ActiveTimerSession,
        playSounds: Boolean
    ): ActiveTimerSession = when (session) {
        is ActiveTimerSession.Interval -> normalizeIntervalSession(session, playSounds)
        is ActiveTimerSession.Reaction -> session
    }

    private fun normalizeIntervalSession(
        session: ActiveTimerSession.Interval,
        playSounds: Boolean
    ): ActiveTimerSession.Interval {
        var current = session
        while (!current.isComplete && current.phaseDurationSeconds == 0) {
            current = when (current.currentPhase) {
                IntervalPhase.Work -> {
                    if (current.currentRound >= current.totalRounds) {
                        current.copy(
                            currentPhase = IntervalPhase.Complete,
                            remainingPhaseSeconds = 0,
                            phaseDurationSeconds = 0,
                            isRunning = false,
                            isComplete = true
                        )
                    } else {
                        if (playSounds) {
                            AppSoundPlayer.play(applicationContext, SoundSettingsRepository.settings.value.modeSwitch)
                        }
                        current.copy(
                            currentPhase = IntervalPhase.Rest,
                            phaseDurationSeconds = current.restSeconds,
                            remainingPhaseSeconds = current.restSeconds
                        )
                    }
                }

                IntervalPhase.Rest -> {
                    if (playSounds) {
                        AppSoundPlayer.play(applicationContext, SoundSettingsRepository.settings.value.modeSwitch)
                    }
                    current.copy(
                        currentRound = current.currentRound + 1,
                        currentPhase = IntervalPhase.Work,
                        phaseDurationSeconds = current.workSeconds,
                        remainingPhaseSeconds = current.workSeconds
                    )
                }

                IntervalPhase.Complete -> current
            }
        }
        return current
    }

    private fun advanceInterval(
        session: ActiveTimerSession.Interval,
        playSounds: Boolean
    ): ActiveTimerSession.Interval {
        if (session.remainingPhaseSeconds > 1) {
            return session.copy(remainingPhaseSeconds = session.remainingPhaseSeconds - 1)
        }

        return when (session.currentPhase) {
            IntervalPhase.Work -> {
                if (session.currentRound >= session.totalRounds) {
                    session.copy(
                        currentPhase = IntervalPhase.Complete,
                        remainingPhaseSeconds = 0,
                        phaseDurationSeconds = 0,
                        isRunning = false,
                        isComplete = true
                    )
                } else {
                    if (playSounds) {
                        AppSoundPlayer.play(applicationContext, SoundSettingsRepository.settings.value.modeSwitch)
                    }
                    normalizeIntervalSession(
                        session.copy(
                            currentPhase = IntervalPhase.Rest,
                            phaseDurationSeconds = session.restSeconds,
                            remainingPhaseSeconds = session.restSeconds
                        ),
                        playSounds = false
                    )
                }
            }

            IntervalPhase.Rest -> {
                if (playSounds) {
                    AppSoundPlayer.play(applicationContext, SoundSettingsRepository.settings.value.modeSwitch)
                }
                normalizeIntervalSession(
                    session.copy(
                        currentRound = session.currentRound + 1,
                        currentPhase = IntervalPhase.Work,
                        phaseDurationSeconds = session.workSeconds,
                        remainingPhaseSeconds = session.workSeconds
                    ),
                    playSounds = false
                )
            }

            IntervalPhase.Complete -> session
        }
    }

    private fun advanceReaction(
        session: ActiveTimerSession.Reaction,
        playSounds: Boolean
    ): ActiveTimerSession.Reaction {
        val remainingSessionSeconds = session.remainingSessionSeconds - 1
        if (remainingSessionSeconds <= 0) {
            return session.copy(
                remainingSessionSeconds = 0,
                nextCueInSeconds = 0,
                isRunning = false,
                isComplete = true
            )
        }

        val nextCueInSeconds = session.nextCueInSeconds - 1
        return if (nextCueInSeconds <= 0) {
            if (playSounds) {
                AppSoundPlayer.play(applicationContext, SoundSettingsRepository.settings.value.reaction)
            }
            session.copy(
                remainingSessionSeconds = remainingSessionSeconds,
                nextCueInSeconds = pickNextCue(
                    minGapSeconds = session.minGapSeconds,
                    maxGapSeconds = session.maxGapSeconds,
                    remainingSessionSeconds = remainingSessionSeconds
                ),
                cueCount = session.cueCount + 1,
                soundEventId = session.soundEventId + 1
            )
        } else {
            session.copy(
                remainingSessionSeconds = remainingSessionSeconds,
                nextCueInSeconds = nextCueInSeconds
            )
        }
    }

    private fun pickNextCue(
        minGapSeconds: Int,
        maxGapSeconds: Int,
        remainingSessionSeconds: Int
    ): Int {
        val upperBound = maxGapSeconds.coerceAtMost(remainingSessionSeconds)
        val lowerBound = minGapSeconds.coerceAtMost(upperBound)
        if (lowerBound == upperBound) return lowerBound
        return Random.nextInt(lowerBound, upperBound + 1)
    }

    private fun ActiveTimerSession.toNotificationState(): TimerNotificationState = when (this) {
        is ActiveTimerSession.Interval -> TimerNotificationState(
            modeLabel = when (currentPhase) {
                IntervalPhase.Work -> getString(R.string.phase_work)
                IntervalPhase.Rest -> getString(R.string.phase_rest)
                IntervalPhase.Complete -> getString(R.string.phase_complete)
            },
            timeText = remainingPhaseSeconds.asClock(),
            isRunning = isRunning
        )

        is ActiveTimerSession.Reaction -> TimerNotificationState(
            modeLabel = getString(R.string.phase_reaction),
            timeText = remainingSessionSeconds.asClock(),
            isRunning = isRunning
        )
    }

    companion object {
        private const val ActionStartInterval = "com.kitsuneo.bquick.timer.START_INTERVAL"
        private const val ActionStartReaction = "com.kitsuneo.bquick.timer.START_REACTION"
        const val ActionToggle = "com.kitsuneo.bquick.timer.TOGGLE"
        const val ActionReset = "com.kitsuneo.bquick.timer.RESET"
        const val ActionStop = "com.kitsuneo.bquick.timer.STOP"

        private const val ExtraWorkSeconds = "workSeconds"
        private const val ExtraRestSeconds = "restSeconds"
        private const val ExtraRounds = "rounds"
        private const val ExtraDurationSeconds = "durationSeconds"
        private const val ExtraMinGapSeconds = "minGapSeconds"
        private const val ExtraMaxGapSeconds = "maxGapSeconds"

        fun startInterval(
            context: Context,
            workSeconds: Int,
            restSeconds: Int,
            rounds: Int
        ) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, TimerForegroundService::class.java).apply {
                    action = ActionStartInterval
                    putExtra(ExtraWorkSeconds, workSeconds)
                    putExtra(ExtraRestSeconds, restSeconds)
                    putExtra(ExtraRounds, rounds)
                }
            )
        }

        fun startReaction(
            context: Context,
            durationSeconds: Int,
            minGapSeconds: Int,
            maxGapSeconds: Int
        ) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, TimerForegroundService::class.java).apply {
                    action = ActionStartReaction
                    putExtra(ExtraDurationSeconds, durationSeconds)
                    putExtra(ExtraMinGapSeconds, minGapSeconds)
                    putExtra(ExtraMaxGapSeconds, maxGapSeconds)
                }
            )
        }

        fun sendAction(context: Context, action: String) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, TimerForegroundService::class.java).apply {
                    this.action = action
                }
            )
        }
    }
}
