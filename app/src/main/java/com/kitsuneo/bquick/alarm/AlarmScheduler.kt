package com.kitsuneo.bquick.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.kitsuneo.bquick.MainActivity
import java.util.Calendar

object AlarmScheduler {
    fun sync(context: Context, alarms: List<AlarmEntry>) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarms.forEach { alarm ->
            cancel(context, alarm.id, manager)
            if (alarm.enabled) {
                schedule(context, alarm, manager)
            }
        }
    }

    fun reschedule(context: Context, alarm: AlarmEntry) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        cancel(context, alarm.id, manager)
        if (alarm.enabled) {
            schedule(context, alarm, manager)
        }
    }

    fun scheduleSnooze(context: Context, alarm: AlarmEntry, afterMinutes: Int = 5) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!canScheduleExactAlarms(manager)) return
        val pendingIntent = snoozePendingIntent(context, alarm)
        val triggerAtMillis = System.currentTimeMillis() + afterMinutes * 60_000L
        runCatching {
            manager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAtMillis, launchPendingIntent(context, alarm.id + 100_000)),
                pendingIntent
            )
        }
    }

    fun cancel(context: Context, alarmId: Int) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        cancel(context, alarmId, manager)
    }

    fun nextTriggerAtMillis(
        alarm: AlarmEntry,
        fromMillis: Long = System.currentTimeMillis()
    ): Long {
        val base = Calendar.getInstance().apply { timeInMillis = fromMillis }
        val selectedDays = if (alarm.repeatDays.isEmpty()) AlarmWeekday.entries.toSet() else alarm.repeatDays

        return (0..7).asSequence()
            .map { offset ->
                Calendar.getInstance().apply {
                    timeInMillis = fromMillis
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    add(Calendar.DAY_OF_YEAR, offset)
                    set(Calendar.HOUR_OF_DAY, alarm.hour)
                    set(Calendar.MINUTE, alarm.minute)
                }
            }
            .filter { candidate ->
                val weekday = AlarmWeekday.fromCalendarDay(candidate.get(Calendar.DAY_OF_WEEK))
                weekday in selectedDays && candidate.timeInMillis > base.timeInMillis
            }
            .first()
            .timeInMillis
    }

    private fun schedule(context: Context, alarm: AlarmEntry, manager: AlarmManager) {
        if (!canScheduleExactAlarms(manager)) return
        val triggerAtMillis = nextTriggerAtMillis(alarm)
        val operation = pendingIntent(context, alarm.id)
        runCatching {
            manager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAtMillis, launchPendingIntent(context, alarm.id)),
                operation
            )
        }
    }

    private fun canScheduleExactAlarms(manager: AlarmManager): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()
    }

    private fun launchPendingIntent(context: Context, requestCode: Int): PendingIntent {
        return PendingIntent.getActivity(
            context,
            requestCode,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun cancel(context: Context, alarmId: Int, manager: AlarmManager) {
        manager.cancel(pendingIntent(context, alarmId))
        manager.cancel(snoozePendingIntent(context, alarmId))
    }

    private fun pendingIntent(context: Context, alarmId: Int): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            alarmId,
            Intent(context, AlarmReceiver::class.java)
                .putExtra(AlarmReceiver.AlarmIdExtra, alarmId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun snoozePendingIntent(context: Context, alarm: AlarmEntry): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            alarm.id + 50_000,
            Intent(context, AlarmReceiver::class.java)
                .putExtra(AlarmReceiver.AlarmJsonExtra, AlarmSerialization.toJson(alarm))
                .putExtra(AlarmReceiver.IsSnoozeExtra, true),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun snoozePendingIntent(context: Context, alarmId: Int): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            alarmId + 50_000,
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
