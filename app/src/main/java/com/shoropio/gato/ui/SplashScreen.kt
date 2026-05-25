package com.shoropio.gato.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shoropio.gato.audio.SoundSynthesizer
import com.shoropio.gato.ui.theme.NeonCyan
import com.shoropio.gato.ui.theme.NeonMagenta
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animatable values for scale and alpha
    val scaleAnim = remember { Animatable(0.4f) }
    val alphaAnim = remember { Animatable(0f) }
    val rotXAnim = remember { Animatable(90f) }

    LaunchedEffect(Unit) {
        // Play quick synthesizer glitch beep upon opening
        SoundSynthesizer.playGlitchBeep()

        // Fast-paced premium kinetic introductory curve
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 750)
        )
        delay(1100) // Brief aesthetic lock
        alphaAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 400)
        )
        onSplashFinished()
    }

    LaunchedEffect(Unit) {
        rotXAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    CyberGridBackground(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Interactive glowing loading logo (Canvas)
            Canvas(
                modifier = Modifier
                    .size(110.dp)
                    .graphicsLayer {
                        scaleX = scaleAnim.value
                        scaleY = scaleAnim.value
                        rotationX = rotXAnim.value
                        alpha = alphaAnim.value
                    }
            ) {
                val gridStroke = 8f
                val w = size.width
                val h = size.height

                // Draw hashtag/grid
                drawLine(
                    color = Color(0x3300F0FF),
                    start = Offset(w * 0.33f, 0f),
                    end = Offset(w * 0.33f, h),
                    strokeWidth = gridStroke
                )
                drawLine(
                    color = Color(0x3300F0FF),
                    start = Offset(w * 0.66f, 0f),
                    end = Offset(w * 0.66f, h),
                    strokeWidth = gridStroke
                )
                drawLine(
                    color = Color(0x3300F0FF),
                    start = Offset(0f, h * 0.33f),
                    end = Offset(w, h * 0.33f),
                    strokeWidth = gridStroke
                )
                drawLine(
                    color = Color(0x3300F0FF),
                    start = Offset(0f, h * 0.66f),
                    end = Offset(w, h * 0.66f),
                    strokeWidth = gridStroke
                )

                // Draw Neon X in Top Left
                val xSize = 20f
                drawLine(
                    color = NeonCyan,
                    start = Offset(w * 0.16f - xSize, h * 0.16f - xSize),
                    end = Offset(w * 0.16f + xSize, h * 0.16f + xSize),
                    strokeWidth = 6f
                )
                drawLine(
                    color = NeonCyan,
                    start = Offset(w * 0.16f + xSize, h * 0.16f - xSize),
                    end = Offset(w * 0.16f - xSize, h * 0.16f + xSize),
                    strokeWidth = 6f
                )

                // Draw Neon O in Center
                drawCircle(
                    color = NeonMagenta,
                    radius = 24f,
                    center = Offset(w * 0.5f, h * 0.5f),
                    style = Stroke(width = 6f)
                )

                // Draw Neon X in Bottom Right
                drawLine(
                    color = NeonCyan,
                    start = Offset(w * 0.83f - xSize, h * 0.83f - xSize),
                    end = Offset(w * 0.83f + xSize, h * 0.83f + xSize),
                    strokeWidth = 6f
                )
                drawLine(
                    color = NeonCyan,
                    start = Offset(w * 0.83f + xSize, h * 0.83f - xSize),
                    end = Offset(w * 0.83f - xSize, h * 0.83f + xSize),
                    strokeWidth = 6f
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Text glowing branding
            NeonText(
                text = "G A T O",
                color = NeonCyan,
                fontSize = 38.sp,
                glowColor = NeonCyan,
                modifier = Modifier.graphicsLayer {
                    alpha = alphaAnim.value
                }
            )

            NeonText(
                text = "CYBERNETIC SYSTEM",
                color = NeonMagenta.copy(alpha = 0.8f),
                fontSize = 11.sp,
                glowColor = NeonMagenta,
                modifier = Modifier.graphicsLayer {
                    alpha = alphaAnim.value
                }
            )
        }
    }
}
