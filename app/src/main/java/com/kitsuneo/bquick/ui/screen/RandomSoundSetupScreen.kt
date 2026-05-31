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
import com.kitsuneo.bquick.feature.randomsound.RandomSoundSetupUiState
import com.kitsuneo.bquick.ui.component.AdjusterCard
import com.kitsuneo.bquick.ui.component.MetricPill
import com.kitsuneo.bquick.ui.component.ScreenFrame
import com.kitsuneo.bquick.ui.util.asClock

@Composable
fun RandomSoundSetupScreen(
    state: RandomSoundSetupUiState,
    onBack: () -> Unit,
    onDurationMinutesChange: (Int) -> Unit,
    onMinGapSecondsChange: (Int) -> Unit,
    onMaxGapSecondsChange: (Int) -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenFrame(
        title = "Random sound setup",
        subtitle = "Replicates the BFast random cue generator with configurable cue spacing.",
        modifier = modifier,
        onBack = onBack
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricPill(label = "Session", value = state.totalSessionSeconds.asClock(), modifier = Modifier.weight(1f))
            MetricPill(label = "Cue range", value = "${state.minGapSeconds}-${state.maxGapSeconds}s", modifier = Modifier.weight(1f))
        }

        AdjusterCard(
            label = "Duration",
            value = "${state.durationMinutes} min",
            helper = "How long the drill should keep generating random cues.",
            onDecrease = { onDurationMinutesChange(state.durationMinutes - 1) },
            onIncrease = { onDurationMinutesChange(state.durationMinutes + 1) }
        )

        AdjusterCard(
            label = "Minimum cue gap",
            value = "${state.minGapSeconds}s",
            helper = "Shortest time between consecutive sound cues.",
            onDecrease = { onMinGapSecondsChange(state.minGapSeconds - 3) },
            onIncrease = { onMinGapSecondsChange(state.minGapSeconds + 3) }
        )

        AdjusterCard(
            label = "Maximum cue gap",
            value = "${state.maxGapSeconds}s",
            helper = "Longest wait before the next cue is allowed.",
            onDecrease = { onMaxGapSecondsChange(state.maxGapSeconds - 3) },
            onIncrease = { onMaxGapSecondsChange(state.maxGapSeconds + 3) }
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f)),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(onClick = onStart) {
                    Text(
                        text = "Start random cue session",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
