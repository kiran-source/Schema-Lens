package com.schemalens.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AccentTeal,
    onPrimary = BgDark,
    primaryContainer = AccentTealDark,
    onPrimaryContainer = TextPrimary,
    secondary = AccentBlue,
    onSecondary = BgDark,
    tertiary = AccentPurple,
    onTertiary = BgDark,
    background = BgDark,
    onBackground = TextPrimary,
    surface = PanelDark,
    onSurface = TextPrimary,
    surfaceVariant = PanelNested,
    onSurfaceVariant = TextDim,
    outline = BorderDark,
    outlineVariant = BorderSubtle,
    error = RiskBreaking,
    onError = TextPrimary,
    errorContainer = RiskBreakingBg,
    onErrorContainer = RiskBreaking
)

@Composable
fun SchemaLensTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BgDark.toArgb()
            window.navigationBarColor = BgDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
