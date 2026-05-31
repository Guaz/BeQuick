package com.kitsuneo.bquick.settings

import androidx.annotation.StringRes
import com.kitsuneo.bquick.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class AlarmTimeFormat(@StringRes val labelRes: Int) {
    Hours24(R.string.alarm_time_format_24h),
    Hours12(R.string.alarm_time_format_12h)
}

fun formatAlarmTime(
    hour: Int,
    minute: Int,
    format: AlarmTimeFormat
): String = when (format) {
    AlarmTimeFormat.Hours24 -> formatWithPattern(hour, minute, "HH:mm")
    AlarmTimeFormat.Hours12 -> formatWithPattern(hour, minute, "h:mm a")
}

private fun formatWithPattern(hour: Int, minute: Int, pattern: String): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return SimpleDateFormat(pattern, Locale.getDefault()).format(calendar.time)
}
