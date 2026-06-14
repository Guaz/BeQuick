package com.kitsuneo.bquick.timer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.kitsuneo.bquick.alarm.AlarmAlertService
import com.kitsuneo.bquick.alarm.AlarmEntry
import com.kitsuneo.bquick.audio.AppSoundPlayer
import com.kitsuneo.bquick.audio.TimerSignalPlayer
import com.kitsuneo.bquick.R
import com.kitsuneo.bquick.notification.TimerNotificationManager
import com.kitsuneo.bquick.notification.TimerNotificationState
import com.kitsuneo.bquick.settings.IntervalCueMode
import com.kitsuneo.bquick.settings.SoundSettingsRepository
import com.kitsuneo.bquick.ui.util.asClock
import com.kitsuneo.bquick.ui.util.asStopwatch
import java.util.Calendar
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
            ActionStartCountdown -> {
                val session = ActiveTimerSession.Countdown(
                    durationSeconds = intent.getIntExtra(ExtraDurationSeconds, 5 * 60).coerceIn(1, 59 * 60 + 59)
                )
                startSession(session)
            }

            ActionStartStopwatch -> {
                startSession(
                    ActiveTimerSession.Stopwatch(
                        startedAtMillis = System.currentTimeMillis(),
                        isRunning = true
                    )
                )
            }

            ActionStartInterval -> {
                val session = ActiveTimerSession.Interval(
                    preparationSeconds = intent.getIntExtra(ExtraPreparationSeconds, 10).coerceIn(0, 120),
                    workSeconds = intent.getIntExtra(ExtraWorkSeconds, 40).coerceIn(0, 180),
                    restSeconds = intent.getIntExtra(ExtraRestSeconds, 20).coerceIn(0, 120),
                    totalRounds = intent.getIntExtra(ExtraRounds, 8).coerceIn(1, 20)
                )
                startSession(session)
            }

            ActionStartReaction -> {
                val preparationSeconds = intent.getIntExtra(ExtraPreparationSeconds, 10).coerceIn(0, 120)
                val durationSeconds = intent.getIntExtra(ExtraDurationSeconds, 5 * 60).coerceIn(10, 59 * 60 + 59)
                val minGapSeconds = intent.getIntExtra(ExtraMinGapSeconds, 15).coerceIn(1, 59 * 60 + 59)
                val maxGapSeconds = intent.getIntExtra(ExtraMaxGapSeconds, 45).coerceIn(minGapSeconds, 59 * 60 + 59)
                val session = ActiveTimerSession.Reaction(
                    preparationSeconds = preparationSeconds,
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
        TimerSignalPlayer.release()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startSession(session: ActiveTimerSession) {
        val normalized = normalizeSession(session, playSounds = false)
        currentSession = normalized
        publish(normalized)
        playSessionStartSignalIfNeeded(normalized)
        startTicker()
    }

    private fun toggleRunning() {
        val session = currentSession ?: return
        val now = System.currentTimeMillis()
        val updated = when (session) {
            is ActiveTimerSession.Countdown -> session.copy(isRunning = !session.isRunning)
            is ActiveTimerSession.Interval -> session.copy(isRunning = !session.isRunning)
            is ActiveTimerSession.Reaction -> session.copy(isRunning = !session.isRunning)
            is ActiveTimerSession.Stopwatch -> {
                if (session.isRunning) {
                    session.copy(
                        elapsedMillis = session.currentElapsedMillis(now),
                        startedAtMillis = null,
                        isRunning = false
                    )
                } else {
                    session.copy(
                        startedAtMillis = now,
                        isRunning = true
                    )
                }
            }
        }
        currentSession = updated
        publish(updated)
        startTicker()
    }

    private fun resetCurrentSession() {
        val session = currentSession ?: return
        val reset = when (session) {
            is ActiveTimerSession.Countdown -> ActiveTimerSession.Countdown(
                durationSeconds = session.durationSeconds,
                isRunning = false
            )

            is ActiveTimerSession.Interval -> ActiveTimerSession.Interval(
                preparationSeconds = session.preparationSeconds,
                workSeconds = session.workSeconds,
                restSeconds = session.restSeconds,
                totalRounds = session.totalRounds,
                isRunning = false
            )

            is ActiveTimerSession.Reaction -> ActiveTimerSession.Reaction(
                preparationSeconds = session.preparationSeconds,
                durationSeconds = session.durationSeconds,
                minGapSeconds = session.minGapSeconds,
                maxGapSeconds = session.maxGapSeconds,
                nextCueInSeconds = pickNextCue(
                    minGapSeconds = session.minGapSeconds,
                    maxGapSeconds = session.maxGapSeconds,
                    remainingSessionSeconds = session.durationSeconds
                ),
                isRunning = false
            )

            is ActiveTimerSession.Stopwatch -> ActiveTimerSession.Stopwatch()
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
        val restored = when (val session = persisted.session) {
            is ActiveTimerSession.Stopwatch -> {
                if (session.isRunning) {
                    session.copy(
                        elapsedMillis = session.currentElapsedMillis(System.currentTimeMillis()),
                        startedAtMillis = System.currentTimeMillis()
                    )
                } else {
                    session
                }
            }

            else -> if (persisted.session.isRunning && !persisted.session.isComplete && elapsedSeconds > 0) {
                fastForward(persisted.session, elapsedSeconds, playSounds = false)
            } else {
                normalizeSession(persisted.session, playSounds = false)
            }
        }
        val shouldTriggerTimerAlarm = persisted.session is ActiveTimerSession.Countdown &&
            persisted.session.isRunning &&
            !persisted.session.isComplete &&
            restored is ActiveTimerSession.Countdown &&
            restored.isComplete

        if (restored.isComplete) {
            currentSession = restored
            TimerSessionStore.update(restored)
            if (shouldTriggerTimerAlarm) {
                triggerTimerAlarm()
            }
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
                if (session is ActiveTimerSession.Countdown &&
                    !session.isComplete &&
                    updated is ActiveTimerSession.Countdown &&
                    updated.isComplete
                ) {
                    triggerTimerAlarm()
                }
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
        if (elapsedSeconds <= 0) return session
        when (session) {
            is ActiveTimerSession.Countdown -> {
                if (!session.isRunning || session.isComplete) return session
                val remainingSeconds = (session.remainingSeconds - elapsedSeconds).coerceAtLeast(0)
                return session.copy(
                    remainingSeconds = remainingSeconds,
                    isRunning = remainingSeconds > 0,
                    isComplete = remainingSeconds == 0
                )
            }

            is ActiveTimerSession.Stopwatch -> {
                if (!session.isRunning || session.isComplete) return session
                return session.copy(
                    elapsedMillis = session.elapsedMillis + (elapsedSeconds * 1_000L),
                    startedAtMillis = System.currentTimeMillis()
                )
            }

            else -> Unit
        }

        var updated = session
        repeat(elapsedSeconds) {
            updated = when (updated) {
                is ActiveTimerSession.Countdown -> advanceCountdown(updated)
                is ActiveTimerSession.Interval -> advanceInterval(updated, playSounds)
                is ActiveTimerSession.Reaction -> advanceReaction(updated, playSounds)
                is ActiveTimerSession.Stopwatch -> advanceStopwatch(updated)
            }
            if (updated.isComplete) return updated
        }
        return updated
    }

    private fun normalizeSession(
        session: ActiveTimerSession,
        playSounds: Boolean
    ): ActiveTimerSession = when (session) {
        is ActiveTimerSession.Countdown -> session
        is ActiveTimerSession.Interval -> normalizeIntervalSession(session, playSounds)
        is ActiveTimerSession.Reaction -> session
        is ActiveTimerSession.Stopwatch -> session
    }

    private fun normalizeIntervalSession(
        session: ActiveTimerSession.Interval,
        playSounds: Boolean
    ): ActiveTimerSession.Interval {
        var current = session
        while (!current.isComplete && current.phaseDurationSeconds == 0) {
            current = when (current.currentPhase) {
                IntervalPhase.Preparation -> {
                    if (playSounds) {
                        TimerSignalPlayer.play(this, SoundSettingsRepository.settings.value.timerSignals.intervalStart)
                    }
                    current.copy(
                        currentPhase = IntervalPhase.Work,
                        phaseDurationSeconds = current.workSeconds,
                        remainingPhaseSeconds = current.workSeconds
                    )
                }

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
                            TimerSignalPlayer.play(this, SoundSettingsRepository.settings.value.timerSignals.intervalRest)
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
                        TimerSignalPlayer.play(this, SoundSettingsRepository.settings.value.timerSignals.intervalWork)
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
            val remainingPhaseSeconds = session.remainingPhaseSeconds - 1
            if (playSounds) {
                maybePlayIntervalExtraCue(session, remainingPhaseSeconds)
            }
            return session.copy(remainingPhaseSeconds = remainingPhaseSeconds)
        }

        return when (session.currentPhase) {
            IntervalPhase.Preparation -> {
                if (playSounds) {
                    TimerSignalPlayer.play(this, SoundSettingsRepository.settings.value.timerSignals.intervalStart)
                }
                normalizeIntervalSession(
                    session.copy(
                        currentPhase = IntervalPhase.Work,
                        phaseDurationSeconds = session.workSeconds,
                        remainingPhaseSeconds = session.workSeconds
                    ),
                    playSounds = false
                )
            }

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
                        TimerSignalPlayer.play(this, SoundSettingsRepository.settings.value.timerSignals.intervalRest)
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
                    TimerSignalPlayer.play(this, SoundSettingsRepository.settings.value.timerSignals.intervalWork)
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
        if (session.remainingPreparationSeconds > 0) {
            val remainingPreparationSeconds = session.remainingPreparationSeconds - 1
            if (remainingPreparationSeconds <= 0) {
                if (playSounds) {
                    TimerSignalPlayer.play(this, SoundSettingsRepository.settings.value.timerSignals.randomStart)
                }
                return session.copy(remainingPreparationSeconds = 0)
            }
            return session.copy(remainingPreparationSeconds = remainingPreparationSeconds)
        }

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
                TimerSignalPlayer.play(this, SoundSettingsRepository.settings.value.timerSignals.randomBeep)
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

    private fun advanceCountdown(session: ActiveTimerSession.Countdown): ActiveTimerSession.Countdown {
        if (session.remainingSeconds > 1) {
            return session.copy(remainingSeconds = session.remainingSeconds - 1)
        }

        return session.copy(
            remainingSeconds = 0,
            isRunning = false,
            isComplete = true
        )
    }

    private fun advanceStopwatch(session: ActiveTimerSession.Stopwatch): ActiveTimerSession.Stopwatch {
        val now = System.currentTimeMillis()
        return session.copy(
            elapsedMillis = session.currentElapsedMillis(now),
            startedAtMillis = now
        )
    }

    private fun playSessionStartSignalIfNeeded(session: ActiveTimerSession) {
        val timerSignals = SoundSettingsRepository.settings.value.timerSignals
        when (session) {
            is ActiveTimerSession.Countdown -> Unit
            is ActiveTimerSession.Interval -> {
                if (session.preparationSeconds == 0 && session.currentPhase == IntervalPhase.Work && !session.isComplete) {
                    TimerSignalPlayer.play(this, timerSignals.intervalStart)
                }
            }
            is ActiveTimerSession.Reaction -> {
                if (session.remainingPreparationSeconds == 0 && !session.isComplete) {
                    TimerSignalPlayer.play(this, timerSignals.randomStart)
                }
            }
            is ActiveTimerSession.Stopwatch -> Unit
        }
    }

    private fun maybePlayIntervalExtraCue(
        session: ActiveTimerSession.Interval,
        remainingPhaseSeconds: Int
    ) {
        if (session.currentPhase !in setOf(IntervalPhase.Work, IntervalPhase.Rest)) return
        val timerSignals = SoundSettingsRepository.settings.value.timerSignals
        timerSignals.intervalExtraCues
            .filter { it.enabled }
            .firstOrNull { cue ->
                when (cue.mode) {
                    IntervalCueMode.SecondsBeforeEnd -> remainingPhaseSeconds == cue.secondsBeforeEnd
                    IntervalCueMode.Middle -> remainingPhaseSeconds == session.phaseDurationSeconds / 2
                }
            }?.let { cue ->
                TimerSignalPlayer.play(this, cue.signal)
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
        is ActiveTimerSession.Countdown -> TimerNotificationState(
            modeLabel = getString(R.string.home_timer_title),
            timeText = remainingSeconds.asClock(),
            isRunning = isRunning
        )

        is ActiveTimerSession.Interval -> TimerNotificationState(
            modeLabel = when (currentPhase) {
                IntervalPhase.Preparation -> getString(R.string.phase_preparation)
                IntervalPhase.Work -> getString(R.string.phase_work)
                IntervalPhase.Rest -> getString(R.string.phase_rest)
                IntervalPhase.Complete -> getString(R.string.phase_complete)
            },
            timeText = remainingPhaseSeconds.asClock(),
            isRunning = isRunning
        )

        is ActiveTimerSession.Reaction -> TimerNotificationState(
            modeLabel = getString(if (isPreparing) R.string.phase_preparation else R.string.phase_reaction),
            timeText = if (isPreparing) remainingPreparationSeconds.asClock() else remainingSessionSeconds.asClock(),
            isRunning = isRunning
        )

        is ActiveTimerSession.Stopwatch -> TimerNotificationState(
            modeLabel = getString(R.string.home_stopwatch_title),
            timeText = currentElapsedMillis(System.currentTimeMillis()).asStopwatch(),
            isRunning = isRunning
        )
    }

    private fun triggerTimerAlarm() {
        AlarmAlertService.start(applicationContext, buildTimerCompletionAlarm())
    }

    private fun buildTimerCompletionAlarm(): AlarmEntry {
        val now = Calendar.getInstance()
        val settings = SoundSettingsRepository.settings.value
        return AlarmEntry(
            id = TimerCompletionAlarmId,
            name = getString(R.string.timer_complete_alert_title),
            hour = now.get(Calendar.HOUR_OF_DAY),
            minute = now.get(Calendar.MINUTE),
            repeatDays = emptySet(),
            enabled = true,
            soundSelection = settings.timerAlarmSound,
            volumePercent = 100,
            fadeUpEnabled = false,
            vibrateEnabled = true,
            snoozeEnabled = false
        )
    }

    private fun ActiveTimerSession.Stopwatch.currentElapsedMillis(now: Long): Long {
        val activeDelta = if (isRunning && startedAtMillis != null) {
            (now - startedAtMillis).coerceAtLeast(0L)
        } else {
            0L
        }
        return elapsedMillis + activeDelta
    }

    companion object {
        private const val ActionStartCountdown = "com.kitsuneo.bquick.timer.START_COUNTDOWN"
        private const val ActionStartInterval = "com.kitsuneo.bquick.timer.START_INTERVAL"
        private const val ActionStartReaction = "com.kitsuneo.bquick.timer.START_REACTION"
        private const val ActionStartStopwatch = "com.kitsuneo.bquick.timer.START_STOPWATCH"
        const val ActionToggle = "com.kitsuneo.bquick.timer.TOGGLE"
        const val ActionReset = "com.kitsuneo.bquick.timer.RESET"
        const val ActionStop = "com.kitsuneo.bquick.timer.STOP"
        private const val TimerCompletionAlarmId = 2_000_001

        private const val ExtraPreparationSeconds = "preparationSeconds"
        private const val ExtraWorkSeconds = "workSeconds"
        private const val ExtraRestSeconds = "restSeconds"
        private const val ExtraRounds = "rounds"
        private const val ExtraDurationSeconds = "durationSeconds"
        private const val ExtraMinGapSeconds = "minGapSeconds"
        private const val ExtraMaxGapSeconds = "maxGapSeconds"

        fun startCountdown(
            context: Context,
            durationSeconds: Int
        ) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, TimerForegroundService::class.java).apply {
                    action = ActionStartCountdown
                    putExtra(ExtraDurationSeconds, durationSeconds)
                }
            )
        }

        fun startInterval(
            context: Context,
            preparationSeconds: Int,
            workSeconds: Int,
            restSeconds: Int,
            rounds: Int
        ) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, TimerForegroundService::class.java).apply {
                    action = ActionStartInterval
                    putExtra(ExtraPreparationSeconds, preparationSeconds)
                    putExtra(ExtraWorkSeconds, workSeconds)
                    putExtra(ExtraRestSeconds, restSeconds)
                    putExtra(ExtraRounds, rounds)
                }
            )
        }

        fun startReaction(
            context: Context,
            preparationSeconds: Int,
            durationSeconds: Int,
            minGapSeconds: Int,
            maxGapSeconds: Int
        ) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, TimerForegroundService::class.java).apply {
                    action = ActionStartReaction
                    putExtra(ExtraPreparationSeconds, preparationSeconds)
                    putExtra(ExtraDurationSeconds, durationSeconds)
                    putExtra(ExtraMinGapSeconds, minGapSeconds)
                    putExtra(ExtraMaxGapSeconds, maxGapSeconds)
                }
            )
        }

        fun startStopwatch(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, TimerForegroundService::class.java).apply {
                    action = ActionStartStopwatch
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
