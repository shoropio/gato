package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundSynthesizer
import com.example.ui.theme.CyberObsidian
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta

/**
 * Renders an immersive, procedurally drawn dark cosmic space grid background
 * with shifting stars and sweeping grid vectors.
 */
@Composable
fun CyberGridBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "cybergrid_anim")
    
    // Constant slow sweep for translation offsets
    val offsetProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_progress"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberObsidian)
            .drawBehind {
                val width = size.width
                val height = size.height
                val gridSpacing = 60f
                val yOffset = offsetProgress * gridSpacing
                val xOffset = offsetProgress * gridSpacing

                // 1. Draw glowing space dust radial gradient
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x1A00F0FF), Color.Transparent),
                        center = Offset(width * 0.5f, height * 0.4f),
                        radius = width * 0.82f
                    )
                )

                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x16FF007F), Color.Transparent),
                        center = Offset(width * 0.8f, height * 0.8f),
                        radius = width * 0.6f
                    )
                )

                // 2. Vertical moving cybergrid lines
                var x = 0f
                while (x < width) {
                    val currX = (x + xOffset) % width
                    drawLine(
                        color = Color(0x0C00F0FF),
                        start = Offset(currX, 0f),
                        end = Offset(currX, height),
                        strokeWidth = 1.3f
                    )
                    x += gridSpacing
                }

                // 3. Horizontal moving cybergrid lines
                var y = 0f
                while (y < height) {
                    val currY = (y + yOffset) % height
                    drawLine(
                        color = Color(0x0C00F0FF),
                        start = Offset(0f, currY),
                        end = Offset(width, currY),
                        strokeWidth = 1.3f
                    )
                    y += gridSpacing
                }
            }
    ) {
        content()
    }
}

/**
 * Glassmorphic translucent panel overlay with neon bordering.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Color(0x3300F0FF),
    glowColor: Color = Color(0x0D00F0FF),
    cornerRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color(0x0F0F172C))
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        borderColor,
                        borderColor.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .drawBehind {
                // Outer glow shadow
                drawRoundRect(
                    color = glowColor,
                    size = size,
                    alpha = 0.5f,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
                    style = Stroke(width = 4.dp.toPx())
                )
            }
            .padding(16.dp),
        content = content
    )
}

/**
 * Neon cyberbutton with dynamic scaling states, sound triggers, and haptic feedback toggles.
 */
@Composable
fun CyberButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = NeonCyan,
    isSecondary: Boolean = false,
    testTag: String = ""
) {
    val haptic = LocalHapticFeedback.current
    val scaleAnim = remember { Animatable(1f) }
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            scaleAnim.animateTo(0.94f, animationSpec = tween(100))
        } else {
            scaleAnim.animateTo(1f, animationSpec = tween(150))
        }
    }

    val finalBorderColor = if (isSecondary) NeonMagenta else color
    val buttonBg = if (isPressed) finalBorderColor.copy(alpha = 0.15f) else Color(0x0DFFFFFF)

    Box(
        modifier = modifier
            .testTag(testTag)
            .graphicsLayer {
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
            }
            .clip(RoundedCornerShape(12.dp))
            .background(buttonBg)
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        finalBorderColor,
                        finalBorderColor.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    SoundSynthesizer.playClick()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
            .height(54.dp),
        contentAlignment = Alignment.Center
    ) {
        NeonText(
            text = text,
            color = if (isPressed) Color.White else finalBorderColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            glowColor = finalBorderColor
        )
    }
}

/**
 * Clean text rendering with overlapping radial neon-shadow simulation.
 */
@Composable
fun NeonText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 28.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    glowColor: Color = color,
    maxLines: Int = 1
) {
    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        fontFamily = FontFamily.Monospace,
        maxLines = maxLines,
        style = TextStyle(
            shadow = Shadow(
                color = glowColor.copy(alpha = 0.8f),
                offset = Offset(0f, 0f),
                blurRadius = 14f
            )
        ),
        modifier = modifier
    )
}
