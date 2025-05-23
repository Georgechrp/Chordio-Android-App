package com.unipi.george.chordshub.screens.auth.welcomeuser

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.unipi.george.chordshub.R
import androidx.compose.animation.core.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

/*
*   Just a Welcome Screen with fade up 3 seconds from 40% --> 200%
*/

@Composable
fun WelcomeScreen(onAnimationEnd: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.4f else 0.6f,
        animationSpec = tween(1200, easing = EaseOutBack), label = ""
    )

    val alphaIn by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1000), label = ""
    )

    val offsetY by animateFloatAsState(
        targetValue = if (finished) -300f else 0f,
        animationSpec = tween(800, easing = EaseInOutCubic), label = ""
    )

    val alphaOut by animateFloatAsState(
        targetValue = if (finished) 0f else 1f,
        animationSpec = tween(800), label = ""
    )

    val effectiveAlpha = alphaIn * alphaOut

    LaunchedEffect(Unit) {
        delay(300)
        startAnimation = true
        delay(1600)
        finished = true
        delay(900)
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
        Image(
            painter = painterResource(id = R.drawable.chordiologo1),
            contentDescription = "App Logo",
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationY = offsetY
                    alpha = effectiveAlpha
                }
                .sizeIn(140.dp, 220.dp)
        )
    }
}
