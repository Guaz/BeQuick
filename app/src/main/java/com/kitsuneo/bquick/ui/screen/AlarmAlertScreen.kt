package com.kitsuneo.bquick.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.kitsuneo.bquick.R
import com.kitsuneo.bquick.ui.component.BQuickCard
import com.kitsuneo.bquick.ui.component.BQuickButton
import com.kitsuneo.bquick.ui.component.BQuickButtonStyle
import com.kitsuneo.bquick.ui.theme.BQuickTheme

@Composable
fun AlarmAlertScreen(
    title: String,
    timeText: String,
    isSnoozeEnabled: Boolean,
    onSnooze: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = BQuickTheme.dimensions
    val background = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surfaceVariant
        )
    )

    BackHandler(onBack = onClose)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Vertical
                )
            )
            .padding(dimensions.space3),
        contentAlignment = Alignment.Center
    ) {
        BQuickCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            contentPadding = PaddingValues(dimensions.space3),
            verticalArrangement = Arrangement.spacedBy(dimensions.space2),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.alarm_ringing_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Text(
                text = timeText,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            BQuickButton(
                text = stringResource(R.string.snooze),
                onClick = onSnooze,
                isDisabled = !isSnoozeEnabled,
                modifier = Modifier.fillMaxWidth()
            )
            BQuickButton(
                text = stringResource(R.string.close),
                onClick = onClose,
                style = BQuickButtonStyle.Outlined,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
