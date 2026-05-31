package com.kitsuneo.bquick

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kitsuneo.bquick.R
import com.kitsuneo.bquick.alarm.format
import com.kitsuneo.bquick.alarm.nextTriggerDelta
import com.kitsuneo.bquick.feature.alarm.AlarmsViewModel
import com.kitsuneo.bquick.feature.home.HomeViewModel
import com.kitsuneo.bquick.feature.interval.IntervalRunningViewModel
import com.kitsuneo.bquick.feature.interval.IntervalSetupViewModel
import com.kitsuneo.bquick.feature.randomsound.RandomSoundRunningViewModel
import com.kitsuneo.bquick.feature.randomsound.RandomSoundSetupViewModel
import com.kitsuneo.bquick.feature.splash.SplashViewModel
import com.kitsuneo.bquick.navigation.AppRoute
import com.kitsuneo.bquick.notification.NotificationPermissionEffect
import com.kitsuneo.bquick.timer.TimerForegroundService
import com.kitsuneo.bquick.ui.screen.AlarmsScreen
import com.kitsuneo.bquick.ui.screen.AlarmCreateScreen
import com.kitsuneo.bquick.ui.screen.HomeScreen
import com.kitsuneo.bquick.ui.screen.IntervalRunningScreen
import com.kitsuneo.bquick.ui.screen.IntervalSetupScreen
import com.kitsuneo.bquick.ui.screen.RandomSoundRunningScreen
import com.kitsuneo.bquick.ui.screen.RandomSoundSetupScreen
import com.kitsuneo.bquick.ui.screen.SettingsScreen
import com.kitsuneo.bquick.ui.screen.SplashScreen

