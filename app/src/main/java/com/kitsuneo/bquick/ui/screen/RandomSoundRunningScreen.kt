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
import androidx.compose.ui.unit.dp
import com.kitsuneo.bquick.feature.randomsound.RandomSoundRunningUiState
import com.kitsuneo.bquick.ui.component.MetricPill
import com.kitsuneo.bquick.ui.component.ScreenFrame
import com.kitsuneo.bquick.ui.component.StatusCard
import com.kitsuneo.bquick.ui.util.asClock

@Composable
fun RandomSoundRunningScreen(
    state: RandomSoundRunningUiState,
    onBack: () -> Unit,
    onPauseResume: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenFrame(
        title = "Random sound running",
        subtitle = "Cue ${state.cueCount + 1} will fire in a random window.",
        modifier = modifier,
        onBack = onBack
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricPill(label = "Remaining", value = state.remainingSessionSeconds.asClock(), modifier = Modifier.weight(1f))
            MetricPill(label = "Cues fired", value = state.cueCount.toString(), modifier = Modifier.weight(1f))
        }

        StatusCard(
            label = if (state.isComplete) "Complete" else "Next cue",
            value = if (state.isComplete) "Done" else "${state.nextCueInSeconds}s",
            supportingText = if (state.isComplete) {
                "The drill ended. Reset to run it again."
            } else {
                "Stay alert for the next sound trigger."
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
                Text(
                    text = "Session progress",
                    style = MaterialTheme.typography.titleMedium
                )
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier.fillMaxWidth()
                )
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
    }
}
