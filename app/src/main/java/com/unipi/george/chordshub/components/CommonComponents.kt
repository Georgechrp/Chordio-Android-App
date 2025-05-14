package com.unipi.george.chordshub.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.unipi.george.chordshub.viewmodels.user.SettingsViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.unipi.george.chordshub.R
/*
*   Just some methods can be used by any place in the app
*/

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(50.dp),
            color = MaterialTheme.colorScheme.primary
        )

    }
}

@Composable
fun SettingsHeads(text: String, settingsViewModel: SettingsViewModel, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = TextStyle(
            fontSize = settingsViewModel.fontSize.value.sp,
            color = MaterialTheme.colorScheme.onBackground
        ),
        modifier = modifier
    )

}

@Composable
fun BlurredBackground(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            .blur(20.dp)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                onClick()
            }
    )

}


@Composable
fun UserProfileImage(
    imageUrl: String?,
    localImage: Uri? = null,
    size: Dp = 40.dp,
    border: Boolean = true,
    placeholderResId: Int = R.drawable.user_icon,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .then(if (border) Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
            else Modifier)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = when {
                localImage != null -> rememberAsyncImagePainter(localImage)
                imageUrl != null -> rememberAsyncImagePainter(imageUrl)
                else -> painterResource(id = placeholderResId)
            },
            contentDescription = stringResource(R.string.circular_image_description),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}


/*
fun addDataForFurElise() {
    val repository = FirestoreRepository(FirebaseFirestore.getInstance())

    lifecycleScope.launch {
        val songData = SongData(
            title = "Für Elise",
            artist = "Ludwig van Beethoven",
            key = "A Minor",
            bpm = 75, // Μέτριο tempo
            genres = listOf("Classical"),
            createdAt = System.currentTimeMillis().toString(),
            creatorId = "admin",
            lyrics = listOf(
                SongLine(
                    lineNumber = 1,
                    text = "Für Elise, section 1 melody",
                    chords = listOf(ChordPosition("Am", 0), ChordPosition("E", 8), ChordPosition("G", 16), ChordPosition("C", 24))
                ),
                SongLine(
                    lineNumber = 2,
                    text = "Für Elise, section 2 transition",
                    chords = listOf(ChordPosition("F", 0), ChordPosition("C", 10), ChordPosition("G", 20), ChordPosition("E", 28))
                )
            )
        )

        repository.addSongData("fur_elise_beethoven", songData)
    }
}*/