@Composable
fun BQuickApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val context = LocalContext.current
    NotificationPermissionEffect()

    NavHost(
        navController = navController,
        startDestination = AppRoute.Splash.route,
        modifier = modifier
    ) {
        composable(AppRoute.Splash.route) {
            val viewModel: SplashViewModel = viewModel()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(state.isReadyToContinue) {
                if (state.isReadyToContinue) {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(AppRoute.Splash.route) {
                            inclusive = true
                        }
                    }
                }
            }

            SplashScreen(state = state)
        }

        composable(AppRoute.Home.route) {
            val viewModel: HomeViewModel = viewModel()
            val state by viewModel.state.collectAsState()

            HomeScreen(
                state = state,
                onOpenInterval = { navController.navigate(AppRoute.IntervalSetup.route) },
                onOpenRandomSound = { navController.navigate(AppRoute.RandomSoundSetup.route) },
                onOpenAlarms = { navController.navigate(AppRoute.Alarms.route) },
                onOpenSettings = { navController.navigate(AppRoute.Settings.route) }
            )
        }

        composable(AppRoute.Alarms.route) {
            val viewModel: AlarmsViewModel = viewModel()
            val state by viewModel.state.collectAsState()

            AlarmsScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onOpenCreateAlarm = { navController.navigate(AppRoute.AlarmCreate.route) },
                onOpenEditAlarm = { alarmId ->
                    navController.navigate(AppRoute.AlarmEdit.createRoute(alarmId))
                },
                onToggleAlarm = { alarmId ->
                    viewModel.toggleAlarm(alarmId)?.let { alarm ->
                        Toast.makeText(
                            context,
                            context.getString(
                                R.string.alarm_will_trigger_in_value,
                                alarm.nextTriggerDelta().format(context)
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }

        composable(AppRoute.AlarmCreate.route) {
            val viewModel: AlarmsViewModel = viewModel()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.resetDraftForNewAlarm()
            }

            AlarmCreateScreen(
                state = state.draft,
                onBack = { navController.popBackStack() },
                onHourChange = viewModel::updateDraftHour,
                onMinuteChange = viewModel::updateDraftMinute,
                onToggleWeekday = viewModel::toggleDraftWeekday,
                onSelectBuiltInSound = viewModel::selectBuiltInSound,
                onSelectCustomSound = viewModel::selectCustomSound,
                onVolumeChange = viewModel::updateVolumePercent,
                onFadeUpChange = viewModel::updateFadeUpEnabled,
                onVibrateChange = viewModel::updateVibrateEnabled,
                onSnoozeChange = viewModel::updateSnoozeEnabled,
                onNameChange = viewModel::updateName,
                onSaveAlarm = {
                    viewModel.saveAlarm()?.let { alarm ->
                        Toast.makeText(
                            context,
                            context.getString(
                                R.string.alarm_will_trigger_in_value,
                                alarm.nextTriggerDelta().format(context)
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    navController.popBackStack()
                },
                onDeleteAlarm = null
            )
        }

        composable(
            route = AppRoute.AlarmEdit.route,
            arguments = listOf(
                navArgument(AppRoute.AlarmEdit.AlarmIdArg) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val alarmId =
                backStackEntry.arguments?.getInt(AppRoute.AlarmEdit.AlarmIdArg) ?: return@composable
            val viewModel: AlarmsViewModel = viewModel()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(alarmId) {
                viewModel.loadAlarmForEdit(alarmId)
            }

            AlarmCreateScreen(
                state = state.draft,
                onBack = { navController.popBackStack() },
                onHourChange = viewModel::updateDraftHour,
                onMinuteChange = viewModel::updateDraftMinute,
                onToggleWeekday = viewModel::toggleDraftWeekday,
                onSelectBuiltInSound = viewModel::selectBuiltInSound,
                onSelectCustomSound = viewModel::selectCustomSound,
                onVolumeChange = viewModel::updateVolumePercent,
                onFadeUpChange = viewModel::updateFadeUpEnabled,
                onVibrateChange = viewModel::updateVibrateEnabled,
                onSnoozeChange = viewModel::updateSnoozeEnabled,
                onNameChange = viewModel::updateName,
                onSaveAlarm = {
                    viewModel.saveAlarm()?.let { alarm ->
                        Toast.makeText(
                            context,
                            context.getString(
                                R.string.alarm_will_trigger_in_value,
                                alarm.nextTriggerDelta().format(context)
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    navController.popBackStack()
                },
                onDeleteAlarm = {
                    viewModel.deleteAlarm()
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoute.Settings.route) {
            val viewModel: HomeViewModel = viewModel()
            val state by viewModel.state.collectAsState()

            SettingsScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onAppLanguageChange = viewModel::updateAppLanguage,
                onAlarmTimeFormatChange = viewModel::updateAlarmTimeFormat,
                onSelectBuiltInSound = viewModel::selectBuiltInSound,
                onSelectCustomSound = viewModel::selectCustomSound
            )
        }

        composable(AppRoute.IntervalSetup.route) {
            val viewModel: IntervalSetupViewModel = viewModel()
            val state by viewModel.state.collectAsState()

            IntervalSetupScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onWorkSecondsChange = viewModel::updateWorkSeconds,
                onRestSecondsChange = viewModel::updateRestSeconds,
                onRoundsChange = viewModel::updateRounds,
                onStart = {
                    TimerForegroundService.startInterval(
                        context = context,
                        workSeconds = state.workSeconds,
                        restSeconds = state.restSeconds,
                        rounds = state.rounds
                    )
                    navController.navigate(AppRoute.IntervalRunning.route)
                }
            )
        }

        composable(AppRoute.IntervalRunning.route) {
            val viewModel: IntervalRunningViewModel = viewModel()
            val state by viewModel.state.collectAsState()

            IntervalRunningScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onPauseResume = { viewModel.toggleRunning(context) },
                onReset = { viewModel.reset(context) }
            )
        }

        composable(AppRoute.RandomSoundSetup.route) {
            val viewModel: RandomSoundSetupViewModel = viewModel()
            val state by viewModel.state.collectAsState()

            RandomSoundSetupScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onDurationSecondsChange = viewModel::updateDurationSeconds,
                onMinGapSecondsChange = viewModel::updateMinGapSeconds,
                onMaxGapSecondsChange = viewModel::updateMaxGapSeconds,
                onStart = {
                    TimerForegroundService.startReaction(
                        context = context,
                        durationSeconds = state.durationSeconds,
                        minGapSeconds = state.minGapSeconds,
                        maxGapSeconds = state.maxGapSeconds
                    )
                    navController.navigate(AppRoute.RandomSoundRunning.route)
                }
            )
        }

        composable(AppRoute.RandomSoundRunning.route) {
            val viewModel: RandomSoundRunningViewModel = viewModel()
            val state by viewModel.state.collectAsState()

            RandomSoundRunningScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onPauseResume = { viewModel.toggleRunning(context) },
                onReset = { viewModel.reset(context) }
            )
        }
    }
}
