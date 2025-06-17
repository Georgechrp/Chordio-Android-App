package com.chordio.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BrightBluePrimaryDark,
    secondary = SoftGreenSecondaryDark,
    tertiary = LightRedTertiaryDark,

    background = BackgroundDark,
    surface = CardSurfaceDark,
    onPrimary = TextOnPrimaryDark,
    onSecondary = TextOnSecondaryDark,
    onTertiary = TextOnTertiaryDark,
    onBackground = TextOnBackgroundDark,
    onSurface = TextOnSurfaceDark
)


private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    secondary = SecondaryLight,
    tertiary = TertiaryLight,

    background = LightBackground,
    surface = SurfaceLight,
    onPrimary = TextOnPrimaryLight,
    onSecondary = TextOnSecondaryLight,
    onTertiary = TextOnTertiaryLight,
    onBackground = TextOnBackgroundLight,
    onSurface = TextOnSurfaceLight
)





@Composable
fun ChordsHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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
        typography = Typography,
        content = content
    )
}