package com.unipi.george.chordshub.screens.slidemenu.options


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.unipi.george.chordshub.R
import com.unipi.george.chordshub.components.CardsView
import com.unipi.george.chordshub.components.LoadingView
import com.unipi.george.chordshub.screens.viewsong.DetailedSongView
import com.unipi.george.chordshub.viewmodels.MainViewModel
import com.unipi.george.chordshub.viewmodels.auth.AuthViewModel
import com.unipi.george.chordshub.viewmodels.main.HomeViewModel
import com.unipi.george.chordshub.viewmodels.main.SearchViewModel
import com.unipi.george.chordshub.viewmodels.user.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentsScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    searchViewModel: SearchViewModel
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
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Πίσω")
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

                recentSongs.isEmpty() && artistList.isEmpty() -> {
                    Text(
                        "Δεν υπάρχουν πρόσφατα ακόμη.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                else -> {
                    if (recentSongs.isNotEmpty()) {
                        Text(
                            text = "Τα τελευταία τραγούδια που είδες",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        val recentSongPairs = remember(recentSongs) {
                            recentSongs.mapNotNull { song ->
                                val title = song.title?.trim()
                                val artist = song.artist?.trim()
                                val songId = song.id
                                if (title != null && songId != null) {
                                    val combinedTitle = if (!artist.isNullOrEmpty()) "$title - $artist" else title
                                    combinedTitle to songId
                                } else null
                            }
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

