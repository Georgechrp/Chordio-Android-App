package com.chordio.screens.viewsong

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.chordio.components.CardsView
import com.chordio.models.song.Song
import com.chordio.repository.firestore.SongRepository
import com.chordio.utils.ArtistInfo
import com.chordio.viewmodels.MainViewModel
import com.chordio.viewmodels.SongViewModelFactory
import com.chordio.viewmodels.auth.AuthViewModel
import com.chordio.viewmodels.main.HomeViewModel
import com.chordio.viewmodels.seconds.SongViewModel
import com.chordio.viewmodels.user.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistScreen(
    artistName: String,
    navController: NavController,
    mainViewModel: MainViewModel,
    homeViewModel: HomeViewModel,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel
) {

    var showInfoSheet by remember { mutableStateOf(false) }
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    val selectedTitle = remember { mutableStateOf<String?>(null) }
    val selectedSongId = remember { mutableStateOf<String?>(null) }
    val songViewModel: SongViewModel = viewModel(
        factory = SongViewModelFactory(SongRepository(FirebaseFirestore.getInstance()))
    )

    LaunchedEffect(artistName) {
        homeViewModel.clearSelectedSong()
        songViewModel.getSongsByArtistName(artistName) { fetchedSongs ->
            val sortedByPopularity = fetchedSongs.sortedByDescending { it.viewsCount ?: 0 }
            val top5 = sortedByPopularity.take(5)
            val remaining = sortedByPopularity.drop(5).sortedBy { it.title }
            songs = top5 + remaining
        }
    }

    val songState by songViewModel.songState.collectAsState()
    val selectedSong = songs.find { it.id == selectedSongId.value }

    LaunchedEffect(selectedSong) {
        if (selectedSong != null) {
            songViewModel.updateLocalSong(selectedSong)
        }
    }

    if (selectedSongId.value != null && songState == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading...")
        }
        return
    }

    if (selectedSongId.value != null) {
        DetailedSongView(
            songId = selectedSongId.value!!,
            onBack = { selectedSongId.value = null },
            navController = navController,
            mainViewModel = mainViewModel,
            homeViewModel = homeViewModel,
            userViewModel = userViewModel,
            authViewModel = authViewModel
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
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
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
                .background(color = MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            CardsView(
                songList = songs.mapNotNull { song ->
                    val title = song.title
                    val id = song.id
                    if (title != null && id != null) title to id else null
                },
                homeViewModel = homeViewModel,
                selectedTitle = selectedTitle,
                columns = 1,
                cardHeight = 60.dp,
                cardElevation = 4.dp,
                cardPadding = 12.dp,
                gridPadding = 16.dp,
                fontSize = 16.sp,
                onSongClick = { clickedId ->
                    selectedSongId.value = clickedId
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