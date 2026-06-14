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
import com.kitsuneo.bquick.feature.interval.IntervalRunningUiState
import com.kitsuneo.bquick.timer.IntervalPhase
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
fun IntervalRunningScreen(
    state: IntervalRunningUiState,
    onBack: () -> Unit,
    onPauseResume: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = BQuickTheme.dimensions
    val backLabel = stringResource(R.string.back)
    val resetLabel = stringResource(R.string.reset)
    var holdOverlayLabel by remember { mutableStateOf<String?>(null) }
    var holdOverlayProgress by remember { mutableFloatStateOf(0f) }
    val phaseLabel = when (state.currentPhase) {
        IntervalPhase.Preparation -> stringResource(R.string.phase_preparation)
        IntervalPhase.Work -> stringResource(R.string.phase_work)
        IntervalPhase.Rest -> stringResource(R.string.phase_rest)
        IntervalPhase.Complete -> stringResource(R.string.phase_complete)
    }
    val backgroundStyle = when (state.currentPhase) {
        IntervalPhase.Preparation -> SessionBackgroundStyle.Preparation
        IntervalPhase.Work -> SessionBackgroundStyle.Fire
        IntervalPhase.Rest -> SessionBackgroundStyle.Wave
        IntervalPhase.Complete -> SessionBackgroundStyle.Neutral
    }

    ScreenFrame(
        title = stringResource(R.string.interval_running_title),
        subtitle = stringResource(R.string.interval_running_subtitle, state.currentRound, state.totalRounds),
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
            MetricPill(label = stringResource(R.string.interval_phase), value = phaseLabel, modifier = Modifier.weight(1f))
            MetricPill(label = stringResource(R.string.remaining), value = state.remainingProgramSeconds.asClock(), modifier = Modifier.weight(1f))
        }

        StatusCard(
            label = phaseLabel,
            value = state.remainingPhaseSeconds.asClock(),
            supportingText = when (state.currentPhase) {
                IntervalPhase.Preparation -> stringResource(R.string.interval_preparation_support)
                IntervalPhase.Work -> stringResource(R.string.interval_work_support)
                IntervalPhase.Rest -> stringResource(R.string.interval_rest_support)
                IntervalPhase.Complete -> stringResource(R.string.interval_complete_support)
            }
        )

        BQuickCard(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            verticalArrangement = Arrangement.spacedBy(dimensions.space1)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.phase_progress),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                LinearProgressIndicator(
                    progress = { state.phaseProgress },
                    drawStopIndicator = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.session_progress),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                LinearProgressIndicator(
                    progress = { state.sessionProgress },
                    drawStopIndicator = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        BQuickCard(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensions.space1)
            ) {
                BQuickButton(
                    text = stringResource(if (state.isRunning) R.string.pause else R.string.resume),
                    onClick = onPauseResume,
                    modifier = Modifier.weight(1f),
                    isDisabled = state.isComplete
                )
                HoldToConfirmButton(
                    text = resetLabel,
                    onConfirmed = onReset,
                    modifier = Modifier.weight(1f),
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

        if (state.isComplete) {
            Text(
                text = stringResource(R.string.interval_complete_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
