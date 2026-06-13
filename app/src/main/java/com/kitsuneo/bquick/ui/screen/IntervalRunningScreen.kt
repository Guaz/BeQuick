package com.kitsuneo.bquick.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kitsuneo.bquick.R
import com.kitsuneo.bquick.feature.interval.IntervalRunningUiState
import com.kitsuneo.bquick.timer.IntervalPhase
import com.kitsuneo.bquick.ui.component.MetricPill
import com.kitsuneo.bquick.ui.component.ScreenFrame
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
    val phaseLabel = when (state.currentPhase) {
        IntervalPhase.Work -> stringResource(R.string.phase_work)
        IntervalPhase.Rest -> stringResource(R.string.phase_rest)
        IntervalPhase.Complete -> stringResource(R.string.phase_complete)
    }

    ScreenFrame(
        title = stringResource(R.string.interval_running_title),
        subtitle = stringResource(R.string.interval_running_subtitle, state.currentRound, state.totalRounds),
        modifier = modifier,
        onBack = onBack
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
                IntervalPhase.Work -> stringResource(R.string.interval_work_support)
                IntervalPhase.Rest -> stringResource(R.string.interval_rest_support)
                IntervalPhase.Complete -> stringResource(R.string.interval_complete_support)
            }
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensions.space2),
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
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensions.space1)
        ) {
            Button(
                onClick = onPauseResume,
                modifier = Modifier.weight(1f),
                enabled = !state.isComplete
            ) {
                Text(text = stringResource(if (state.isRunning) R.string.pause else R.string.resume))
            }
            Button(
                onClick = onReset,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(R.string.reset))
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
