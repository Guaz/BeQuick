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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kitsuneo.bquick.feature.interval.IntervalPhase
import com.kitsuneo.bquick.feature.interval.IntervalRunningUiState
import com.kitsuneo.bquick.ui.component.MetricPill
import com.kitsuneo.bquick.ui.component.ScreenFrame
import com.kitsuneo.bquick.ui.component.StatusCard
import com.kitsuneo.bquick.ui.util.asClock

@Composable
fun IntervalRunningScreen(
    state: IntervalRunningUiState,
    onBack: () -> Unit,
    onPauseResume: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val phaseLabel = when (state.currentPhase) {
        IntervalPhase.Work -> "Work"
        IntervalPhase.Rest -> "Rest"
        IntervalPhase.Complete -> "Complete"
    }

    ScreenFrame(
        title = "Interval running",
        subtitle = "Round ${state.currentRound} of ${state.totalRounds}",
        modifier = modifier,
        onBack = onBack
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricPill(label = "Phase", value = phaseLabel, modifier = Modifier.weight(1f))
            MetricPill(label = "Remaining", value = state.remainingProgramSeconds.asClock(), modifier = Modifier.weight(1f))
        }

        StatusCard(
            label = phaseLabel,
            value = state.remainingPhaseSeconds.asClock(),
            supportingText = when (state.currentPhase) {
                IntervalPhase.Work -> "Push through the active interval."
                IntervalPhase.Rest -> "Recover before the next round."
                IntervalPhase.Complete -> "Session complete."
            }
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Phase progress",
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
                        text = "Session progress",
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
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onPauseResume,
                modifier = Modifier.weight(1f),
                enabled = !state.isComplete
            ) {
                Text(text = if (state.isRunning) "Pause" else "Resume")
            }
            Button(
                onClick = onReset,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Reset")
            }
        }

        if (state.isComplete) {
            Text(
                text = "All rounds are done. Use Back to return or Reset to run the session again.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
