package com.kitsuneo.bquick.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kitsuneo.bquick.R
import com.kitsuneo.bquick.ui.theme.BQuickTheme
import com.kitsuneo.bquick.ui.util.asClock
import com.kitsuneo.bquick.ui.util.toClockSecondsOrNull

@Composable
fun ScreenFrame(
    modifier: Modifier = Modifier,
    title: String = "",
    subtitle: String = "",
    onBack: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
    showHeader: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val dimensions = BQuickTheme.dimensions
    val cardColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    val background = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surfaceVariant
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Vertical
                )
            )
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimensions.space2, vertical = dimensions.space2),
            verticalArrangement = Arrangement.spacedBy(dimensions.space3)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    Button(onClick = onBack) {
                        Text(text = stringResource(R.string.back))
                    }
                } else {
                    Spacer(modifier = Modifier.height(1.dp))
                }

                if (actions != null) {
                    actions()
                }
            }

            if (showHeader) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardColor),
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
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            content()
        }
    }
}

@Composable
fun TimeAdjusterCard(
    label: String,
    seconds: Int,
    helper: String,
    onSecondsChange: (Int) -> Unit,
    minSeconds: Int,
    maxSeconds: Int,
    modifier: Modifier = Modifier
) {
    val dimensions = BQuickTheme.dimensions
    var textValue by rememberSaveable { mutableStateOf(seconds.asClock()) }

    LaunchedEffect(seconds) {
        val formatted = seconds.asClock()
        if (textValue != formatted) {
            textValue = formatted
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
        shape = RoundedCornerShape(dimensions.space3)
    ) {
        Column(
            modifier = Modifier.padding(dimensions.space2),
            verticalArrangement = Arrangement.spacedBy(dimensions.space2)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = textValue,
                onValueChange = { input ->
                    val parsed = input.toClockSecondsOrNull() ?: return@OutlinedTextField
                    val clamped = parsed.coerceIn(minSeconds, maxSeconds)
                    val formatted = clamped.asClock()
                    textValue = formatted
                    if (clamped != seconds) {
                        onSecondsChange(clamped)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = {
                    Text(
                        text = helper,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensions.space1)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensions.space1)
                ) {
                    TimeAdjustmentButton(
                        label = "-10s",
                        onClick = { onSecondsChange((seconds - 10).coerceAtLeast(minSeconds)) }
                    )
                    TimeAdjustmentButton(
                        label = "-5s",
                        onClick = { onSecondsChange((seconds - 5).coerceAtLeast(minSeconds)) }
                    )
                    TimeAdjustmentButton(
                        label = "-1s",
                        onClick = { onSecondsChange((seconds - 1).coerceAtLeast(minSeconds)) }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensions.space1)
                ) {
                    TimeAdjustmentButton(
                        label = "+1s",
                        onClick = { onSecondsChange((seconds + 1).coerceAtMost(maxSeconds)) }
                    )
                    TimeAdjustmentButton(
                        label = "+5s",
                        onClick = { onSecondsChange((seconds + 5).coerceAtMost(maxSeconds)) }
                    )
                    TimeAdjustmentButton(
                        label = "+10s",
                        onClick = { onSecondsChange((seconds + 10).coerceAtMost(maxSeconds)) }
                    )
                }
            }
        }
    }
}

@Composable
fun NumberAdjusterCard(
    label: String,
    value: Int,
    helper: String,
    onValueChange: (Int) -> Unit,
    minValue: Int,
    maxValue: Int,
    modifier: Modifier = Modifier
) {
    val dimensions = BQuickTheme.dimensions
    var textValue by rememberSaveable { mutableStateOf(value.toString()) }

    LaunchedEffect(value) {
        val formatted = value.toString()
        if (textValue != formatted) {
            textValue = formatted
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
        shape = RoundedCornerShape(dimensions.space3)
    ) {
        Column(
            modifier = Modifier.padding(dimensions.space2),
            verticalArrangement = Arrangement.spacedBy(dimensions.space2)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = textValue,
                onValueChange = { input ->
                    val digits = input.filter(Char::isDigit).take(2)
                    textValue = digits
                    val parsed = digits.toIntOrNull() ?: return@OutlinedTextField
                    val clamped = parsed.coerceIn(minValue, maxValue)
                    if (clamped != value) {
                        onValueChange(clamped)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = {
                    Text(
                        text = helper,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimensions.space2)
            ) {
                Button(
                    onClick = { onValueChange((value - 1).coerceAtLeast(minValue)) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "-")
                }
                Button(
                    onClick = { onValueChange((value + 1).coerceAtMost(maxValue)) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "+")
                }
            }
        }
    }
}

@Composable
private fun TimeAdjustmentButton(
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick
    ) {
        Text(text = label)
    }
}

@Composable
fun MetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val dimensions = BQuickTheme.dimensions
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(dimensions.space2)),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = dimensions.space2, vertical = dimensions.space2),
            verticalArrangement = Arrangement.spacedBy(dimensions.space05)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun StatusCard(
    label: String,
    value: String,
    supportingText: String,
    modifier: Modifier = Modifier
) {
    val dimensions = BQuickTheme.dimensions
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        shape = RoundedCornerShape(dimensions.space3)
    ) {
        Column(
            modifier = Modifier.padding(dimensions.space3),
            verticalArrangement = Arrangement.spacedBy(dimensions.space1),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = value,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
