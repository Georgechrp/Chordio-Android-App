package com.unipi.george.chordshub.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),         // Bright Blue 200 για primary (αντίστοιχο του #2196F3)
    secondary = Color(0xFFA5D6A7),       // Soft Green 200 (αντίστοιχο του #4CAF50)
    tertiary = Color(0xFFEF9A9A),        // Light Red 200 (error/warning)

    background = Color(0xFF121212),      // Dark background (standard Material Dark)
    surface = Color(0xFF1E1E1E),         // Πιο φωτεινό σκούρο για κάρτες/επιφάνειες
    onPrimary = Color.Black,             // Text πάνω στο ανοιχτό μπλε
    onSecondary = Color.Black,           // Text πάνω στο ανοιχτό πράσινο
    onTertiary = Color.Black,            // Text πάνω στο ροζ
    onBackground = Color.White,          // Κείμενο πάνω στο dark background
    onSurface = Color.White              // Κείμενο πάνω σε κάρτες
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFFA726),         // Warm Orange (για κουμπιά/filters)
    secondary = Color(0xFFB3E5FC),       // Light Blue (για chip highlights, tags)
    tertiary = Color(0xFFE57373),        // Coral Red (για warnings, secondary accents)

    background = Color(0xFF0D1B2A),      // Deep Blue Background (navy-night style)
    surface = Color(0xFF1B263B),         // Λίγο πιο ανοιχτό μπλε (για κάρτες)
    onPrimary = Color.Black,             // Text πάνω στο πορτοκαλί
    onSecondary = Color.Black,           // Text πάνω στο ανοιχτό μπλε
    onTertiary = Color.White,            // Text πάνω στο coral
    onBackground = Color(0xFFE0E1DD),    // Pale gray – text πάνω στο background
    onSurface = Color(0xFF121212)        // Text πάνω σε κάρτες
)




@Composable
fun ChordsHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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