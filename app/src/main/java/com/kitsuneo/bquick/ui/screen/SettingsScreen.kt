package com.kitsuneo.bquick.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.kitsuneo.bquick.R
import com.kitsuneo.bquick.feature.home.HomeDestination
import com.kitsuneo.bquick.feature.home.HomeUiState
import com.kitsuneo.bquick.settings.AlarmTimeFormat
import com.kitsuneo.bquick.settings.AppLanguage
import com.kitsuneo.bquick.settings.SoundTarget
import com.kitsuneo.bquick.settings.displayLabel
import com.kitsuneo.bquick.ui.component.ScreenFrame
import com.kitsuneo.bquick.ui.theme.BQuickTheme

@Composable
fun SettingsScreen(
    state: HomeUiState,
    onBack: () -> Unit,
    onAppLanguageChange: (AppLanguage) -> Unit,
    onAlarmTimeFormatChange: (AlarmTimeFormat) -> Unit,
    onHomeOrderChange: (List<HomeDestination>) -> Unit,
    onOpenSoundPicker: (SoundTarget) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
            modeSwitchSoundLabel = state.modeSwitchSound.displayLabel(context),
            reactionSoundLabel = state.reactionSound.displayLabel(context),
            onOpenSoundPicker = onOpenSoundPicker
        )
    }
}

@Composable
private fun AppLanguageCard(
    selectedLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit
) {
    val dimensions = BQuickTheme.dimensions
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(dimensions.space2),
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
}

@Composable
private fun AlarmTimeFormatCard(
    selectedFormat: AlarmTimeFormat,
    onFormatChange: (AlarmTimeFormat) -> Unit
) {
    val dimensions = BQuickTheme.dimensions
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(dimensions.space2),
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
}

@Composable
private fun HomeMenuOrderCard(
    order: List<HomeDestination>,
    onOrderChange: (List<HomeDestination>) -> Unit
) {
    val dimensions = BQuickTheme.dimensions
    val moveUpDescription = stringResource(R.string.settings_home_order_move_up)
    val moveDownDescription = stringResource(R.string.settings_home_order_move_down)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(dimensions.space2),
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensions.space2),
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
                            Button(
                                onClick = {
                                    onOrderChange(order.moveItem(fromIndex = index, toIndex = index - 1))
                                },
                                enabled = index > 0,
                                modifier = Modifier.semantics {
                                    contentDescription = moveUpDescription
                                }
                            ) {
                                Text(text = "˄")
                            }
                            Button(
                                onClick = {
                                    onOrderChange(order.moveItem(fromIndex = index, toIndex = index + 1))
                                },
                                enabled = index < order.lastIndex,
                                modifier = Modifier.semantics {
                                    contentDescription = moveDownDescription
                                }
                            ) {
                                Text(text = "˅")
                            }
                        }
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
    HomeDestination.Alarms -> stringResource(R.string.home_alarms_title)
}

@Composable
private fun homeDestinationSubtitle(destination: HomeDestination): String = when (destination) {
    HomeDestination.Interval -> stringResource(R.string.home_interval_eyebrow)
    HomeDestination.RandomSound -> stringResource(R.string.home_random_eyebrow)
    HomeDestination.Alarms -> stringResource(R.string.home_alarms_eyebrow)
}

@Composable
private fun SoundSettingsCard(
    modeSwitchSoundLabel: String,
    reactionSoundLabel: String,
    onOpenSoundPicker: (SoundTarget) -> Unit
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
            SoundTargetSection(
                title = stringResource(R.string.settings_mode_switch_sound_title),
                currentLabel = modeSwitchSoundLabel,
                target = SoundTarget.ModeSwitch,
                onOpenSoundPicker = onOpenSoundPicker
            )
            SoundTargetSection(
                title = stringResource(R.string.settings_reaction_sound_title),
                currentLabel = reactionSoundLabel,
                target = SoundTarget.Reaction,
                onOpenSoundPicker = onOpenSoundPicker
            )
        }
    }
}

@Composable
private fun SoundTargetSection(
    title: String,
    currentLabel: String,
    target: SoundTarget,
    onOpenSoundPicker: (SoundTarget) -> Unit
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
            Button(onClick = { onOpenSoundPicker(target) }) {
                Text(text = stringResource(R.string.choose_sound))
            }
        }
    }
}
