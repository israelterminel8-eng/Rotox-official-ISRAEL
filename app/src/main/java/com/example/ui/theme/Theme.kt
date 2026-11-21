package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = RotoxBlue,
    onPrimary = Color.White,
    primaryContainer = RotoxBlueContainer,
    onPrimaryContainer = RotoxBlueDark,
    secondary = RotoxRed,
    onSecondary = Color.White,
    secondaryContainer = RotoxRedContainer,
    onSecondaryContainer = RotoxRedDark,
    tertiary = RotoxGreen,
    onTertiary = Color.White,
    tertiaryContainer = RotoxGreenContainer,
    onTertiaryContainer = RotoxGreenDark,
    background = RotoxStoneBg,
    onBackground = RotoxInk,
    surface = RotoxSurface,
    onSurface = RotoxInk,
    surfaceVariant = RotoxSurfaceVariant,
    onSurfaceVariant = RotoxMuted,
    outline = RotoxBorder,
    error = RotoxRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF5B9BFF),
    onPrimary = Color(0xFF0A2454),
    primaryContainer = RotoxBlueDark,
    onPrimaryContainer = Color(0xFFDCE9FF),
    secondary = Color(0xFFFF6B6B),
    onSecondary = Color(0xFF4D0808),
    secondaryContainer = RotoxRedDark,
    onSecondaryContainer = Color(0xFFFFDAD6),
    tertiary = Color(0xFF56D364),
    onTertiary = Color(0xFF0A3312),
    tertiaryContainer = RotoxGreenDark,
    onTertiaryContainer = Color(0xFFD3F4D8),
    background = RotoxDarkBg,
    onBackground = RotoxDarkInk,
    surface = RotoxDarkSurface,
    onSurface = RotoxDarkInk,
    surfaceVariant = RotoxDarkSurfaceVar,
    onSurfaceVariant = RotoxDarkMuted,
    outline = RotoxDarkBorder,
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF4D0808)
)

val RotoxShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(22.dp)
)

@Composable
fun RotoxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RotoxTypography,
        shapes = RotoxShapes,
        content = content
    )
}
