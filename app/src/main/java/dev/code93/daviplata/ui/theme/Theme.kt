package dev.code93.daviplata.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DaviColorScheme = lightColorScheme(
    primary = BrandRed,
    onPrimary = White,
    primaryContainer = BrandRedSurface,
    secondary = TextSecondary,
    onSecondary = White,
    background = White,
    onBackground = TextPrimary,
    surface = White,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextSecondary,
    error = AppError,
    onError = White,
    errorContainer = ErrorLight,
    outline = StrokeDefault,
    outlineVariant = StrokeFocus,
)

@Composable
fun DaviPlataTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DaviColorScheme,
        typography = DaviTypography,
        content = content,
    )
}
