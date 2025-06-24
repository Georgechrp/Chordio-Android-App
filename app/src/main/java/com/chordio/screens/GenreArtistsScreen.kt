package com.chordio.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chordio.components.CardsView
import com.chordio.components.LoadingView
import com.chordio.repository.firestore.SongRepository
import com.chordio.viewmodels.main.HomeViewModel
import com.chordio.viewmodels.seconds.SongViewModel

@Composable
fun GenreArtistsScreen(
    genre: String,
    songViewModel: SongViewModel,
    navController: NavController
) {
    val artists = remember { mutableStateListOf<String>() }
    val isLoading = remember { mutableStateOf(true) }

    LaunchedEffect(genre) {
        songViewModel.getSongsByGenres(listOf(genre)) { songs ->
            val uniqueArtists = songs.mapNotNull { it.artist }.distinct()
            artists.clear()
            artists.addAll(uniqueArtists)
            isLoading.value = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Καλλιτέχνες στο είδος: $genre",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (isLoading.value) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingView()
            }
        } else if (artists.isEmpty()) {
            Text(
                text = "Δεν βρέθηκαν καλλιτέχνες για αυτό το είδος.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            CardsView(
                songList = artists.map { it to "artist:$it" },
                homeViewModel = remember { HomeViewModel() },
                selectedTitle = remember { mutableStateOf(null) },
                onSongClick = { tag ->
                    if (tag.startsWith("artist:")) {
                        val artist = tag.removePrefix("artist:")
                        navController.navigate("artist/${Uri.encode(artist)}")
                    }
                }
            )
        }
    }
}
