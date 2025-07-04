package pt.isel.keepmyplanet.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors =
    lightColorScheme(
        primary = primaryLight,
        onPrimary = onPrimaryLight,
        secondary = secondaryLight,
        onSecondary = onSecondaryLight,
        tertiary = tertiaryLight,
        onTertiary = onTertiaryLight,
        error = errorLight,
        onError = onErrorLight,
        background = backgroundLight,
        onBackground = onBackgroundLight,
        surface = surfaceLight,
        onSurface = onSurfaceLight,
        surfaceVariant = surfaceVariantLight,
        onSurfaceVariant = onSurfaceVariantLight,
        outline = outlineLight,
    )

private val DarkColors =
    darkColorScheme(
        primary = primaryDark,
        onPrimary = onPrimaryDark,
        secondary = secondaryDark,
        onSecondary = onSecondaryDark,
        tertiary = tertiaryDark,
        onTertiary = onTertiaryDark,
        error = errorDark,
        onError = onErrorDark,
        background = backgroundDark,
        onBackground = onBackgroundDark,
        surface = surfaceDark,
        onSurface = onSurfaceDark,
        surfaceVariant = surfaceVariantDark,
        onSurfaceVariant = onSurfaceVariantDark,
        outline = outlineDark,
    )

@Composable
fun KeepMyPlanetTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors =
        if (!useDarkTheme) {
            LightColors
        } else {
            DarkColors
        }

    MaterialTheme(
        colorScheme = colors,
        // Aqui também podes definir a tua própria tipografia e formas
        // typography = YourAppTypography,
        // shapes = YourAppShapes,
        content = content,
    )
}
