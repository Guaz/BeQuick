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
import com.kitsuneo.bquick.R
import com.kitsuneo.bquick.feature.randomsound.RandomSoundRunningUiState
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
fun RandomSoundRunningScreen(
    state: RandomSoundRunningUiState,
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
    val backgroundStyle = when {
        state.isComplete -> SessionBackgroundStyle.Neutral
        state.isPreparing -> SessionBackgroundStyle.Preparation
        else -> SessionBackgroundStyle.Fire
    }

    ScreenFrame(
        title = stringResource(R.string.random_running_title),
        subtitle = if (state.isPreparing) {
            stringResource(R.string.random_preparation_subtitle)
        } else {
            stringResource(R.string.random_running_subtitle, state.cueCount + 1)
        },
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
            MetricPill(label = stringResource(R.string.remaining), value = state.remainingTotalSeconds.asClock(), modifier = Modifier.weight(1f))
            MetricPill(label = stringResource(R.string.cues_fired), value = state.cueCount.toString(), modifier = Modifier.weight(1f))
        }

        StatusCard(
            label = stringResource(
                when {
                    state.isComplete -> R.string.phase_complete
                    state.isPreparing -> R.string.phase_preparation
                    else -> R.string.random_next_cue
                }
            ),
            value = when {
                state.isComplete -> stringResource(R.string.done)
                state.isPreparing -> state.remainingPreparationSeconds.asClock()
                else -> state.nextCueInSeconds.asClock()
            },
            supportingText = when {
                state.isComplete -> stringResource(R.string.random_complete_support)
                state.isPreparing -> stringResource(R.string.random_preparation_support)
                else -> stringResource(R.string.random_running_support)
            }
        )

        BQuickCard(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            verticalArrangement = Arrangement.spacedBy(dimensions.space1)
        ) {
            Text(
                text = stringResource(R.string.session_progress),
                style = MaterialTheme.typography.titleMedium
            )
            LinearProgressIndicator(
                progress = { state.progress },
                drawStopIndicator = {},
                modifier = Modifier.fillMaxWidth()
            )
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
    }
}
