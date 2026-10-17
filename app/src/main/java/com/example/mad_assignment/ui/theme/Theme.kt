package com.example.mad_assignment.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

/*
 Every screen reads colours from MaterialTheme.colorScheme, never from the raw
 constants in Color.kt, so the light and dark palettes stay in sync.

 role used in the UI            light            dark
 primary (buttons, logo)        Forest           DarkForest
 primaryContainer (good chip)   ForestTint       DarkForestTint
 background / surface           Cream / Paper    DarkCream / DarkPaper
 surfaceVariant (cover tiles)   Sand             DarkSand
 onSurfaceVariant (muted text)  InkMuted         DarkInkMuted
 tertiary (warning / due soon)  Amber            DarkAmber
 error (overdue / errors)       Rust             DarkRust
*/
private val FolioLightColors = lightColorScheme(
    primary = Forest,
    onPrimary = Color.White,
    primaryContainer = ForestTint,
    onPrimaryContainer = Forest,
    secondary = Forest,
    onSecondary = Color.White,
    secondaryContainer = ForestTint,
    onSecondaryContainer = Forest,
    tertiary = Amber,
    onTertiary = Color.White,
    tertiaryContainer = AmberTint,
    onTertiaryContainer = Amber,
    background = Cream,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Sand,
    onSurfaceVariant = InkMuted,
    outline = Outline,
    outlineVariant = Outline,
    error = Rust,
    onError = Color.White,
    errorContainer = RustTint,
    onErrorContainer = Rust,
    surfaceContainerLowest = Paper,
    surfaceContainerLow = Paper,
    surfaceContainer = Paper,
    surfaceContainerHigh = Paper,
    surfaceContainerHighest = Sand,
)

private val FolioDarkColors = darkColorScheme(
    primary = DarkForest,
    onPrimary = DarkOnForest,
    primaryContainer = DarkForestTint,
    onPrimaryContainer = DarkForest,
    secondary = DarkForest,
    onSecondary = DarkOnForest,
    secondaryContainer = DarkForestTint,
    onSecondaryContainer = DarkForest,
    tertiary = DarkAmber,
    onTertiary = DarkCream,
    tertiaryContainer = DarkAmberTint,
    onTertiaryContainer = DarkAmber,
    background = DarkCream,
    onBackground = DarkInk,
    surface = DarkPaper,
    onSurface = DarkInk,
    surfaceVariant = DarkSand,
    onSurfaceVariant = DarkInkMuted,
    outline = DarkOutline,
    outlineVariant = DarkOutline,
    error = DarkRust,
    onError = DarkCream,
    errorContainer = DarkRustTint,
    onErrorContainer = DarkRust,
    surfaceContainerLowest = DarkCream,
    surfaceContainerLow = DarkPaper,
    surfaceContainer = DarkPaper,
    surfaceContainerHigh = DarkPaper,
    surfaceContainerHighest = DarkSand,
)

private val FolioShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp),
)

@Composable
fun FolioTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            // keep the status / navigation bar icons readable in both palettes
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = if (darkTheme) FolioDarkColors else FolioLightColors,
        typography = FolioTypography,
        shapes = FolioShapes,
        content = content
    )
}
