package com.kitsuneo.bquick.alarm

import android.content.Context
import androidx.annotation.StringRes
import com.kitsuneo.bquick.R
import com.kitsuneo.bquick.settings.BuiltInSound
import com.kitsuneo.bquick.settings.AlarmTimeFormat
import com.kitsuneo.bquick.settings.SoundSelection
import com.kitsuneo.bquick.settings.formatAlarmTime
import java.util.Calendar

enum class AlarmWeekday(
    val calendarDay: Int,
    @StringRes val shortLabelRes: Int
) {
    Monday(Calendar.MONDAY, R.string.weekday_short_monday),
    Tuesday(Calendar.TUESDAY, R.string.weekday_short_tuesday),
    Wednesday(Calendar.WEDNESDAY, R.string.weekday_short_wednesday),
    Thursday(Calendar.THURSDAY, R.string.weekday_short_thursday),
    Friday(Calendar.FRIDAY, R.string.weekday_short_friday),
    Saturday(Calendar.SATURDAY, R.string.weekday_short_saturday),
    Sunday(Calendar.SUNDAY, R.string.weekday_short_sunday);

    companion object {
        fun fromCalendarDay(day: Int): AlarmWeekday? = entries.firstOrNull { it.calendarDay == day }
    }

    fun shortLabel(context: Context): String = context.getString(shortLabelRes)
}

data class AlarmEntry(
    val id: Int,
    val name: String,
    val hour: Int,
    val minute: Int,
    val repeatDays: Set<AlarmWeekday>,
    val enabled: Boolean,
    val soundSelection: SoundSelection = SoundSelection.BuiltIn(BuiltInSound.WakeUpAnthem),
    val volumePercent: Int = 100,
    val fadeUpEnabled: Boolean = false,
    val vibrateEnabled: Boolean = true,
    val snoozeEnabled: Boolean = true
) {
    fun displayTime(format: AlarmTimeFormat): String = formatAlarmTime(
        hour = hour,
        minute = minute,
        format = format
    )

    fun displayName(context: Context): String = name.ifBlank {
        context.getString(R.string.alarm_default_name)
    }

    fun repeatSummary(context: Context): String = if (repeatDays.isEmpty()) {
        context.getString(R.string.alarm_repeat_once)
    } else {
        repeatDays.sortedBy { it.ordinal }.joinToString(separator = ", ") { it.shortLabel(context) }
    }
}
