package com.kitsuneo.bquick.alarm

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.kitsuneo.bquick.MainActivity
import com.kitsuneo.bquick.R
import com.kitsuneo.bquick.settings.BuiltInSound
import com.kitsuneo.bquick.settings.SoundSelection
import com.kitsuneo.bquick.settings.SoundSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlarmAlertService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var player: MediaPlayer? = null
    private var fadeJob: Job? = null
    private var currentAlarm: AlarmEntry? = null
    private var previousAlarmVolume: Int? = null
    private var vibrator: Vibrator? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ActionStart -> {
                SoundSettingsRepository.initialize(applicationContext)
                val alarmJson = intent.getStringExtra(AlarmJsonExtra) ?: return START_NOT_STICKY
                val alarm = AlarmSerialization.fromJson(alarmJson)
                currentAlarm = alarm
                startForeground(
                    NotificationId,
                    buildNotification(alarm)
                )
                startAlarm(alarm)
            }

            ActionDismiss -> dismiss()
            ActionSnooze -> snooze()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopAlarmPlayback()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startAlarm(alarm: AlarmEntry) {
        stopAlarmPlayback()
        applyAlarmVolume(alarm)
        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(applicationContext, resolveSoundUri(alarm.soundSelection))
            isLooping = true
            prepare()
            start()
        }
        if (alarm.vibrateEnabled) {
            startVibration()
        }
        if (alarm.fadeUpEnabled) {
            startFadeUp(alarm)
        }
    }

    private fun applyAlarmVolume(alarm: AlarmEntry) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        previousAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        val targetVolume = (maxVolume * (alarm.volumePercent / 100f)).toInt().coerceIn(0, maxVolume)
        val initialVolume = if (alarm.fadeUpEnabled) {
            (maxVolume * 0.1f).toInt().coerceAtLeast(1).coerceAtMost(targetVolume.coerceAtLeast(1))
        } else {
            targetVolume
        }
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, initialVolume, 0)
    }

    private fun startFadeUp(alarm: AlarmEntry) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        val targetVolume = (maxVolume * (alarm.volumePercent / 100f)).toInt().coerceIn(0, maxVolume)
        if (targetVolume <= 0) return
        fadeJob = scope.launch {
            val steps = 10
            repeat(steps) { index ->
                delay(3_000)
                val progress = (index + 1) / steps.toFloat()
                val nextVolume = (targetVolume * progress).toInt().coerceAtLeast(1)
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, nextVolume, 0)
            }
        }
    }

    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        val effect = VibrationEffect.createWaveform(longArrayOf(0, 500, 500), 0)
        vibrator?.vibrate(effect)
    }

    private fun stopAlarmPlayback() {
        fadeJob?.cancel()
        fadeJob = null
        player?.stop()
        player?.release()
        player = null
        vibrator?.cancel()
        vibrator = null
        restoreAlarmVolume()
    }

    private fun restoreAlarmVolume() {
        val previous = previousAlarmVolume ?: return
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, previous, 0)
        previousAlarmVolume = null
    }

    private fun dismiss() {
        stopAlarmPlayback()
        NotificationManagerCompat.from(this).cancel(NotificationId)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun snooze() {
        currentAlarm?.let { alarm ->
            if (alarm.snoozeEnabled) {
                AlarmScheduler.scheduleSnooze(applicationContext, alarm, afterMinutes = 5)
            }
        }
        dismiss()
    }

    private fun buildNotification(alarm: AlarmEntry) = NotificationCompat.Builder(this, ChannelId)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(alarm.displayName(this))
        .setContentText(
            getString(
                R.string.alarm_for_time,
                alarm.displayTime(SoundSettingsRepository.settings.value.alarmTimeFormat)
            )
        )
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setOngoing(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                alarm.id + 90_000,
                Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        .addAction(
            android.R.drawable.ic_media_pause,
            getString(R.string.dismiss),
            PendingIntent.getService(
                this,
                alarm.id + 91_000,
                Intent(this, AlarmAlertService::class.java).apply { action = ActionDismiss },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        .apply {
            if (alarm.snoozeEnabled) {
                addAction(
                    android.R.drawable.ic_lock_idle_alarm,
                    getString(R.string.snooze),
                    PendingIntent.getService(
                        this@AlarmAlertService,
                        alarm.id + 92_000,
                        Intent(this@AlarmAlertService, AlarmAlertService::class.java).apply { action = ActionSnooze },
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
            }
        }
        .build()

    private fun resolveSoundUri(selection: SoundSelection) = when (selection) {
        is SoundSelection.Custom -> android.net.Uri.parse(selection.uri)
        is SoundSelection.BuiltIn -> when (selection.sound) {
            BuiltInSound.Pulse -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            BuiltInSound.Bell -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            BuiltInSound.Chime -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
    }

    companion object {
        private const val ChannelId = "alarm_ringing"
        private const val NotificationId = 84001
        const val ActionStart = "com.kitsuneo.bquick.alarm.START_ALERT"
        const val ActionDismiss = "com.kitsuneo.bquick.alarm.DISMISS_ALERT"
        const val ActionSnooze = "com.kitsuneo.bquick.alarm.SNOOZE_ALERT"
        const val AlarmJsonExtra = "alarmJson"

        fun start(context: Context, alarm: AlarmEntry) {
            AlarmChannelInitializer.ensure(context)
            ContextCompat.startForegroundService(
                context,
                Intent(context, AlarmAlertService::class.java).apply {
                    action = ActionStart
                    putExtra(AlarmJsonExtra, AlarmSerialization.toJson(alarm))
                }
            )
        }
    }
}
