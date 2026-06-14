package com.kitsuneo.bquick.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.sin

enum class SessionBackgroundStyle {
    Preparation,
    Fire,
    Wave,
    Neutral
}

@Composable
fun AnimatedSessionBackground(
    style: SessionBackgroundStyle,
    modifier: Modifier = Modifier
) {
    when (style) {
        SessionBackgroundStyle.Preparation -> PreparationBackground(modifier)
        SessionBackgroundStyle.Fire -> FireBackground(modifier)
        SessionBackgroundStyle.Wave -> WaveBackground(modifier)
        SessionBackgroundStyle.Neutral -> NeutralBackground(modifier)
    }
}

@Composable
private fun NeutralBackground(modifier: Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF7F8FA),
                    Color(0xFFE7EBF2),
                    Color(0xFFD8DFEA)
                )
            )
        )
    }
}

@Composable
private fun PreparationBackground(modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "preparationBackground")
    val drift = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "preparationDrift"
    )
    val shimmer = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "preparationShimmer"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF0B2319),
                    Color(0xFF165135),
                    Color(0xFF2F8D5E),
                    Color(0xFF76D89E)
                ),
                start = Offset.Zero,
                end = Offset(size.width, size.height * 1.2f)
            )
        )

        val blobs = listOf(
            BlobSpec(0.18f, 0.22f, 0.28f, Color(0x5534D38A)),
            BlobSpec(0.74f, 0.18f, 0.24f, Color(0x4448E3A1)),
            BlobSpec(0.28f, 0.68f, 0.34f, Color(0x4445C789)),
            BlobSpec(0.82f, 0.74f, 0.30f, Color(0x335EF0B0)),
            BlobSpec(0.52f, 0.48f, 0.22f, Color(0x3367FFC0))
        )
        blobs.forEachIndexed { index, blob ->
            val phase = ((drift.value + (index * 0.17f)) % 1f)
            val cx = (blob.x + sin(phase * PI.toFloat() * 2f) * 0.05f) * size.width
            val cy = (blob.y + sin((phase + shimmer.value) * PI.toFloat()) * 0.04f) * size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(blob.color, Color.Transparent),
                    center = Offset(cx, cy),
                    radius = size.minDimension * blob.radius
                ),
                radius = size.minDimension * blob.radius,
                center = Offset(cx, cy)
            )
        }
    }
}

@Composable
private fun FireBackground(modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "fireBackground")
    val sway = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fireSway"
    )
    val flicker = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fireFlicker"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF6B2328),
                    Color(0xFF982E36),
                    Color(0xFFC84245),
                    Color(0xFFE15A52)
                )
            )
        )

        repeat(22) { index ->
            val phase = (sway.value + index * 0.071f) % 1f
            val seedX = (index * 53 % 100) / 100f
            val x = size.width * (0.08f + seedX * 0.84f) + sin(phase * PI.toFloat() * 4f + index) * size.width * 0.03f
            val y = size.height - (phase * size.height * 0.86f)
            val radius = size.minDimension * (0.005f + (index % 3) * 0.0018f)
            val alpha = (1f - phase) * 0.65f
            drawCircle(
                color = Color(0xFFFFD27A).copy(alpha = alpha),
                radius = radius,
                center = Offset(x, y)
            )
            drawCircle(
                color = Color(0xFFFF7A4F).copy(alpha = alpha * 0.55f),
                radius = radius * 1.9f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun WaveBackground(modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "waveBackground")
    val phaseA = transition.animateFloat(
        initialValue = 0f,
        targetValue = (PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhaseA"
    )
    val phaseB = transition.animateFloat(
        initialValue = 0f,
        targetValue = (PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhaseB"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFD6E0FF),
                    Color(0xFF5D73FF),
                    Color(0xFF0827F5)
                )
            )
        )

        drawWaveLayer(
            color = Color(0x66FFFFFF),
            baseYFraction = 0.33f,
            amplitude = size.height * 0.035f,
            wavelength = size.width * 0.78f,
            phase = phaseA.value
        )
        drawWaveLayer(
            color = Color(0x660827F5),
            baseYFraction = 0.52f,
            amplitude = size.height * 0.045f,
            wavelength = size.width * 0.62f,
            phase = phaseB.value + 1.2f
        )
        drawWaveLayer(
            color = Color(0x88406BFF),
            baseYFraction = 0.72f,
            amplitude = size.height * 0.055f,
            wavelength = size.width * 0.95f,
            phase = phaseA.value * 0.75f + 2.1f
        )

        repeat(5) { index ->
            val x = size.width * (0.12f + index * 0.18f) + sin(phaseB.value + index) * size.width * 0.015f
            val y = size.height * (0.18f + index * 0.12f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x22FFFFFF), Color.Transparent),
                    center = Offset(x, y),
                    radius = size.minDimension * 0.12f
                ),
                radius = size.minDimension * 0.12f,
                center = Offset(x, y)
            )
        }
    }
}

private fun DrawScope.drawWaveLayer(
    color: Color,
    baseYFraction: Float,
    amplitude: Float,
    wavelength: Float,
    phase: Float
) {
    val path = Path().apply {
        moveTo(0f, size.height)
        lineTo(0f, size.height * baseYFraction)

        var x = 0f
        while (x <= size.width + 20f) {
            val y = (size.height * baseYFraction) +
                sin(((x / wavelength) * PI * 2).toFloat() + phase) * amplitude
            lineTo(x, y)
            x += 20f
        }

        lineTo(size.width, size.height)
        close()
    }
    drawPath(path = path, color = color)
}

private data class BlobSpec(
    val x: Float,
    val y: Float,
    val radius: Float,
    val color: Color
)
