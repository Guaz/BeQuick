package com.kitsuneo.bquick.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.kitsuneo.bquick.R
import com.kitsuneo.bquick.feature.timer.TimerRunningUiState
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
import com.kitsuneo.bquick.ui.util.asClock

@Composable
fun TimerRunningScreen(
    state: TimerRunningUiState,
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
    val backgroundStyle = when {
        state.isComplete -> SessionBackgroundStyle.Neutral
        state.isRunning -> SessionBackgroundStyle.Wave
        else -> SessionBackgroundStyle.Preparation
    }

    ScreenFrame(
        title = stringResource(R.string.timer_running_title),
        subtitle = stringResource(R.string.timer_running_subtitle),
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
        backgroundContent = {
            AnimatedSessionBackground(style = backgroundStyle, modifier = Modifier.fillMaxSize())
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
                label = stringResource(R.string.remaining),
                value = state.remainingSeconds.asClock(),
                modifier = Modifier.weight(1f)
            )
            MetricPill(
                label = stringResource(R.string.elapsed),
                value = state.elapsedSeconds.asClock(),
                modifier = Modifier.weight(1f)
            )
        }

        StatusCard(
            label = stringResource(R.string.home_timer_title),
            value = if (state.isComplete) stringResource(R.string.done) else state.remainingSeconds.asClock(),
            supportingText = when {
                state.isComplete -> stringResource(R.string.timer_complete_support)
                state.isRunning -> stringResource(R.string.timer_running_support)
                else -> stringResource(R.string.timer_paused_support)
            }
        )

        BQuickCard(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            verticalArrangement = Arrangement.spacedBy(dimensions.space1)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.session_progress),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                LinearProgressIndicator(
                    progress = { state.progress },
                    drawStopIndicator = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

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
                modifier = Modifier.weight(1f),
                isDisabled = state.isComplete
            )
            HoldToConfirmButton(
                text = resetLabel,
                onConfirmed = onReset,
                modifier = Modifier.weight(1f),
                enabled = state.hasActiveSession,
                onHoldStateChange = { isHolding ->
                    holdOverlayLabel = if (isHolding) resetLabel else null
                },
                onHoldProgressChange = { progress ->
                    holdOverlayLabel = if (progress > 0f) resetLabel else null
                    holdOverlayProgress = progress
                }
            )
        }

        if (state.isComplete) {
            Text(
                text = stringResource(R.string.timer_complete_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
