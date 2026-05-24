package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit

private val DarkColorScheme = darkColorScheme(
    primary = Blue500,
    onPrimary = Color.White,
    secondary = Teal400,
    onSecondary = Slate900,
    background = Slate900,
    onBackground = Color.White,
    surface = Slate800,
    onSurface = Color.White,
    surfaceVariant = Slate700,
    onSurfaceVariant = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Blue600,
    onPrimary = Color.White,
    secondary = Teal500,
    onSecondary = Color.White,
    background = Slate50,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Slate100,
    onSurfaceVariant = Color.Black
)

private fun scaleTextStyle(style: TextStyle, scale: Float): TextStyle {
    val size = if (style.fontSize.isSp) style.fontSize * scale else style.fontSize
    val line = if (style.lineHeight.isSp) style.lineHeight * scale else style.lineHeight
    return style.copy(fontSize = size, lineHeight = line)
}

private fun getScaledTypography(scale: Float): androidx.compose.material3.Typography {
    val base = com.example.ui.theme.Typography
    return androidx.compose.material3.Typography(
        displayLarge = scaleTextStyle(base.displayLarge, scale),
        displayMedium = scaleTextStyle(base.displayMedium, scale),
        displaySmall = scaleTextStyle(base.displaySmall, scale),
        headlineLarge = scaleTextStyle(base.headlineLarge, scale),
        headlineMedium = scaleTextStyle(base.headlineMedium, scale),
        headlineSmall = scaleTextStyle(base.headlineSmall, scale),
        titleLarge = scaleTextStyle(base.titleLarge, scale),
        titleMedium = scaleTextStyle(base.titleMedium, scale),
        titleSmall = scaleTextStyle(base.titleSmall, scale),
        bodyLarge = scaleTextStyle(base.bodyLarge, scale),
        bodyMedium = scaleTextStyle(base.bodyMedium, scale),
        bodySmall = scaleTextStyle(base.bodySmall, scale),
        labelLarge = scaleTextStyle(base.labelLarge, scale),
        labelMedium = scaleTextStyle(base.labelMedium, scale),
        labelSmall = scaleTextStyle(base.labelSmall, scale)
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to strictly enforce our custom professional Slate&Teal SaaS identity
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getScaledTypography(1.15f),
        content = content
    )
}

