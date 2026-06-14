package com.kitsuneo.bquick.settings

import android.content.Context
import android.media.ToneGenerator
import androidx.annotation.StringRes
import com.kitsuneo.bquick.R

data class TimerToneStep(
    val toneType: Int,
    val durationMs: Int,
    val pauseAfterMs: Int = 80
)

enum class TimerSignal(
    val id: String,
    @StringRes val labelRes: Int,
    val pattern: List<TimerToneStep>
) {
    ShortBeep(
        id = "short_beep",
        labelRes = R.string.timer_signal_short_beep,
        pattern = listOf(TimerToneStep(ToneGenerator.TONE_PROP_BEEP, 120))
    ),
    DoubleBeep(
        id = "double_beep",
        labelRes = R.string.timer_signal_double_beep,
        pattern = listOf(
            TimerToneStep(ToneGenerator.TONE_PROP_BEEP, 100, 60),
            TimerToneStep(ToneGenerator.TONE_PROP_BEEP, 100)
        )
    ),
    TripleTick(
        id = "triple_tick",
        labelRes = R.string.timer_signal_triple_tick,
        pattern = listOf(
            TimerToneStep(ToneGenerator.TONE_CDMA_PIP, 70, 40),
            TimerToneStep(ToneGenerator.TONE_CDMA_PIP, 70, 40),
            TimerToneStep(ToneGenerator.TONE_CDMA_PIP, 70)
        )
    ),
    BrightPing(
        id = "bright_ping",
        labelRes = R.string.timer_signal_bright_ping,
        pattern = listOf(TimerToneStep(ToneGenerator.TONE_PROP_ACK, 180))
    ),
    LowPulse(
        id = "low_pulse",
        labelRes = R.string.timer_signal_low_pulse,
        pattern = listOf(TimerToneStep(ToneGenerator.TONE_PROP_NACK, 180))
    ),
    ReadyChime(
        id = "ready_chime",
        labelRes = R.string.timer_signal_ready_chime,
        pattern = listOf(
            TimerToneStep(ToneGenerator.TONE_PROP_PROMPT, 170, 70),
            TimerToneStep(ToneGenerator.TONE_PROP_ACK, 200)
        )
    );

    companion object {
        fun fromId(id: String?): TimerSignal? = entries.firstOrNull { it.id == id }
    }
}

enum class IntervalCueMode {
    SecondsBeforeEnd,
    Middle
}

data class IntervalExtraCue(
    val enabled: Boolean = false,
    val mode: IntervalCueMode = IntervalCueMode.SecondsBeforeEnd,
    val secondsBeforeEnd: Int = 3,
    val signal: TimerSignal = TimerSignal.ShortBeep
)

data class TimerSignalSettings(
    val intervalStart: TimerSignal = TimerSignal.ReadyChime,
    val intervalWork: TimerSignal = TimerSignal.ShortBeep,
    val intervalRest: TimerSignal = TimerSignal.DoubleBeep,
    val randomStart: TimerSignal = TimerSignal.ReadyChime,
    val randomBeep: TimerSignal = TimerSignal.BrightPing,
    val intervalExtraCues: List<IntervalExtraCue> = List(MaxIntervalExtraCues) { IntervalExtraCue() }
) {
    val configuredIntervalExtraCueCount: Int
        get() = intervalExtraCues.count { it.enabled }

    fun normalized(): TimerSignalSettings = copy(
        intervalExtraCues = normalizeIntervalExtraCues(intervalExtraCues)
    )

    companion object {
        const val MaxIntervalExtraCues = 5
    }
}

sealed interface TimerSignalPickerTarget {
    val routeArg: String

    data object IntervalStart : TimerSignalPickerTarget { override val routeArg: String = "interval_start" }
    data object IntervalWork : TimerSignalPickerTarget { override val routeArg: String = "interval_work" }
    data object IntervalRest : TimerSignalPickerTarget { override val routeArg: String = "interval_rest" }
    data object RandomStart : TimerSignalPickerTarget { override val routeArg: String = "random_start" }
    data object RandomBeep : TimerSignalPickerTarget { override val routeArg: String = "random_beep" }
    data class IntervalExtraCueTarget(val index: Int) : TimerSignalPickerTarget {
        override val routeArg: String = "interval_extra_$index"
    }

    companion object {
        fun fromRouteArg(arg: String): TimerSignalPickerTarget? = when {
            arg == IntervalStart.routeArg -> IntervalStart
            arg == IntervalWork.routeArg -> IntervalWork
            arg == IntervalRest.routeArg -> IntervalRest
            arg == RandomStart.routeArg -> RandomStart
            arg == RandomBeep.routeArg -> RandomBeep
            arg.startsWith("interval_extra_") -> {
                arg.substringAfterLast('_').toIntOrNull()
                    ?.takeIf { it in 0 until TimerSignalSettings.MaxIntervalExtraCues }
                    ?.let(::IntervalExtraCueTarget)
            }
            else -> null
        }
    }
}

fun TimerSignal.timerLabel(context: Context): String = context.getString(labelRes)

fun TimerSignalPickerTarget.displayTitle(context: Context): String = when (this) {
    TimerSignalPickerTarget.IntervalStart -> context.getString(R.string.settings_interval_start_sound_title)
    TimerSignalPickerTarget.IntervalWork -> context.getString(R.string.settings_interval_work_sound_title)
    TimerSignalPickerTarget.IntervalRest -> context.getString(R.string.settings_interval_rest_sound_title)
    TimerSignalPickerTarget.RandomStart -> context.getString(R.string.settings_random_start_sound_title)
    TimerSignalPickerTarget.RandomBeep -> context.getString(R.string.settings_random_beep_sound_title)
    is TimerSignalPickerTarget.IntervalExtraCueTarget -> context.getString(
        R.string.settings_interval_extra_sound_title,
        index + 1
    )
}

fun TimerSignalSettings.signalFor(target: TimerSignalPickerTarget): TimerSignal = when (target) {
    TimerSignalPickerTarget.IntervalStart -> intervalStart
    TimerSignalPickerTarget.IntervalWork -> intervalWork
    TimerSignalPickerTarget.IntervalRest -> intervalRest
    TimerSignalPickerTarget.RandomStart -> randomStart
    TimerSignalPickerTarget.RandomBeep -> randomBeep
    is TimerSignalPickerTarget.IntervalExtraCueTarget -> intervalExtraCues[target.index].signal
}

fun normalizeIntervalExtraCues(cues: List<IntervalExtraCue>): List<IntervalExtraCue> {
    val normalized = cues
        .take(TimerSignalSettings.MaxIntervalExtraCues)
        .map { cue ->
            cue.copy(secondsBeforeEnd = cue.secondsBeforeEnd.coerceIn(1, 3599))
        }
        .toMutableList()
    while (normalized.size < TimerSignalSettings.MaxIntervalExtraCues) {
        normalized += IntervalExtraCue()
    }
    return normalized
}
