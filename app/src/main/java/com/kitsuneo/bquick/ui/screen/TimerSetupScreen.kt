package com.kitsuneo.bquick.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kitsuneo.bquick.R
import com.kitsuneo.bquick.feature.timer.TimerSetupUiState
import com.kitsuneo.bquick.ui.component.BQuickCard
import com.kitsuneo.bquick.ui.component.BQuickButton
import com.kitsuneo.bquick.ui.component.MetricPill
import com.kitsuneo.bquick.ui.component.ScreenFrame
import com.kitsuneo.bquick.ui.component.TimeAdjusterCard
import com.kitsuneo.bquick.ui.theme.BQuickTheme
import com.kitsuneo.bquick.ui.util.asClock

@Composable
fun TimerSetupScreen(
    state: TimerSetupUiState,
    onBack: () -> Unit,
    onDurationSecondsChange: (Int) -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = BQuickTheme.dimensions

    ScreenFrame(
        title = stringResource(R.string.timer_setup_title),
        subtitle = stringResource(R.string.timer_setup_subtitle),
        modifier = modifier,
        onBack = onBack
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensions.space1)
        ) {
            MetricPill(
                label = stringResource(R.string.timer_duration),
                value = state.durationSeconds.asClock(),
                modifier = Modifier.weight(1f)
            )
        }

        TimeAdjusterCard(
            label = stringResource(R.string.timer_duration),
            seconds = state.durationSeconds,
            helper = stringResource(R.string.timer_duration_helper),
            onSecondsChange = onDurationSecondsChange,
            minSeconds = 1,
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
                    text = stringResource(R.string.timer_start),
                    onClick = onStart
                )
            }
        }
    }
}
