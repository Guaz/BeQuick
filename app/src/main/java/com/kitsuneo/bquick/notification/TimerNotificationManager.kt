package com.kitsuneo.bquick.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.kitsuneo.bquick.MainActivity
import com.kitsuneo.bquick.R
import com.kitsuneo.bquick.timer.TimerForegroundService

object TimerNotificationManager {
    private const val ChannelId = "active_timer_session"
    const val notificationId = 7001

    fun build(context: Context, state: TimerNotificationState): Notification {
        ensureChannel(context)

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val actionIntent = PendingIntent.getBroadcast(
            context,
            1,
            Intent(context, TimerNotificationActionReceiver::class.java).apply {
                action = TimerForegroundService.ActionToggle
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val actionLabel = if (state.isRunning) "Pause" else "Resume"
        val actionIcon = if (state.isRunning) android.R.drawable.ic_media_pause
        else android.R.drawable.ic_media_play

        return NotificationCompat.Builder(context, ChannelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(state.modeLabel)
            .setContentText(state.timeText)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setShowWhen(false)
            .addAction(actionIcon, actionLabel, actionIntent)
            .build()
    }

    fun show(context: Context, state: TimerNotificationState) {
        if (!canPostNotifications(context)) return
        NotificationManagerCompat.from(context).notify(notificationId, build(context, state))
    }

    fun cancel(context: Context) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            ChannelId,
            "Timer session",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows the currently running timer session."
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun canPostNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
