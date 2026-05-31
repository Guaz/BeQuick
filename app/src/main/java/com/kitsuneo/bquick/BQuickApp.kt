package com.kitsuneo.bquick

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kitsuneo.bquick.feature.home.HomeViewModel
import com.kitsuneo.bquick.feature.interval.IntervalRunningViewModel
import com.kitsuneo.bquick.feature.interval.IntervalSetupViewModel
import com.kitsuneo.bquick.feature.randomsound.RandomSoundRunningViewModel
import com.kitsuneo.bquick.feature.randomsound.RandomSoundSetupViewModel
import com.kitsuneo.bquick.feature.splash.SplashViewModel
import com.kitsuneo.bquick.navigation.AppRoute
import com.kitsuneo.bquick.notification.NotificationPermissionEffect
import com.kitsuneo.bquick.settings.BuiltInSound
import com.kitsuneo.bquick.settings.SoundTarget
import com.kitsuneo.bquick.timer.TimerForegroundService
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
                onOpenSettings = { navController.navigate(AppRoute.Settings.route) }
            )
        }

        composable(AppRoute.Settings.route) {
            val viewModel: HomeViewModel = viewModel()
            val state by viewModel.state.collectAsState()

            SettingsScreen(
                state = state,
                onBack = { navController.popBackStack() },
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
