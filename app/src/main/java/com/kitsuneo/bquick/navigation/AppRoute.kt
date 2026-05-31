package com.kitsuneo.bquick.navigation

sealed class AppRoute(val route: String) {
    data object Splash : AppRoute("splash")
    data object Home : AppRoute("home")
    data object Alarms : AppRoute("alarms")
    data object AlarmCreate : AppRoute("alarms/create")
    data object AlarmEdit : AppRoute("alarms/edit/{alarmId}") {
        const val AlarmIdArg = "alarmId"

        fun createRoute(alarmId: Int): String = "alarms/edit/$alarmId"
    }
    data object Settings : AppRoute("settings")
    data object IntervalSetup : AppRoute("interval/setup")
    data object IntervalRunning : AppRoute("interval/running")

    data object RandomSoundSetup : AppRoute("random-sound/setup")
    data object RandomSoundRunning : AppRoute("random-sound/running")
}
