package com.kitsuneo.bquick.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
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
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope

@Composable
fun BQuickCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
    contentPadding: PaddingValues = PaddingValues(BQuickTheme.dimensions.space4),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = BQuickTheme.shapes.card
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            content = content
        )
    }
}

enum class BQuickButtonStyle {
    Filled,
    Outlined
}

@Composable
fun BQuickButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    isDisabled: Boolean = false,
    style: BQuickButtonStyle = BQuickButtonStyle.Filled,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    containerColor: Color? = null,
    contentColor: Color? = null,
    borderColor: Color? = null
) {
    val resolvedContainerColor = containerColor ?: when {
        style == BQuickButtonStyle.Outlined -> Color.Transparent
        isDisabled -> MaterialTheme.colorScheme.surfaceVariant
        isActive -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val resolvedContentColor = contentColor ?: when {
        isDisabled -> MaterialTheme.colorScheme.onSurfaceVariant
        style == BQuickButtonStyle.Outlined && isActive -> MaterialTheme.colorScheme.primary
        style == BQuickButtonStyle.Outlined -> MaterialTheme.colorScheme.onSurfaceVariant
        isActive -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    val resolvedBorderColor = borderColor ?: when {
        style != BQuickButtonStyle.Outlined -> Color.Transparent
        isDisabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
        isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
    }

    if (style == BQuickButtonStyle.Outlined) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = !isDisabled,
            contentPadding = contentPadding,
            border = BorderStroke(1.dp, resolvedBorderColor),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = resolvedContentColor,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                containerColor = resolvedContainerColor,
                disabledContainerColor = Color.Transparent
            )
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold
            )
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier,
            enabled = !isDisabled,
            contentPadding = contentPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = resolvedContainerColor,
                contentColor = resolvedContentColor,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ScreenFrame(
    modifier: Modifier = Modifier,
    title: String = "",
    subtitle: String = "",
    onBack: (() -> Unit)? = null,
    navigationContent: (@Composable RowScope.() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
    backgroundBrush: Brush? = null,
    backgroundContent: (@Composable BoxScope.() -> Unit)? = null,
    overlayContent: (@Composable BoxScope.() -> Unit)? = null,
    showHeader: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val dimensions = BQuickTheme.dimensions
    val cardColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    val resolvedBackground = backgroundBrush ?: Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surfaceVariant
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(resolvedBackground)
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Vertical
                )
            )
    ) {
        backgroundContent?.invoke(this)

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
                if (navigationContent != null) {
                    navigationContent()
                } else if (onBack != null) {
                    BQuickButton(
                        text = stringResource(R.string.back),
                        onClick = onBack
                    )
                } else {
                    Spacer(modifier = Modifier.height(1.dp))
                }

                if (actions != null) {
                    actions()
                }
            }

            if (showHeader) {
                BQuickCard(
                    containerColor = cardColor,
                    contentPadding = PaddingValues(dimensions.space3),
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

            content()
        }

        overlayContent?.invoke(this)
    }
}

@Composable
fun HoldToConfirmButton(
    text: String,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    holdDurationMillis: Long = 1_200L,
    onHoldStateChange: (Boolean) -> Unit = {},
    onHoldProgressChange: (Float) -> Unit = {}
) {
    val dimensions = BQuickTheme.dimensions
    val shape = RoundedCornerShape(100)
    val containerColor = if (enabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier
            .clip(shape)
            .pointerInput(enabled, holdDurationMillis) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        onHoldStateChange(true)
                        onHoldProgressChange(0f)
                        val startedAt = System.currentTimeMillis()
                        val confirmed = coroutineScope {
                            val release = async { tryAwaitRelease() }
                            var didConfirm = false
                            while (!release.isCompleted) {
                                val elapsed = System.currentTimeMillis() - startedAt
                                val progress = (elapsed / holdDurationMillis.toFloat()).coerceIn(0f, 1f)
                                onHoldProgressChange(progress)
                                if (elapsed >= holdDurationMillis) {
                                    didConfirm = true
                                    onHoldProgressChange(1f)
                                    onConfirmed()
                                    release.cancel()
                                    break
                                }
                                withFrameNanos { }
                            }
                            didConfirm
                        }
                        if (!confirmed) {
                            onHoldProgressChange(0f)
                        }
                        onHoldStateChange(false)
                        onHoldProgressChange(0f)
                    }
                )
            },
        shape = shape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(
            modifier = Modifier.padding(
                horizontal = dimensions.space3,
                vertical = dimensions.space2
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun HoldProgressOverlay(
    progress: Float,
    label: String? = null,
    modifier: Modifier = Modifier
) {
    val dimensions = BQuickTheme.dimensions
    if (progress <= 0f) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.28f)),
        contentAlignment = Alignment.Center
    ) {
        BQuickCard(
            modifier = Modifier.size(180.dp),
            contentPadding = PaddingValues(dimensions.space2),
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            verticalArrangement = Arrangement.spacedBy(dimensions.space1),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(116.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 8.dp
                )
                Icon(
                    painter = painterResource(R.drawable.ic_hold_lock),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(52.dp)
                )
            }
            if (label != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
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

    BQuickCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
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

    BQuickCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
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
            BQuickButton(
                text = "-",
                onClick = { onValueChange((value - 1).coerceAtLeast(minValue)) },
                modifier = Modifier.weight(1f)
            )
            BQuickButton(
                text = "+",
                onClick = { onValueChange((value + 1).coerceAtMost(maxValue)) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TimeAdjustmentButton(
    label: String,
    onClick: () -> Unit
) {
    BQuickButton(
        text = label,
        onClick = onClick
    )
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
    BQuickCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        contentPadding = PaddingValues(dimensions.space3),
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
