package com.shoropio.gato.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonMagenta,
    tertiary = NeonAmber,
    background = CyberObsidian,
    surface = Color(0xFF121824),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0),
    primaryContainer = TransparentCyan,
    secondaryContainer = TransparentMagenta,
    outline = CyberDarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0F766E), // Deep teal
    secondary = Color(0xFFBE123C), // Deep rose
    tertiary = Color(0xFFB45309), // Amber
    background = Color(0xFFF8FAFC), // Off-white Slate 50
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    primaryContainer = Color(0xFFCCFBF1),
    secondaryContainer = Color(0xFFFFE4E6),
    outline = Color(0xFFCBD5E1)
)

private val ZeroRadiusShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp)
)

@Composable
fun GatoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color support is disabled by default to maintain the curated futuristic gamer look,
    // but the system is capable of keeping it if desired.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = ZeroRadiusShapes,
        typography = Typography,
        content = content
    )
}
