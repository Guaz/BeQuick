package com.kitsuneo.bquick.alarm

import android.content.Context
import com.kitsuneo.bquick.R

data class AlarmTriggerDelta(
    val days: Long,
    val hours: Long,
    val minutes: Long
)

fun AlarmEntry.nextTriggerDelta(nowMillis: Long = System.currentTimeMillis()): AlarmTriggerDelta {
    val durationMillis = (AlarmScheduler.nextTriggerAtMillis(this, nowMillis) - nowMillis).coerceAtLeast(0L)
    val totalMinutes = (durationMillis + 59_999L) / 60_000L
    return AlarmTriggerDelta(
        days = totalMinutes / (24 * 60),
        hours = (totalMinutes % (24 * 60)) / 60,
        minutes = totalMinutes % 60
    )
}

fun AlarmTriggerDelta.format(context: Context): String = listOf(
    context.resources.getQuantityString(R.plurals.duration_days, days.toInt(), days),
    context.resources.getQuantityString(R.plurals.duration_hours, hours.toInt(), hours),
    context.resources.getQuantityString(R.plurals.duration_minutes, minutes.toInt(), minutes)
).joinToString(separator = ", ")
