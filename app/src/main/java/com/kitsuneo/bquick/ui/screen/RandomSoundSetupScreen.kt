package com.kitsuneo.bquick.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kitsuneo.bquick.R
import com.kitsuneo.bquick.feature.randomsound.RandomSoundSetupUiState
import com.kitsuneo.bquick.ui.component.BQuickCard
import com.kitsuneo.bquick.ui.component.BQuickButton
import com.kitsuneo.bquick.ui.component.MetricPill
import com.kitsuneo.bquick.ui.component.ScreenFrame
import com.kitsuneo.bquick.ui.component.TimeAdjusterCard
import com.kitsuneo.bquick.ui.theme.BQuickTheme
import com.kitsuneo.bquick.ui.util.asClock

@Composable
fun RandomSoundSetupScreen(
    state: RandomSoundSetupUiState,
    onBack: () -> Unit,
    onPreparationSecondsChange: (Int) -> Unit,
    onDurationSecondsChange: (Int) -> Unit,
    onMinGapSecondsChange: (Int) -> Unit,
    onMaxGapSecondsChange: (Int) -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = BQuickTheme.dimensions
    ScreenFrame(
        title = stringResource(R.string.random_setup_title),
        subtitle = stringResource(R.string.random_setup_subtitle),
        modifier = modifier,
        onBack = onBack
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensions.space1)
        ) {
            MetricPill(label = stringResource(R.string.random_session), value = state.totalSessionSeconds.asClock(), modifier = Modifier.weight(1f))
            MetricPill(
                label = stringResource(R.string.random_cue_range),
                value = "${state.minGapSeconds.asClock()}-${state.maxGapSeconds.asClock()}",
                modifier = Modifier.weight(1f)
            )
        }

        TimeAdjusterCard(
            label = stringResource(R.string.random_preparation_time),
            seconds = state.preparationSeconds,
            helper = stringResource(R.string.random_preparation_helper),
            onSecondsChange = onPreparationSecondsChange,
            minSeconds = 0,
            maxSeconds = 120
        )

        TimeAdjusterCard(
            label = stringResource(R.string.random_duration),
            seconds = state.durationSeconds,
            helper = stringResource(R.string.random_duration_helper),
            onSecondsChange = onDurationSecondsChange,
            minSeconds = 10,
            maxSeconds = 59 * 60 + 59
        )

        TimeAdjusterCard(
            label = stringResource(R.string.random_min_gap),
            seconds = state.minGapSeconds,
            helper = stringResource(R.string.random_min_gap_helper),
            onSecondsChange = onMinGapSecondsChange,
            minSeconds = 1,
            maxSeconds = 59 * 60 + 59
        )

        TimeAdjusterCard(
            label = stringResource(R.string.random_max_gap),
            seconds = state.maxGapSeconds,
            helper = stringResource(R.string.random_max_gap_helper),
            onSecondsChange = onMaxGapSecondsChange,
            minSeconds = state.minGapSeconds,
            maxSeconds = 59 * 60 + 59
        )

        BQuickCard(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                BQuickButton(
                    text = stringResource(R.string.random_start),
                    onClick = onStart
                )
            }
        }
    }
}
