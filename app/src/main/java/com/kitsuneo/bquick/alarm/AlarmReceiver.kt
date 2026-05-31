package com.kitsuneo.bquick.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        AlarmRepository.initialize(context)
        val alarm = intent?.getStringExtra(AlarmJsonExtra)?.let(AlarmSerialization::fromJson)
            ?: AlarmRepository.findAlarm(intent?.getIntExtra(AlarmIdExtra, -1) ?: -1)
            ?: return

        val isSnooze = intent?.getBooleanExtra(IsSnoozeExtra, false) == true
        if (!isSnooze && !alarm.enabled) return

        AlarmAlertService.start(context, alarm)

        if (!isSnooze) {
            if (alarm.repeatDays.isEmpty()) {
                AlarmRepository.disableAlarm(alarm.id)
            } else {
                AlarmScheduler.reschedule(context, alarm)
            }
        }
    }

    companion object {
        const val AlarmIdExtra = "alarmId"
        const val AlarmJsonExtra = "alarmJson"
        const val IsSnoozeExtra = "isSnooze"
    }
}
