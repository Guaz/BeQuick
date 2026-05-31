package com.kitsuneo.bquick

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kitsuneo.bquick.feature.home.HomeViewModel
import com.kitsuneo.bquick.feature.interval.IntervalRunningViewModel
import com.kitsuneo.bquick.feature.interval.IntervalSetupViewModel
import com.kitsuneo.bquick.feature.randomsound.RandomSoundRunningViewModel
import com.kitsuneo.bquick.feature.randomsound.RandomSoundSetupViewModel
import com.kitsuneo.bquick.feature.splash.SplashViewModel
import com.kitsuneo.bquick.navigation.AppRoute
import com.kitsuneo.bquick.ui.screen.HomeScreen
import com.kitsuneo.bquick.ui.screen.IntervalRunningScreen
import com.kitsuneo.bquick.ui.screen.IntervalSetupScreen
import com.kitsuneo.bquick.ui.screen.RandomSoundRunningScreen
import com.kitsuneo.bquick.ui.screen.RandomSoundSetupScreen
import com.kitsuneo.bquick.ui.screen.SplashScreen

@Composable
fun BQuickApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

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
                onOpenRandomSound = { navController.navigate(AppRoute.RandomSoundSetup.route) }
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
                    navController.navigate(
                        AppRoute.IntervalRunning.createRoute(
                            workSeconds = state.workSeconds,
                            restSeconds = state.restSeconds,
                            rounds = state.rounds
                        )
                    )
                }
            )
        }

        composable(
            route = AppRoute.IntervalRunning.route,
            arguments = listOf(
                navArgument(AppRoute.IntervalRunning.WorkSecondsArg) { type = NavType.IntType },
                navArgument(AppRoute.IntervalRunning.RestSecondsArg) { type = NavType.IntType },
                navArgument(AppRoute.IntervalRunning.RoundsArg) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val workSeconds =
                backStackEntry.arguments?.getInt(AppRoute.IntervalRunning.WorkSecondsArg) ?: 30
            val restSeconds =
                backStackEntry.arguments?.getInt(AppRoute.IntervalRunning.RestSecondsArg) ?: 15
            val rounds =
                backStackEntry.arguments?.getInt(AppRoute.IntervalRunning.RoundsArg) ?: 6
            val viewModel: IntervalRunningViewModel = viewModel(
                key = "interval-$workSeconds-$restSeconds-$rounds",
                factory = IntervalRunningViewModel.factory(
                    workSeconds = workSeconds,
                    restSeconds = restSeconds,
                    rounds = rounds
                )
            )
            val state by viewModel.state.collectAsState()

            IntervalRunningScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onPauseResume = viewModel::toggleRunning,
                onReset = viewModel::reset
            )
        }

        composable(AppRoute.RandomSoundSetup.route) {
            val viewModel: RandomSoundSetupViewModel = viewModel()
            val state by viewModel.state.collectAsState()

            RandomSoundSetupScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onDurationMinutesChange = viewModel::updateDurationMinutes,
                onMinGapSecondsChange = viewModel::updateMinGapSeconds,
                onMaxGapSecondsChange = viewModel::updateMaxGapSeconds,
                onStart = {
                    navController.navigate(
                        AppRoute.RandomSoundRunning.createRoute(
                            durationMinutes = state.durationMinutes,
                            minGapSeconds = state.minGapSeconds,
                            maxGapSeconds = state.maxGapSeconds
                        )
                    )
                }
            )
        }

        composable(
            route = AppRoute.RandomSoundRunning.route,
            arguments = listOf(
                navArgument(AppRoute.RandomSoundRunning.DurationMinutesArg) { type = NavType.IntType },
                navArgument(AppRoute.RandomSoundRunning.MinGapSecondsArg) { type = NavType.IntType },
                navArgument(AppRoute.RandomSoundRunning.MaxGapSecondsArg) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val durationMinutes =
                backStackEntry.arguments?.getInt(AppRoute.RandomSoundRunning.DurationMinutesArg) ?: 5
            val minGapSeconds =
                backStackEntry.arguments?.getInt(AppRoute.RandomSoundRunning.MinGapSecondsArg) ?: 15
            val maxGapSeconds =
                backStackEntry.arguments?.getInt(AppRoute.RandomSoundRunning.MaxGapSecondsArg) ?: 45
            val viewModel: RandomSoundRunningViewModel = viewModel(
                key = "random-sound-$durationMinutes-$minGapSeconds-$maxGapSeconds",
                factory = RandomSoundRunningViewModel.factory(
                    durationMinutes = durationMinutes,
                    minGapSeconds = minGapSeconds,
                    maxGapSeconds = maxGapSeconds
                )
            )
            val state by viewModel.state.collectAsState()

            RandomSoundRunningScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onPauseResume = viewModel::toggleRunning,
                onReset = viewModel::reset
            )
        }
    }
}
