package com.penaltycoach.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// A single, deliberately light color scheme. The app owns its identity through
// the orange header + goal board rather than following the system dark theme,
// which keeps contrast predictable and the design consistent.
private val PenaltyColorScheme = lightColorScheme(
    primary = BrightOrange,
    onPrimary = WhiteText,
    primaryContainer = SoftOrangePanel,
    onPrimaryContainer = DeepOrange,
    secondary = DarkGraphite,
    onSecondary = WhiteText,
    tertiary = SavedBlue,
    onTertiary = WhiteText,
    background = LightAppBackground,
    onBackground = DarkText,
    surface = WhiteCard,
    onSurface = DarkText,
    surfaceVariant = SoftOrangePanel,
    onSurfaceVariant = SecondaryGrayText,
    outline = MutedLabelGray,
    error = MissRed,
    onError = WhiteText
)

@Composable
fun PenaltyCoachTheme(
    // Kept for API symmetry; the app intentionally stays light for a consistent look.
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = PenaltyColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color(0xFFE65300).toArgb()
                // Orange status bar is dark enough to want light icons.
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                    Color(0xFFE65300).luminance() > 0.5f
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

private fun Color.toArgb(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(),
    (red * 255).toInt(),
    (green * 255).toInt(),
    (blue * 255).toInt()
)
