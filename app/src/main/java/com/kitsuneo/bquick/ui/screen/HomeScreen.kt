package com.kitsuneo.bquick.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.kitsuneo.bquick.R
import com.kitsuneo.bquick.feature.home.HomeDestination
import com.kitsuneo.bquick.feature.home.HomeUiState
import com.kitsuneo.bquick.ui.component.BQuickCard
import com.kitsuneo.bquick.ui.component.BQuickButton
import com.kitsuneo.bquick.ui.component.ScreenFrame
import com.kitsuneo.bquick.ui.theme.BQuickTheme

@Composable
fun HomeScreen(
    state: HomeUiState,
    onOpenTimer: () -> Unit,
    onOpenStopwatch: () -> Unit,
    onOpenInterval: () -> Unit,
    onOpenRandomSound: () -> Unit,
    onOpenAlarms: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenFrame(
        showHeader = false,
        modifier = modifier
    ) {
        state.homeOrder.forEach { destination ->
            when (destination) {
                HomeDestination.Interval -> {
                    HomeFeatureCard(
                        eyebrow = stringResource(R.string.home_interval_eyebrow),
                        title = stringResource(R.string.home_interval_title),
                        description = stringResource(R.string.home_interval_description),
                        actionLabel = stringResource(R.string.home_interval_action),
                        onClick = onOpenInterval
                    )
                }

                HomeDestination.RandomSound -> {
                    HomeFeatureCard(
                        eyebrow = stringResource(R.string.home_random_eyebrow),
                        title = stringResource(R.string.home_random_title),
                        description = stringResource(R.string.home_random_description),
                        actionLabel = stringResource(R.string.home_random_action),
                        onClick = onOpenRandomSound
                    )
                }

                HomeDestination.Timer -> {
                    HomeFeatureCard(
                        eyebrow = stringResource(R.string.home_timer_eyebrow),
                        title = stringResource(R.string.home_timer_title),
                        description = stringResource(R.string.home_timer_description),
                        actionLabel = stringResource(R.string.home_timer_action),
                        onClick = onOpenTimer
                    )
                }

                HomeDestination.Stopwatch -> {
                    HomeFeatureCard(
                        eyebrow = stringResource(R.string.home_stopwatch_eyebrow),
                        title = stringResource(R.string.home_stopwatch_title),
                        description = stringResource(R.string.home_stopwatch_description),
                        actionLabel = stringResource(R.string.home_stopwatch_action),
                        onClick = onOpenStopwatch
                    )
                }

                HomeDestination.Alarms -> {
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
                }
            }
        }
        HomeFeatureCard(
            eyebrow = stringResource(R.string.home_settings_eyebrow),
            title = stringResource(R.string.settings_title),
            description = stringResource(R.string.home_settings_description),
            actionLabel = stringResource(R.string.home_settings_action),
            onClick = onOpenSettings
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
    val dimensions = BQuickTheme.dimensions
    BQuickCard(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensions.space2)
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
            BQuickButton(
                text = actionLabel,
                onClick = onClick
            )
        }
    }
}
