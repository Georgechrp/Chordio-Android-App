package com.chordio.screens.viewsong

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.chordio.R
import com.chordio.screens.SwipeableSongViewer
import com.chordio.viewmodels.MainViewModel
import com.chordio.viewmodels.auth.AuthViewModel
import com.chordio.viewmodels.main.HomeViewModel
import com.chordio.viewmodels.main.LibraryViewModel
import com.chordio.viewmodels.seconds.SongViewModel
import com.chordio.viewmodels.user.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistName: String,
    viewModel: LibraryViewModel,
    onBack: () -> Unit,
    mainViewModel: MainViewModel,
    homeViewModel: HomeViewModel,
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel,
    navController: NavHostController,
    songViewModel: SongViewModel
)
{
    val playlists by viewModel.playlists.collectAsState()
    val songs = playlists[playlistName] ?: emptyList()
    var selectedSongId by remember { mutableStateOf<String?>(null) }


    var songList by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    LaunchedEffect(songs) {
        println("🔁 Rebuilding songList for playlist: $playlistName")

        val fetched = songs.distinct().mapNotNull { id ->
            val song = songViewModel.getSongById(id)
            song?.let {
                println("✅ Adding to songList: ${it.title}")
                it.title to it.id
            }
        }

        songList = fetched
    }

    DisposableEffect(Unit) {
        homeViewModel.setFullScreen(false)
        onDispose {
            homeViewModel.setFullScreen(false)
        }
    }

    /*if (selectedSongId != null) {
        SwipeableSongViewer(
            songViewModel = songViewModel,
            songs = songList.map { it.second },
            initialSongId = selectedSongId!!,
            navController = navController,
            mainViewModel = mainViewModel,
            homeViewModel = homeViewModel,
            userViewModel = userViewModel,
            authViewModel = authViewModel,
            onExit = { selectedSongId = null },
        )
    }*/
    if (selectedSongId != null) {
        DetailedSongView(
            songViewModel = songViewModel,
            songId = selectedSongId!!,
            onBack = { selectedSongId = null },
            navController = navController,
            mainViewModel = mainViewModel,
            homeViewModel = homeViewModel,
            userViewModel = userViewModel,
            authViewModel = authViewModel
        )
    }

    else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(playlistName) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
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
                    songList.forEach { (title, id) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable {
                                    selectedSongId = id
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
                                    text = title,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                TextButton(onClick = {
                                    viewModel.removeSongFromPlaylist(playlistName, id) {}
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

