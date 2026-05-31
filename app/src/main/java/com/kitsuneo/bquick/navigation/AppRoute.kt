package com.kitsuneo.bquick.navigation

sealed class AppRoute(val route: String) {
    data object Splash : AppRoute("splash")
    data object Home : AppRoute("home")
    data object IntervalSetup : AppRoute("interval/setup")

    data object IntervalRunning :
        AppRoute("interval/running/{workSeconds}/{restSeconds}/{rounds}") {
        const val WorkSecondsArg = "workSeconds"
        const val RestSecondsArg = "restSeconds"
        const val RoundsArg = "rounds"

        fun createRoute(workSeconds: Int, restSeconds: Int, rounds: Int): String {
            return "interval/running/$workSeconds/$restSeconds/$rounds"
        }
    }

    data object RandomSoundSetup : AppRoute("random-sound/setup")

    data object RandomSoundRunning :
        AppRoute("random-sound/running/{durationMinutes}/{minGapSeconds}/{maxGapSeconds}") {
        const val DurationMinutesArg = "durationMinutes"
        const val MinGapSecondsArg = "minGapSeconds"
        const val MaxGapSecondsArg = "maxGapSeconds"

        fun createRoute(durationMinutes: Int, minGapSeconds: Int, maxGapSeconds: Int): String {
            return "random-sound/running/$durationMinutes/$minGapSeconds/$maxGapSeconds"
        }
    }
}
