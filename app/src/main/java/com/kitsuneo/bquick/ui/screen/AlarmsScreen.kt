package com.kitsuneo.bquick.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kitsuneo.bquick.R
import com.kitsuneo.bquick.alarm.AlarmEntry
import com.kitsuneo.bquick.alarm.AlarmWeekday
import com.kitsuneo.bquick.alarm.format
import com.kitsuneo.bquick.feature.alarm.AlarmDraftUiState
import com.kitsuneo.bquick.feature.alarm.AlarmsUiState
import com.kitsuneo.bquick.settings.displayLabel
import com.kitsuneo.bquick.ui.theme.BQuickTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun AlarmsScreen(
    state: AlarmsUiState,
    onBack: () -> Unit,
    onOpenCreateAlarm: () -> Unit,
    onOpenEditAlarm: (Int) -> Unit,
    onToggleAlarm: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dimensions = BQuickTheme.dimensions
    val background = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surfaceVariant
        )
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Top
                )
            ),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding()
                    .padding(dimensions.space2)
            ) {
                Button(
                    onClick = onOpenCreateAlarm,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.create_new_alarm))
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = dimensions.space2),
            contentPadding = PaddingValues(vertical = dimensions.space2),
            verticalArrangement = Arrangement.spacedBy(dimensions.space2)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = onBack) {
                        Text(text = stringResource(R.string.back))
                    }
                    Spacer(modifier = Modifier.height(1.dp))
                }
            }
            item {
                HeaderCard(
                    title = stringResource(R.string.alarms_title),
                    subtitle = stringResource(
                        R.string.alarms_summary,
                        state.enabledCount,
                        state.alarms.size
                    )
                )
            }
            if (state.alarms.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Text(
                            text = stringResource(R.string.alarms_empty),
                            modifier = Modifier.padding(dimensions.space2),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(state.alarms, key = { it.id }) { alarm ->
                    AlarmListRow(
                        alarm = alarm,
                        timeFormat = state.alarmTimeFormat,
                        context = context,
                        onOpenEdit = { onOpenEditAlarm(alarm.id) },
                        onToggle = { onToggleAlarm(alarm.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AlarmCreateScreen(
    state: AlarmDraftUiState,
    onBack: () -> Unit,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onToggleWeekday: (AlarmWeekday) -> Unit,
    onOpenSoundPicker: () -> Unit,
    onVolumeChange: (Int) -> Unit,
    onFadeUpChange: (Boolean) -> Unit,
    onVibrateChange: (Boolean) -> Unit,
    onSnoozeChange: (Boolean) -> Unit,
    onNameChange: (String) -> Unit,
    onSaveAlarm: () -> Unit,
    onDeleteAlarm: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dimensions = BQuickTheme.dimensions
    val background = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surfaceVariant
        )
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Top
                )
            ),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding()
                    .padding(dimensions.space2)
            ) {
                Button(
                    onClick = onSaveAlarm,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(
                            if (state.editingAlarmId == null) R.string.save_alarm else R.string.update_alarm
                        )
                    )
                }
                if (state.editingAlarmId != null && onDeleteAlarm != null) {
                    TextButton(
                        onClick = onDeleteAlarm,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.remove_alarm),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = dimensions.space2),
            contentPadding = PaddingValues(vertical = dimensions.space2),
            verticalArrangement = Arrangement.spacedBy(dimensions.space2)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = onBack) {
                        Text(text = stringResource(R.string.back))
                    }
                    Spacer(modifier = Modifier.height(1.dp))
                }
            }
            item {
                HeaderCard(
                    title = stringResource(
                        if (state.editingAlarmId == null) R.string.new_alarm else R.string.edit_alarm
                    ),
                    subtitle = stringResource(R.string.alarm_editor_subtitle)
                )
            }
            item {
                TimePickerCard(
                    hour = state.hour,
                    minute = state.minute,
                    onHourChange = onHourChange,
                    onMinuteChange = onMinuteChange
                )
            }
            item {
                InfoCard(
                    text = stringResource(
                        R.string.alarm_in_value,
                        state.triggerDelta().format(context)
                    )
                )
            }
            item {
                RepeatDaysCard(
                    selectedDays = state.repeatDays,
                    onToggleDay = onToggleWeekday
                )
            }
            item {
                SoundPickerCard(
                    currentLabel = state.soundSelection.displayLabel(context),
                    onOpenSoundPicker = onOpenSoundPicker
                )
            }
            item {
                SliderOptionCard(
                    title = stringResource(R.string.alarm_volume),
                    valueLabel = "${state.volumePercent}%",
                    sliderValue = state.volumePercent / 100f,
                    onSliderValueChange = { onVolumeChange((it * 100).toInt()) }
                )
            }
            item {
                ToggleOptionCard(
                    title = stringResource(R.string.fade_up_effect),
                    description = stringResource(R.string.fade_up_description),
                    checked = state.fadeUpEnabled,
                    onCheckedChange = onFadeUpChange
                )
            }
            item {
                ToggleOptionCard(
                    title = stringResource(R.string.vibrate),
                    description = stringResource(R.string.vibrate_description),
                    checked = state.vibrateEnabled,
                    onCheckedChange = onVibrateChange
                )
            }
            item {
                ToggleOptionCard(
                    title = stringResource(R.string.allow_snooze),
                    description = stringResource(R.string.allow_snooze_description),
                    checked = state.snoozeEnabled,
                    onCheckedChange = onSnoozeChange
                )
            }
            item {
                NameCard(
                    name = state.name,
                    onNameChange = onNameChange
                )
            }
        }
    }
}

