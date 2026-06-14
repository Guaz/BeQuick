package com.kitsuneo.bquick.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.kitsuneo.bquick.R
import com.kitsuneo.bquick.audio.TimerSignalPlayer
import com.kitsuneo.bquick.settings.TimerSignal
import com.kitsuneo.bquick.settings.timerLabel
import com.kitsuneo.bquick.ui.component.BQuickCard
import com.kitsuneo.bquick.ui.component.BQuickButton
import com.kitsuneo.bquick.ui.component.ScreenFrame
import com.kitsuneo.bquick.ui.theme.BQuickTheme

@Composable
fun TimerSignalPickerScreen(
    title: String,
    currentSignal: TimerSignal,
    onBack: () -> Unit,
    onSelectSignal: (TimerSignal) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        onDispose { TimerSignalPlayer.release() }
    }

    ScreenFrame(
        title = title,
        subtitle = stringResource(R.string.timer_signal_picker_subtitle),
        modifier = modifier,
        onBack = onBack
    ) {
        SignalSectionCard(
            title = stringResource(R.string.timer_signal_picker_title)
        ) {
            TimerSignal.entries.forEach { signal ->
                TimerSignalRow(
                    label = signal.timerLabel(context),
                    isSelected = signal == currentSignal,
                    onPlay = { TimerSignalPlayer.play(context, signal) },
                    onSelect = { onSelectSignal(signal) }
                )
            }
        }
    }
}

@Composable
private fun SignalSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val dimensions = BQuickTheme.dimensions
    BQuickCard(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensions.space1 + dimensions.space05)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        content()
    }
}

@Composable
private fun TimerSignalRow(
    label: String,
    isSelected: Boolean,
    onPlay: () -> Unit,
    onSelect: () -> Unit
) {
    val dimensions = BQuickTheme.dimensions
    BQuickCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensions.space1 + dimensions.space05),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BQuickButton(
                text = stringResource(R.string.play),
                onClick = onPlay
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )
        }
    }
}
