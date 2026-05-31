package com.kitsuneo.bquick.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kitsuneo.bquick.feature.interval.IntervalSetupUiState
import com.kitsuneo.bquick.ui.component.MetricPill
import com.kitsuneo.bquick.ui.component.NumberAdjusterCard
import com.kitsuneo.bquick.ui.component.ScreenFrame
import com.kitsuneo.bquick.ui.component.TimeAdjusterCard
import com.kitsuneo.bquick.ui.util.asClock

@Composable
fun IntervalSetupScreen(
    state: IntervalSetupUiState,
    onBack: () -> Unit,
    onWorkSecondsChange: (Int) -> Unit,
    onRestSecondsChange: (Int) -> Unit,
    onRoundsChange: (Int) -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val startEnabled = state.workSeconds > 0 || state.restSeconds > 0

    ScreenFrame(
        title = "Interval setup",
        subtitle = "Replicates the BFast interval flow, now as a real Compose + MVVM timer.",
        modifier = modifier,
        onBack = onBack
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricPill(label = "Total time", value = state.totalDurationSeconds.asClock(), modifier = Modifier.weight(1f))
            MetricPill(label = "Rounds", value = state.rounds.toString(), modifier = Modifier.weight(1f))
        }

        TimeAdjusterCard(
            label = "Work window",
            seconds = state.workSeconds,
            helper = "The active part of each round.",
            onSecondsChange = onWorkSecondsChange,
            minSeconds = 0,
            maxSeconds = 180
        )

        TimeAdjusterCard(
            label = "Rest window",
            seconds = state.restSeconds,
            helper = "Recovery time between work rounds.",
            onSecondsChange = onRestSecondsChange,
            minSeconds = 0,
            maxSeconds = 120
        )

        NumberAdjusterCard(
            label = "Rounds",
            value = state.rounds,
            helper = "How many work blocks the session should run.",
            onValueChange = onRoundsChange,
            minValue = 1,
            maxValue = 20
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f)),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onStart,
                    enabled = startEnabled
                ) {
                    Text(
                        text = "Start interval session",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
