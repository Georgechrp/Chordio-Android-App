package com.chordio.screens.auth.welcomeuser

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.chordio.R
import kotlinx.coroutines.delay
import androidx.compose.animation.core.*
import android.media.MediaPlayer
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.chordio.sharedpreferences.AppSettingsPreferences

@Composable
fun WelcomeScreen(onAnimationEnd: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val preferences = remember { AppSettingsPreferences(context) }
    val isDarkTheme = remember { preferences.isDarkMode() }

    val logoPainter = painterResource(
        id = if (isDarkTheme) R.drawable.blacklogotransparent else R.drawable.chordiowhitetransparent
    )
    PlaySplashSound()
    // Logo animations
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.3f else 0.6f,
        animationSpec = tween(1200, easing = EaseOutBack), label = ""
    )

    val alphaIn by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1000), label = ""
    )

    val offsetY by animateFloatAsState(
        targetValue = if (finished) -250f else 0f,
        animationSpec = tween(800, easing = EaseInOutCubic), label = ""
    )

    val alphaOut by animateFloatAsState(
        targetValue = if (finished) 0f else 1f,
        animationSpec = tween(800), label = ""
    )

    val effectiveAlpha = (alphaIn * alphaOut).coerceIn(0f, 1f)

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    LaunchedEffect(Unit) {
        delay(300)
        startAnimation = true
        delay(1800)
        finished = true
        delay(800)
        onAnimationEnd()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .graphicsLayer {
                    alpha = glowAlpha
                    scaleX = 1.2f
                    scaleY = 1.2f
                }
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.6f))
        )

        // Logo
        Image(
            painter = logoPainter,
            contentDescription = "App Logo",
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationY = offsetY
                    alpha = effectiveAlpha
                }
                .size(200.dp)
        )
    }
}


@Composable
fun PlaySplashSound() {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.splash_sound2)
        mediaPlayer.start()

        onDispose {
            mediaPlayer.release()
        }
    }
}