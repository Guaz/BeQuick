package com.kitsuneo.bquick.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.kitsuneo.bquick.R
import com.kitsuneo.bquick.feature.home.HomeDestination
import com.kitsuneo.bquick.feature.home.HomeUiState
import com.kitsuneo.bquick.settings.AlarmTimeFormat
import com.kitsuneo.bquick.settings.AppLanguage
import com.kitsuneo.bquick.settings.IntervalCueMode
import com.kitsuneo.bquick.settings.TimerSignalPickerTarget
import com.kitsuneo.bquick.settings.displayLabel
import com.kitsuneo.bquick.settings.timerLabel
import com.kitsuneo.bquick.ui.component.BQuickCard
import com.kitsuneo.bquick.ui.component.BQuickButton
import com.kitsuneo.bquick.ui.component.BQuickButtonStyle
import com.kitsuneo.bquick.ui.component.ScreenFrame
import com.kitsuneo.bquick.ui.theme.BQuickTheme

@Composable
fun SettingsScreen(
    state: HomeUiState,
    onBack: () -> Unit,
    onAppLanguageChange: (AppLanguage) -> Unit,
    onAlarmTimeFormatChange: (AlarmTimeFormat) -> Unit,
    onHomeOrderChange: (List<HomeDestination>) -> Unit,
    onOpenTimerSignalPicker: (TimerSignalPickerTarget) -> Unit,
    onOpenTimerAlarmSoundPicker: () -> Unit,
    onOpenAlarmSoundPicker: () -> Unit,
    onIntervalExtraCueEnabledChange: (Int, Boolean) -> Unit,
    onIntervalExtraCueModeChange: (Int, IntervalCueMode) -> Unit,
    onIntervalExtraCueSecondsChange: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenFrame(
        title = stringResource(R.string.settings_title),
        subtitle = stringResource(R.string.settings_subtitle),
        modifier = modifier,
        onBack = onBack
    ) {
        AppLanguageCard(
            selectedLanguage = state.appLanguage,
            onLanguageChange = onAppLanguageChange
        )
        AlarmTimeFormatCard(
            selectedFormat = state.alarmTimeFormat,
            onFormatChange = onAlarmTimeFormatChange
        )
        HomeMenuOrderCard(
            order = state.homeOrder,
            onOrderChange = onHomeOrderChange
        )
        SoundSettingsCard(
            state = state,
            onOpenTimerSignalPicker = onOpenTimerSignalPicker,
            onOpenTimerAlarmSoundPicker = onOpenTimerAlarmSoundPicker,
            onOpenAlarmSoundPicker = onOpenAlarmSoundPicker,
            onIntervalExtraCueEnabledChange = onIntervalExtraCueEnabledChange,
            onIntervalExtraCueModeChange = onIntervalExtraCueModeChange,
            onIntervalExtraCueSecondsChange = onIntervalExtraCueSecondsChange
        )
    }
}

