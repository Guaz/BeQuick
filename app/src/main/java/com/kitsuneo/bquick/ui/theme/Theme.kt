package com.kitsuneo.bquick.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SignalBlue,
    onPrimary = Midnight,
    primaryContainer = SignalBlueDeep,
    onPrimaryContainer = Mist,
    secondary = LimeFlash,
    onSecondary = Midnight,
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

@Composable
fun BQuickTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