@Composable
private fun HeaderCard(
    title: String,
    subtitle: String
) {
    val dimensions = BQuickTheme.dimensions
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
        shape = RoundedCornerShape(dimensions.space3)
    ) {
        Column(
            modifier = Modifier.padding(dimensions.space3),
            verticalArrangement = Arrangement.spacedBy(dimensions.space1)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AlarmListRow(
    alarm: AlarmEntry,
    timeFormat: com.kitsuneo.bquick.settings.AlarmTimeFormat,
    context: android.content.Context,
    onOpenEdit: () -> Unit,
    onToggle: () -> Unit
) {
    val dimensions = BQuickTheme.dimensions
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenEdit),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.space2),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensions.space05)
            ) {
                Text(
                    text = alarm.displayName(context),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = alarm.displayTime(timeFormat),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = alarm.repeatSummary(context),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = alarm.enabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
private fun TimePickerCard(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    val dimensions = BQuickTheme.dimensions
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(dimensions.space2),
            verticalArrangement = Arrangement.spacedBy(dimensions.space2)
        ) {
            Text(
                text = stringResource(R.string.time),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensions.space2)
            ) {
                WheelPicker(
                    label = stringResource(R.string.hour),
                    range = 0..23,
                    selected = hour,
                    onSelectedChange = onHourChange,
                    modifier = Modifier.weight(1f)
                )
                WheelPicker(
                    label = stringResource(R.string.minute),
                    range = 0..59,
                    selected = minute,
                    onSelectedChange = onMinuteChange,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun WheelPicker(
    label: String,
    range: IntRange,
    selected: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = BQuickTheme.dimensions
    val values = remember(range) { range.toList() }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selected.coerceIn(range.first, range.last))

    LaunchedEffect(selected) {
        if (listState.firstVisibleItemIndex != selected) {
            listState.animateScrollToItem(selected)
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { index -> values.getOrElse(index) { values.last() } }
            .distinctUntilChanged()
            .collect { value ->
                if (value != selected) {
                    onSelectedChange(value)
                }
            }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensions.space1)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Card(
            shape = RoundedCornerShape(dimensions.space3),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.height(180.dp),
                contentPadding = PaddingValues(vertical = 60.dp),
                verticalArrangement = Arrangement.spacedBy(dimensions.space1)
            ) {
                items(values, key = { it }) { value ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectedChange(value) }
                            .padding(vertical = dimensions.space1),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "%02d".format(value),
                            style = if (value == selected) {
                                MaterialTheme.typography.headlineMedium
                            } else {
                                MaterialTheme.typography.titleLarge
                            },
                            color = if (value == selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = if (value == selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RepeatDaysCard(
    selectedDays: Set<AlarmWeekday>,
    onToggleDay: (AlarmWeekday) -> Unit
) {
    val dimensions = BQuickTheme.dimensions
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(dimensions.space2),
            verticalArrangement = Arrangement.spacedBy(dimensions.space2)
        ) {
            Text(
                text = stringResource(R.string.repeat),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.repeat_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(dimensions.space1)
            ) {
                items(AlarmWeekday.entries, key = { it.name }) { day ->
                    FilterChip(
                        selected = day in selectedDays,
                        onClick = { onToggleDay(day) },
                        label = { Text(day.shortLabel(LocalContext.current)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SoundPickerCard(
    currentLabel: String,
    onOpenSoundPicker: () -> Unit
) {
    val dimensions = BQuickTheme.dimensions
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(dimensions.space2),
            verticalArrangement = Arrangement.spacedBy(dimensions.space2)
        ) {
            Text(
                text = stringResource(R.string.alarm_sound),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
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
            Button(onClick = onOpenSoundPicker) {
                Text(text = stringResource(R.string.choose_sound))
            }
        }
    }
}
}

@Composable
private fun SliderOptionCard(
    title: String,
    valueLabel: String,
    sliderValue: Float,
    onSliderValueChange: (Float) -> Unit
) {
    val dimensions = BQuickTheme.dimensions
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(dimensions.space2),
            verticalArrangement = Arrangement.spacedBy(dimensions.space2)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Slider(
                value = sliderValue,
                onValueChange = onSliderValueChange,
                valueRange = 0f..1f
            )
        }
    }
}

@Composable
private fun ToggleOptionCard(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val dimensions = BQuickTheme.dimensions
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.space2),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimensions.space05)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.padding(dimensions.space1))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun NameCard(
    name: String,
    onNameChange: (String) -> Unit
) {
    val dimensions = BQuickTheme.dimensions
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(dimensions.space2),
            verticalArrangement = Arrangement.spacedBy(dimensions.space2)
        ) {
            Text(
                text = stringResource(R.string.alarm_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.alarm_name_placeholder)) }
            )
        }
    }
}

@Composable
private fun InfoCard(text: String) {
    val dimensions = BQuickTheme.dimensions
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f)),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(dimensions.space2),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold
        )
    }
}
