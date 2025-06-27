package com.chordio.screens.slidemenu.options


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.chordio.R
import com.chordio.components.CardsView
import com.chordio.components.LoadingView
import com.chordio.screens.viewsong.DetailedSongView
import com.chordio.viewmodels.MainViewModel
import com.chordio.viewmodels.auth.AuthViewModel
import com.chordio.viewmodels.main.HomeViewModel
import com.chordio.viewmodels.main.SearchViewModel
import com.chordio.viewmodels.seconds.SongViewModel
import com.chordio.viewmodels.user.UserViewModel
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    searchViewModel: SearchViewModel,
    songViewModel: SongViewModel
) {
    val recentSongs by userViewModel.recentSongs
    val artistList by searchViewModel.artistList.collectAsState()
    val userId = authViewModel.getUserId()
    var isLoading by remember { mutableStateOf(true) }
    val selectedSongId by homeViewModel.selectedSongId.collectAsState()
    val mainViewModel = remember { MainViewModel() }
    // Fetch songs and artists
    LaunchedEffect(userId) {
        if (userId != null) {
            userViewModel.fetchRecentSongs(userId)
        }
        homeViewModel.getAllArtists()
        isLoading = false
    }
    LaunchedEffect(Unit) {
        searchViewModel.fetchAllArtists()
    }




    if (selectedSongId != null) {
        DetailedSongView(
            songViewModel = songViewModel,
            songId = selectedSongId!!,
            onBack = {
                homeViewModel.clearSelectedSong()
            },
            navController = navController,
            mainViewModel = mainViewModel,
            homeViewModel = homeViewModel,
            userViewModel = userViewModel,
            authViewModel = authViewModel
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.recent_text)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when {
                isLoading -> {
                    LoadingView()
                }

                recentSongs.isEmpty() -> {
                    Text(
                        text = stringResource(id = R.string.no_recents),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                else -> {
                    if (recentSongs.isNotEmpty()) {
                       /* Text(
                            text = stringResource(id = R.string.last_songs_u_see),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onBackground
                        )*/

                        val songTimestampMap = userViewModel.getRecentSongMap()

                        val grouped = remember(recentSongs) {
                            recentSongs.groupBy { song ->
                                val ts = songTimestampMap[song.id] ?: 0L
                                Instant.ofEpochMilli(ts)
                                    .atZone(ZoneId.of("Europe/Athens"))
                                    .toLocalDate()
                            }.toSortedMap(reverseOrder())
                        }






                        grouped.forEach { (date, songs) ->
                            val label = when (date) {
                                java.time.LocalDate.now() -> "Today"
                                java.time.LocalDate.now().minusDays(1) -> "Yesterday"
                                else -> date.toString()
                            }

                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.primary
                            )

                            val recentSongPairs = songs.map { song ->
                                val artist = song.artist.trim()
                                val combinedTitle = if (artist.isNotEmpty()) "${song.title} - $artist" else song.title
                                combinedTitle to song.id
                            }

                            CardsView(
                                songList = recentSongPairs,
                                homeViewModel = homeViewModel,
                                selectedTitle = remember { mutableStateOf(null) },
                                columns = 1,
                                cardHeight = 80.dp,
                                cardPadding = 12.dp,
                                fontSize = 16.sp,
                                onSongClick = { songId ->
                                    homeViewModel.selectSong(songId)
                                }
                            )
                        }


                    }


                }

            }

        }

    }

}

