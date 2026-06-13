package com.kitsuneo.bquick.alarm

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.kitsuneo.bquick.R
import com.kitsuneo.bquick.settings.SoundSettingsRepository
import com.kitsuneo.bquick.ui.screen.AlarmAlertScreen
import com.kitsuneo.bquick.ui.theme.BQuickTheme

class AlarmAlertActivity : ComponentActivity() {
    private var alertState by mutableStateOf(AlarmAlertUiState())
    private var isCloseReceiverRegistered = false

    private val closeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        SoundSettingsRepository.applyStoredAppLanguage(this)
        super.onCreate(savedInstanceState)
        SoundSettingsRepository.initialize(applicationContext)
        configureWindow()
        enableEdgeToEdge()
        alertState = intent.toAlertUiState()
        setContent {
            BQuickTheme {
                AlarmAlertScreen(
                    title = alertState.title,
                    timeText = alertState.timeText,
                    isSnoozeEnabled = alertState.isSnoozeEnabled,
                    onSnooze = {
                        AlarmAlertService.sendAction(this, AlarmAlertService.ActionSnooze)
                        finish()
                    },
                    onClose = {
                        AlarmAlertService.sendAction(this, AlarmAlertService.ActionDismiss)
                        finish()
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        alertState = intent.toAlertUiState()
    }

    override fun onStart() {
        super.onStart()
        if (!isCloseReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(AlarmAlertService.ActionAlertDismissed)
                addAction(AlarmAlertService.ActionAlertSnoozed)
            }
            ContextCompat.registerReceiver(
                this,
                closeReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            isCloseReceiverRegistered = true
        }
    }

    override fun onStop() {
        if (isCloseReceiverRegistered) {
            unregisterReceiver(closeReceiver)
            isCloseReceiverRegistered = false
        }
        super.onStop()
    }

    private fun configureWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun Intent.toAlertUiState(): AlarmAlertUiState = AlarmAlertUiState(
        title = getStringExtra(ExtraAlarmTitle).orEmpty().ifBlank {
            getString(R.string.alarm_default_name)
        },
        timeText = getStringExtra(ExtraAlarmTimeText).orEmpty(),
        isSnoozeEnabled = getBooleanExtra(ExtraAlarmSnoozeEnabled, true)
    )

    companion object {
        private const val ExtraAlarmTitle = "extra_alarm_title"
        private const val ExtraAlarmTimeText = "extra_alarm_time_text"
        private const val ExtraAlarmSnoozeEnabled = "extra_alarm_snooze_enabled"

        fun createIntent(context: Context, alarm: AlarmEntry, timeText: String) =
            Intent(context, AlarmAlertActivity::class.java).apply {
                putExtra(ExtraAlarmTitle, alarm.displayName(context))
                putExtra(ExtraAlarmTimeText, timeText)
                putExtra(ExtraAlarmSnoozeEnabled, alarm.snoozeEnabled)
            }

        fun createPendingIntent(context: Context, alarm: AlarmEntry, timeText: String): PendingIntent =
            PendingIntent.getActivity(
                context,
                alarm.id + 90_000,
                createIntent(context, alarm, timeText).apply {
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
    }
}

private data class AlarmAlertUiState(
    val title: String = "",
    val timeText: String = "",
    val isSnoozeEnabled: Boolean = true
)
