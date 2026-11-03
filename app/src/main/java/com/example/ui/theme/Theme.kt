package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkNavyColorScheme = darkColorScheme(
    primary = ElectricPurple,
    onPrimary = Color.White,
    primaryContainer = PurpleContainer,
    onPrimaryContainer = OnPurpleContainer,

    secondary = CyanBlue,
    onSecondary = Color(0xFF032840),
    secondaryContainer = BlueContainer,
    onSecondaryContainer = OnBlueContainer,

    tertiary = NeonIndigo,
    onTertiary = Color.White,

    background = NavyBackground,
    onBackground = TextPrimary,

    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavySurfaceVariant,
    onSurfaceVariant = TextSecondary,

    outline = NavyBorder,
    outlineVariant = NavyBorderLight,

    error = AccentPink,
    onError = Color.White
)

@Composable
fun VideoStudioTheme(
    darkTheme: Boolean = true, // Force rich dark navy by default
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkNavyColorScheme,
        typography = Typography,
        content = content
    )
}

// Keep backward compatibility name alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    VideoStudioTheme(content = content)
}
