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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.kitsuneo.bquick.R
import com.kitsuneo.bquick.feature.home.HomeDestination
import com.kitsuneo.bquick.feature.home.HomeUiState
import com.kitsuneo.bquick.settings.displayLabel
import com.kitsuneo.bquick.ui.component.ScreenFrame

@Composable
fun HomeScreen(
    state: HomeUiState,
    onOpenInterval: () -> Unit,
    onOpenRandomSound: () -> Unit,
    onOpenAlarms: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    ScreenFrame(
        title = stringResource(R.string.home_title),
        subtitle = stringResource(R.string.home_subtitle),
        modifier = modifier
    ) {
        HomeFeatureCard(
            eyebrow = stringResource(R.string.home_interval_eyebrow),
            title = stringResource(R.string.home_interval_title),
            description = stringResource(R.string.home_interval_description),
            actionLabel = stringResource(R.string.home_interval_action),
            onClick = onOpenInterval
        )
        HomeFeatureCard(
            eyebrow = stringResource(R.string.home_random_eyebrow),
            title = stringResource(R.string.home_random_title),
            description = stringResource(R.string.home_random_description),
            actionLabel = stringResource(R.string.home_random_action),
            onClick = onOpenRandomSound
        )
        HomeFeatureCard(
            eyebrow = stringResource(R.string.home_alarms_eyebrow),
            title = stringResource(R.string.home_alarms_title),
            description = stringResource(R.string.home_alarms_description),
            actionLabel = stringResource(R.string.home_alarms_action),
            supportingValue = if (state.totalAlarmCount == 0) {
                stringResource(R.string.home_no_alarms)
            } else {
                pluralStringResource(
                    R.plurals.home_enabled_alarms,
                    state.enabledAlarmCount,
                    state.enabledAlarmCount
                )
            },
            onClick = onOpenAlarms
        )

        SettingsEntryCard(
            modeSwitchSoundLabel = state.modeSwitchSound.displayLabel(context),
            reactionSoundLabel = state.reactionSound.displayLabel(context),
            alarmTimeFormatLabel = stringResource(state.alarmTimeFormat.labelRes),
            languageLabel = stringResource(state.appLanguage.labelRes),
            onOpenSettings = onOpenSettings
        )
    }
}

@Composable
private fun HomeFeatureCard(
    eyebrow: String,
    title: String,
    description: String,
    actionLabel: String,
    onClick: () -> Unit,
    supportingValue: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = eyebrow.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            supportingValue?.let { value ->
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onClick) {
                    Text(text = actionLabel)
                }
            }
        }
    }
}

@Composable
private fun SettingsEntryCard(
    modeSwitchSoundLabel: String,
    reactionSoundLabel: String,
    alarmTimeFormatLabel: String,
    languageLabel: String,
    onOpenSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_card_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.settings_mode_switch_sound, modeSwitchSoundLabel),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.settings_reaction_sound, reactionSoundLabel),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.settings_alarm_time_format, alarmTimeFormatLabel),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.settings_language, languageLabel),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onOpenSettings) {
                    Text(text = stringResource(R.string.settings_open))
                }
            }
        }
    }
}
