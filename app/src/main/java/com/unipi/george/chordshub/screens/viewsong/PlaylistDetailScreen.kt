package com.unipi.george.chordshub.screens.viewsong

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.unipi.george.chordshub.R
import com.unipi.george.chordshub.screens.SwipeableSongViewer
import com.unipi.george.chordshub.viewmodels.MainViewModel
import com.unipi.george.chordshub.viewmodels.main.HomeViewModel
import com.unipi.george.chordshub.viewmodels.main.LibraryViewModel
import com.unipi.george.chordshub.viewmodels.user.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistName: String,
    viewModel: LibraryViewModel,
    onBack: () -> Unit,
    mainViewModel: MainViewModel,
    homeViewModel: HomeViewModel,
    userViewModel: UserViewModel,
    navController: NavHostController
)
{
    val playlists by viewModel.playlists.collectAsState()
    val songs = playlists[playlistName] ?: emptyList()
    var selectedSongId by remember { mutableStateOf<String?>(null) }


    DisposableEffect(Unit) {
        homeViewModel.setFullScreen(false)
        onDispose {
            homeViewModel.setFullScreen(false)
        }
    }

    if (selectedSongId != null) {
        SwipeableSongViewer(
            songs = songs,
            initialSongId = selectedSongId!!,
            navController = navController,
            mainViewModel = mainViewModel,
            homeViewModel = homeViewModel,
            userViewModel = userViewModel,
            onExit = { selectedSongId = null }
        )
    }
    else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(playlistName) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                if (songs.isEmpty()) {
                    Text(
                        stringResource(R.string.no_songs_here),
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    songs.forEach { songTitle ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable {

                                    selectedSongId = songTitle
                                },
                            shape = MaterialTheme.shapes.medium,
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = songTitle,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                TextButton(onClick = {
                                    viewModel.removeSongFromPlaylist(playlistName, songTitle) {}
                                }) {
                                    Text("❌")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

