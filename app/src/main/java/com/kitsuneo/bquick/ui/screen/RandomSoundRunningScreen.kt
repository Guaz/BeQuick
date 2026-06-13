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
import androidx.compose.ui.unit.dp
import com.kitsuneo.bquick.R
import com.kitsuneo.bquick.feature.randomsound.RandomSoundRunningUiState
import com.kitsuneo.bquick.ui.component.MetricPill
import com.kitsuneo.bquick.ui.component.ScreenFrame
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
    ScreenFrame(
        title = stringResource(R.string.random_running_title),
        subtitle = stringResource(R.string.random_running_subtitle, state.cueCount + 1),
        modifier = modifier,
        onBack = onBack
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensions.space1)
        ) {
            MetricPill(label = stringResource(R.string.remaining), value = state.remainingSessionSeconds.asClock(), modifier = Modifier.weight(1f))
            MetricPill(label = stringResource(R.string.cues_fired), value = state.cueCount.toString(), modifier = Modifier.weight(1f))
        }

        StatusCard(
            label = stringResource(if (state.isComplete) R.string.phase_complete else R.string.random_next_cue),
            value = if (state.isComplete) stringResource(R.string.done) else "${state.nextCueInSeconds}s",
            supportingText = if (state.isComplete) {
                stringResource(R.string.random_complete_support)
            } else {
                stringResource(R.string.random_running_support)
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
                Text(
                    text = stringResource(R.string.session_progress),
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
    }
}
