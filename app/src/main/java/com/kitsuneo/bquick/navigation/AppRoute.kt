package com.kitsuneo.bquick.navigation

sealed class AppRoute(val route: String) {
    data object Splash : AppRoute("splash")
    data object Home : AppRoute("home")
    data object AlarmSoundPicker : AppRoute("alarms/sound")
    data object Alarms : AppRoute("alarms")
    data object AlarmCreate : AppRoute("alarms/create")
    data object AlarmEdit : AppRoute("alarms/edit/{alarmId}") {
        const val AlarmIdArg = "alarmId"

        fun createRoute(alarmId: Int): String = "alarms/edit/$alarmId"
    }
    data object Settings : AppRoute("settings")
    data object SettingsSoundPicker : AppRoute("settings/sound/{target}") {
        const val TargetArg = "target"

        fun createRoute(target: String): String = "settings/sound/$target"
    }
    data object SettingsTimerAlarmSoundPicker : AppRoute("settings/timer-alarm-sound")
    data object SettingsAlarmSoundPicker : AppRoute("settings/alarm-sound")
    data object TimerSetup : AppRoute("timer/setup")
    data object TimerRunning : AppRoute("timer/running")
    data object StopwatchRunning : AppRoute("stopwatch/running")
    data object IntervalSetup : AppRoute("interval/setup")
    data object IntervalRunning : AppRoute("interval/running")

    data object RandomSoundSetup : AppRoute("random-sound/setup")
    data object RandomSoundRunning : AppRoute("random-sound/running")
}
