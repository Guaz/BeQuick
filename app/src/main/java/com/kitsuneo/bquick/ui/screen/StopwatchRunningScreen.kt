package com.kitsuneo.bquick.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kitsuneo.bquick.R
import com.kitsuneo.bquick.feature.stopwatch.StopwatchRunningUiState
import com.kitsuneo.bquick.ui.component.AnimatedSessionBackground
import com.kitsuneo.bquick.ui.component.BQuickCard
import com.kitsuneo.bquick.ui.component.BQuickButton
import com.kitsuneo.bquick.ui.component.HoldProgressOverlay
import com.kitsuneo.bquick.ui.component.HoldToConfirmButton
import com.kitsuneo.bquick.ui.component.MetricPill
import com.kitsuneo.bquick.ui.component.ScreenFrame
import com.kitsuneo.bquick.ui.component.SessionBackgroundStyle
import com.kitsuneo.bquick.ui.component.StatusCard
import com.kitsuneo.bquick.ui.theme.BQuickTheme
import com.kitsuneo.bquick.ui.util.asStopwatch
import kotlinx.coroutines.delay

@Composable
fun StopwatchRunningScreen(
    state: StopwatchRunningUiState,
    onBack: () -> Unit,
    onPrimaryAction: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = BQuickTheme.dimensions
    val backLabel = stringResource(R.string.back)
    val resetLabel = stringResource(R.string.reset)
    var holdOverlayLabel by remember { mutableStateOf<String?>(null) }
    var holdOverlayProgress by remember { mutableFloatStateOf(0f) }
    val displayedElapsedMillis by produceState(
        initialValue = state.elapsedMillis,
        key1 = state.elapsedMillis,
        key2 = state.startedAtMillis,
        key3 = state.isRunning
    ) {
        while (true) {
            value = if (state.isRunning && state.startedAtMillis != null) {
                state.elapsedMillis + (System.currentTimeMillis() - state.startedAtMillis).coerceAtLeast(0L)
            } else {
                state.elapsedMillis
            }
            if (!state.isRunning) break
            delay(16L)
        }
    }

    ScreenFrame(
        title = stringResource(R.string.stopwatch_running_title),
        subtitle = stringResource(R.string.stopwatch_running_subtitle),
        modifier = modifier,
        navigationContent = {
            if (state.isRunning) {
                HoldToConfirmButton(
                    text = backLabel,
                    onConfirmed = onBack,
                    onHoldStateChange = { isHolding ->
                        holdOverlayLabel = if (isHolding) backLabel else null
                    },
                    onHoldProgressChange = { progress ->
                        holdOverlayLabel = if (progress > 0f) backLabel else null
                        holdOverlayProgress = progress
                    }
                )
            } else {
                BQuickButton(
                    text = backLabel,
                    onClick = onBack
                )
            }
        },
        backgroundContent = if (state.isRunning) {
            {
                AnimatedSessionBackground(
                    style = SessionBackgroundStyle.Fire,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            null
        },
        overlayContent = {
            HoldProgressOverlay(
                progress = holdOverlayProgress,
                label = holdOverlayLabel
            )
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensions.space1)
        ) {
            MetricPill(
                label = stringResource(R.string.elapsed),
                value = displayedElapsedMillis.asStopwatch(),
                modifier = Modifier.weight(1f)
            )
            MetricPill(
                label = stringResource(R.string.status),
                value = stringResource(
                    when {
                        !state.hasActiveSession -> R.string.stopwatch_status_ready
                        state.isRunning -> R.string.stopwatch_status_running
                        else -> R.string.stopwatch_status_paused
                    }
                ),
                modifier = Modifier.weight(1f)
            )
        }

        StatusCard(
            label = stringResource(R.string.home_stopwatch_title),
            value = displayedElapsedMillis.asStopwatch(),
            supportingText = stringResource(
                when {
                    !state.hasActiveSession -> R.string.stopwatch_ready_support
                    state.isRunning -> R.string.stopwatch_running_support
                    else -> R.string.stopwatch_paused_support
                }
            )
        )

        BQuickCard(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensions.space1)
            ) {
                BQuickButton(
                    text = stringResource(
                        when {
                            !state.hasActiveSession -> R.string.start
                            state.isRunning -> R.string.pause
                            else -> R.string.resume
                        }
                    ),
                    onClick = onPrimaryAction,
                    modifier = Modifier.weight(1f)
                )
                HoldToConfirmButton(
                    text = resetLabel,
                    onConfirmed = onReset,
                    modifier = Modifier.weight(1f),
                    enabled = state.hasActiveSession || displayedElapsedMillis > 0L,
                    onHoldStateChange = { isHolding ->
                        holdOverlayLabel = if (isHolding) resetLabel else null
                    },
                    onHoldProgressChange = { progress ->
                        holdOverlayLabel = if (progress > 0f) resetLabel else null
                        holdOverlayProgress = progress
                    }
                )
            }
        }
    }
}
