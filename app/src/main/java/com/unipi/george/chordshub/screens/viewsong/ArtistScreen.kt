package com.unipi.george.chordshub.screens.viewsong

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.unipi.george.chordshub.R
import com.unipi.george.chordshub.components.CardsView
import com.unipi.george.chordshub.models.song.Song
import com.unipi.george.chordshub.repository.firestore.SongRepository
import com.unipi.george.chordshub.utils.ArtistInfo
import com.unipi.george.chordshub.viewmodels.main.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistScreen(artistName: String, navController: NavController) {
    var showInfoSheet by remember { mutableStateOf(false) }
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    val homeViewModel: HomeViewModel = viewModel()

    val selectedTitle = remember { mutableStateOf<String?>(null) }
    val selectedSongId = remember { mutableStateOf<String?>(null) }

    val songRepository = remember { SongRepository(FirebaseFirestore.getInstance()) }


    LaunchedEffect(artistName) {
        homeViewModel.clearSelectedSong()
        songRepository.getSongsByArtistName(artistName) { fetchedSongs ->
            songs = fetchedSongs
        }
    }



    // Show DetailedSongView if a song is selected
    if (selectedSongId.value != null) {
        DetailedSongView(
            songId = selectedSongId.value!!,
            isFullScreenState = false,
            onBack = { selectedSongId.value = null },
            navController = navController,
            mainViewModel = viewModel(),
            homeViewModel = homeViewModel,
            userViewModel = viewModel()
        )
        return
    }

    // Scaffold with CardsView
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = artistName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoSheet = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Info")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.main_blue_background))
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            CardsView(
                songList = songs.mapNotNull { song ->
                    val title = song.title
                    if (title != null) title to title else null
                },
                homeViewModel = homeViewModel,
                selectedTitle = selectedTitle,
                columns = 1,
                cardHeight = 80.dp,
                cardElevation = 4.dp,
                cardPadding = 12.dp,
                gridPadding = 16.dp,
                fontSize = 16.sp,
                onSongClick = { clickedTitle ->
                    selectedSongId.value = clickedTitle
                }
            )

        }
    }

    if (showInfoSheet) {
        ArtistInfoBottomSheet(artistName) { showInfoSheet = false }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistInfoBottomSheet(artistName: String, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = artistName, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ArtistInfo(artistName)
                Spacer(modifier = Modifier.height(16.dp))
            }

        }
    }
}