@Composable
private fun AppLanguageCard(
    selectedLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit
) {
    val dimensions = BQuickTheme.dimensions
    BQuickCard(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensions.space1)
    ) {
        Text(
            text = stringResource(R.string.settings_language_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        AppLanguage.entries.forEach { language ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.space1)
            ) {
                RadioButton(
                    selected = selectedLanguage == language,
                    onClick = { onLanguageChange(language) }
                )
                Text(
                    text = stringResource(language.labelRes),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AlarmTimeFormatCard(
    selectedFormat: AlarmTimeFormat,
    onFormatChange: (AlarmTimeFormat) -> Unit
) {
    val dimensions = BQuickTheme.dimensions
    BQuickCard(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensions.space1)
    ) {
        Text(
            text = stringResource(R.string.settings_alarm_time_format_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        AlarmTimeFormat.entries.forEach { format ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.space1)
            ) {
                RadioButton(
                    selected = selectedFormat == format,
                    onClick = { onFormatChange(format) }
                )
                Text(
                    text = stringResource(format.labelRes),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HomeMenuOrderCard(
    order: List<HomeDestination>,
    onOrderChange: (List<HomeDestination>) -> Unit
) {
    val dimensions = BQuickTheme.dimensions
    val moveUpDescription = stringResource(R.string.settings_home_order_move_up)
    val moveDownDescription = stringResource(R.string.settings_home_order_move_down)

    BQuickCard(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensions.space1)
    ) {
        Text(
            text = stringResource(R.string.settings_home_order_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.settings_home_order_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        order.forEachIndexed { index, destination ->
            BQuickCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.space1)
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(dimensions.space05)
                    ) {
                        Text(
                            text = homeDestinationTitle(destination),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = homeDestinationSubtitle(destination),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimensions.space05)
                    ) {
                        BQuickButton(
                            text = "˄",
                            onClick = {
                                onOrderChange(order.moveItem(fromIndex = index, toIndex = index - 1))
                            },
                            isDisabled = index <= 0,
                            modifier = Modifier.semantics {
                                contentDescription = moveUpDescription
                            }
                        )
                        BQuickButton(
                            text = "˅",
                            onClick = {
                                onOrderChange(order.moveItem(fromIndex = index, toIndex = index + 1))
                            },
                            isDisabled = index >= order.lastIndex,
                            modifier = Modifier.semantics {
                                contentDescription = moveDownDescription
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun List<HomeDestination>.moveItem(fromIndex: Int, toIndex: Int): List<HomeDestination> {
    if (fromIndex == toIndex || fromIndex !in indices || toIndex !in indices) return this
    return toMutableList().apply {
        val item = removeAt(fromIndex)
        add(toIndex, item)
    }
}

@Composable
private fun homeDestinationTitle(destination: HomeDestination): String = when (destination) {
    HomeDestination.Interval -> stringResource(R.string.home_interval_title)
    HomeDestination.RandomSound -> stringResource(R.string.home_random_title)
    HomeDestination.Timer -> stringResource(R.string.home_timer_title)
    HomeDestination.Stopwatch -> stringResource(R.string.home_stopwatch_title)
    HomeDestination.Alarms -> stringResource(R.string.home_alarms_title)
}

@Composable
private fun homeDestinationSubtitle(destination: HomeDestination): String = when (destination) {
    HomeDestination.Interval -> stringResource(R.string.home_interval_eyebrow)
    HomeDestination.RandomSound -> stringResource(R.string.home_random_eyebrow)
    HomeDestination.Timer -> stringResource(R.string.home_timer_eyebrow)
    HomeDestination.Stopwatch -> stringResource(R.string.home_stopwatch_eyebrow)
    HomeDestination.Alarms -> stringResource(R.string.home_alarms_eyebrow)
}

@Composable
private fun SoundSettingsCard(
    state: HomeUiState,
    onOpenTimerSignalPicker: (TimerSignalPickerTarget) -> Unit,
    onOpenTimerAlarmSoundPicker: () -> Unit,
    onOpenAlarmSoundPicker: () -> Unit,
    onIntervalExtraCueEnabledChange: (Int, Boolean) -> Unit,
    onIntervalExtraCueModeChange: (Int, IntervalCueMode) -> Unit,
    onIntervalExtraCueSecondsChange: (Int, Int) -> Unit
) {
    val context = LocalContext.current
    val dimensions = BQuickTheme.dimensions
    BQuickCard(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensions.space2)
    ) {
        Text(
            text = stringResource(R.string.settings_timer_sounds_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.settings_timer_sounds_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TimerSignalSection(
            title = stringResource(R.string.settings_interval_start_sound_title),
            currentLabel = state.timerSignals.intervalStart.timerLabel(context),
            onOpenPicker = { onOpenTimerSignalPicker(TimerSignalPickerTarget.IntervalStart) }
        )
        TimerSignalSection(
            title = stringResource(R.string.settings_interval_work_sound_title),
            currentLabel = state.timerSignals.intervalWork.timerLabel(context),
            onOpenPicker = { onOpenTimerSignalPicker(TimerSignalPickerTarget.IntervalWork) }
        )
        TimerSignalSection(
            title = stringResource(R.string.settings_interval_rest_sound_title),
            currentLabel = state.timerSignals.intervalRest.timerLabel(context),
            onOpenPicker = { onOpenTimerSignalPicker(TimerSignalPickerTarget.IntervalRest) }
        )
        AlarmSoundSection(
            title = stringResource(R.string.settings_timer_alarm_sound_title),
            currentLabel = state.timerAlarmSound.displayLabel(context),
            onOpenPicker = onOpenTimerAlarmSoundPicker
        )
        Text(
            text = stringResource(R.string.settings_interval_extra_sounds_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.settings_interval_extra_sounds_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        state.timerSignals.intervalExtraCues.forEachIndexed { index, cue ->
            IntervalExtraCueRow(
                index = index,
                cueLabel = cue.signal.timerLabel(context),
                enabled = cue.enabled,
                mode = cue.mode,
                secondsBeforeEnd = cue.secondsBeforeEnd,
                onEnabledChange = { onIntervalExtraCueEnabledChange(index, it) },
                onModeChange = { onIntervalExtraCueModeChange(index, it) },
                onSecondsBeforeEndChange = { onIntervalExtraCueSecondsChange(index, it) },
                onOpenPicker = {
                    onOpenTimerSignalPicker(TimerSignalPickerTarget.IntervalExtraCueTarget(index))
                }
            )
        }
    }

    BQuickCard(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensions.space2)
    ) {
        Text(
            text = stringResource(R.string.settings_random_sounds_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.settings_random_sounds_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TimerSignalSection(
            title = stringResource(R.string.settings_random_start_sound_title),
            currentLabel = state.timerSignals.randomStart.timerLabel(context),
            onOpenPicker = { onOpenTimerSignalPicker(TimerSignalPickerTarget.RandomStart) }
        )
        TimerSignalSection(
            title = stringResource(R.string.settings_random_beep_sound_title),
            currentLabel = state.timerSignals.randomBeep.timerLabel(context),
            onOpenPicker = { onOpenTimerSignalPicker(TimerSignalPickerTarget.RandomBeep) }
        )
    }

    BQuickCard(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensions.space2)
    ) {
        Text(
            text = stringResource(R.string.settings_alarm_sounds_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.settings_alarm_sounds_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        AlarmSoundSection(
            title = stringResource(R.string.settings_default_alarm_sound_title),
            currentLabel = state.defaultAlarmSound.displayLabel(context),
            onOpenPicker = onOpenAlarmSoundPicker
        )
    }
}

@Composable
private fun TimerSignalSection(
    title: String,
    currentLabel: String,
    onOpenPicker: () -> Unit
) {
    val dimensions = BQuickTheme.dimensions
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensions.space1)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.current_value, currentLabel),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            BQuickButton(
                text = stringResource(R.string.choose_sound),
                onClick = onOpenPicker
            )
        }
    }
}

@Composable
private fun AlarmSoundSection(
    title: String,
    currentLabel: String,
    onOpenPicker: () -> Unit
) {
    val dimensions = BQuickTheme.dimensions
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensions.space1)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.current_value, currentLabel),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            BQuickButton(
                text = stringResource(R.string.choose_sound),
                onClick = onOpenPicker
            )
        }
    }
}

@Composable
private fun IntervalExtraCueRow(
    index: Int,
    cueLabel: String,
    enabled: Boolean,
    mode: IntervalCueMode,
    secondsBeforeEnd: Int,
    onEnabledChange: (Boolean) -> Unit,
    onModeChange: (IntervalCueMode) -> Unit,
    onSecondsBeforeEndChange: (Int) -> Unit,
    onOpenPicker: () -> Unit
) {
    val dimensions = BQuickTheme.dimensions
    var secondsText by rememberSaveable(index, mode) { mutableStateOf(secondsBeforeEnd.toString()) }

    LaunchedEffect(secondsBeforeEnd, mode) {
        if (mode == IntervalCueMode.SecondsBeforeEnd && secondsText != secondsBeforeEnd.toString()) {
            secondsText = secondsBeforeEnd.toString()
        }
    }

    BQuickCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        verticalArrangement = Arrangement.spacedBy(dimensions.space1)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensions.space1)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimensions.space05)
            ) {
                Text(
                    text = stringResource(R.string.settings_interval_extra_sound_title, index + 1),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = cueLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange
            )
        }

        if (enabled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                BQuickButton(
                    text = stringResource(R.string.choose_sound),
                    onClick = onOpenPicker
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensions.space1)
            ) {
                ModeOptionButton(
                    text = stringResource(R.string.settings_interval_extra_before_end),
                    isActive = mode == IntervalCueMode.SecondsBeforeEnd,
                    onClick = { onModeChange(IntervalCueMode.SecondsBeforeEnd) },
                    modifier = Modifier.weight(1f)
                )
                ModeOptionButton(
                    text = stringResource(R.string.settings_interval_extra_middle),
                    isActive = mode == IntervalCueMode.Middle,
                    onClick = { onModeChange(IntervalCueMode.Middle) },
                    modifier = Modifier.weight(1f)
                )
            }
            if (mode == IntervalCueMode.SecondsBeforeEnd) {
                OutlinedTextField(
                    value = secondsText,
                    onValueChange = { input ->
                        val filtered = input.filter(Char::isDigit).take(4)
                        secondsText = filtered
                        filtered.toIntOrNull()
                            ?.takeIf { it > 0 }
                            ?.let(onSecondsBeforeEndChange)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.settings_interval_extra_before_end_field)) },
                    placeholder = { Text("20") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text(stringResource(R.string.settings_interval_extra_seconds_suffix)) }
                )
            } else {
                Text(
                    text = stringResource(R.string.settings_interval_extra_middle_value),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ModeOptionButton(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BQuickButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        isActive = isActive,
        style = if (isActive) BQuickButtonStyle.Filled else BQuickButtonStyle.Outlined
    )
}
