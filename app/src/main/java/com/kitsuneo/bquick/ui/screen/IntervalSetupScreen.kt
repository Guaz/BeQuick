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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kitsuneo.bquick.R
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
        title = stringResource(R.string.interval_setup_title),
        subtitle = stringResource(R.string.interval_setup_subtitle),
        modifier = modifier,
        onBack = onBack
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricPill(label = stringResource(R.string.interval_total_time), value = state.totalDurationSeconds.asClock(), modifier = Modifier.weight(1f))
            MetricPill(label = stringResource(R.string.interval_rounds), value = state.rounds.toString(), modifier = Modifier.weight(1f))
        }

        TimeAdjusterCard(
            label = stringResource(R.string.interval_work_window),
            seconds = state.workSeconds,
            helper = stringResource(R.string.interval_work_helper),
            onSecondsChange = onWorkSecondsChange,
            minSeconds = 0,
            maxSeconds = 180
        )

        TimeAdjusterCard(
            label = stringResource(R.string.interval_rest_window),
            seconds = state.restSeconds,
            helper = stringResource(R.string.interval_rest_helper),
            onSecondsChange = onRestSecondsChange,
            minSeconds = 0,
            maxSeconds = 120
        )

        NumberAdjusterCard(
            label = stringResource(R.string.interval_rounds),
            value = state.rounds,
            helper = stringResource(R.string.interval_rounds_helper),
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
                        text = stringResource(R.string.interval_start),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
