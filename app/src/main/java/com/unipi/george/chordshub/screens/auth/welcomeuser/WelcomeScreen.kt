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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import kotlinx.coroutines.delay

/*
*   Just a Welcome Screen with fade up 3 seconds from 40% --> 200%
*/

@Composable
fun WelcomeScreen() {
    // Ελέγχει αν το animation πρέπει να ξεκινήσει (μεταβαίνει σε true με καθυστέρηση)
    var visible by remember { mutableStateOf(false) }

    // Δημιουργεί animation για το μέγεθος (scale) του logo: από 40% σε 200% σε 3s
    val scale by animateFloatAsState(
        targetValue = if (visible) 2.4f else 0.4f,
        animationSpec = tween(durationMillis = 3000, easing = EaseOutCubic)
    )

    // Δημιουργεί animation για τη διαφάνεια (alpha): από 0 (αόρατο) σε 2 (πλήρως ορατό + έξτρα φωτεινότητα)
    val alpha by animateFloatAsState(
        targetValue = if (visible) 2.4f else 0f,
        animationSpec = tween(durationMillis = 3000)
    )

    // Ξεκινάει το animation 400ms μετά την είσοδο του composable
    LaunchedEffect(Unit) {
        delay(400)
        visible = true
    }

    // Κεντράρει το περιεχόμενο στην οθόνη με background από το theme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Το λογότυπο με εφέ μεγέθυνσης και ξεθολώματος, βάση των scale & alpha
            Image(
                painter = painterResource(id = R.drawable.chordiologo1),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(180.dp)
                    .scale(scale)
                    .alpha(alpha)
            )
        }
    }
}
