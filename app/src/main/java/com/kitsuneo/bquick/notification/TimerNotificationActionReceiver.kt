package com.kitsuneo.bquick.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kitsuneo.bquick.timer.TimerForegroundService

class TimerNotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == TimerForegroundService.ActionToggle) {
            TimerForegroundService.sendAction(context, action)
        }
    }
}
