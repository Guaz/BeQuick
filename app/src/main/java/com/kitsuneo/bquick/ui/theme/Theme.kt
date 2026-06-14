package com.kitsuneo.bquick.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = SignalBlue,
    onPrimary = Mist,
    primaryContainer = SignalBlueDeep,
    onPrimaryContainer = Mist,
    secondary = LimeFlash,
    onSecondary = Mist,
    background = Midnight,
    onBackground = Mist,
    surface = Slate,
    onSurface = Mist,
    surfaceVariant = SurfaceLift,
    onSurfaceVariant = MistMuted,
    tertiary = SoftRose
)

private val LightColorScheme = lightColorScheme(
    primary = SignalBlueDeep,
    onPrimary = Mist,
    primaryContainer = SignalBlue,
    onPrimaryContainer = Midnight,
    secondary = LimeFlash,
    onSecondary = Midnight,
    background = Mist,
    onBackground = Midnight,
    surface = Color.White,
    onSurface = Midnight,
    surfaceVariant = Color(0xFFDCE8FF),
    onSurfaceVariant = Color(0xFF475569),
    tertiary = SoftRose
)

data class BQuickDimensions(
    val space0: Dp = 0.dp,
    val space025: Dp = 2.dp,
    val space05: Dp = 4.dp,
    val space1: Dp = 8.dp,
    val space2: Dp = 16.dp,
    val space3: Dp = 24.dp,
    val space4: Dp = 32.dp,
)

private val DefaultDimensions = BQuickDimensions()
data class BQuickShapes(
    val card: CornerBasedShape = RoundedCornerShape(24.dp)
)

private val DefaultShapes = BQuickShapes()
private val LocalBQuickDimensions = staticCompositionLocalOf { DefaultDimensions }
private val LocalBQuickShapes = staticCompositionLocalOf { DefaultShapes }

object BQuickTheme {
    val dimensions: BQuickDimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalBQuickDimensions.current

    val shapes: BQuickShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalBQuickShapes.current

    @Composable
    operator fun invoke(
        darkTheme: Boolean = true,
        content: @Composable () -> Unit
    ) {
        val shapes = DefaultShapes
        CompositionLocalProvider(
            LocalBQuickDimensions provides DefaultDimensions,
            LocalBQuickShapes provides shapes
        ) {
            MaterialTheme(
                colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
                shapes = Shapes(
                    large = shapes.card,
                    extraLarge = shapes.card
                ),
                typography = Typography,
                content = content
            )
        }
    }
}
