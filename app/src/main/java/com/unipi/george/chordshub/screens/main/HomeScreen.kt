// package και imports δεν αλλάζουν
package com.unipi.george.chordshub.screens.main

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.unipi.george.chordshub.components.FilterRow
import com.unipi.george.chordshub.components.CardsView
import com.unipi.george.chordshub.components.LoadingView
import com.unipi.george.chordshub.repository.firestore.SongRepository
import com.unipi.george.chordshub.screens.viewsong.DetailedSongView
import com.unipi.george.chordshub.utils.ArtistImageOnly
import com.unipi.george.chordshub.viewmodels.MainViewModel
import com.unipi.george.chordshub.viewmodels.main.HomeViewModel
import com.unipi.george.chordshub.viewmodels.user.UserViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    mainViewModel: MainViewModel,
    userViewModel: UserViewModel,
    navController: NavController,
    onMenuClick: () -> Unit,
    profileImageUrl: String?
) {
    val selectedSongId by homeViewModel.selectedSongId.collectAsState()
    val songList by homeViewModel.songList.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    val selectedTitle = remember { mutableStateOf<String?>(null) }
    var artistMode by remember { mutableStateOf(false) }
    var artistList by remember { mutableStateOf<List<String>>(emptyList()) }
    val isMenuOpen by mainViewModel.isMenuOpen
    val isFullScreenState by homeViewModel.isFullScreen.collectAsState()
    var topBarOffset by rememberSaveable { mutableFloatStateOf(0f) }
    var showNoResults by remember { mutableStateOf(false) }
    val isFullScreen = remember { mutableStateOf(false) }

    LaunchedEffect(isFullScreen.value) {
        mainViewModel.setTopBarVisible(!isFullScreen.value)
        mainViewModel.setBottomBarVisible(!isFullScreen.value)
    }


    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val newOffset = (topBarOffset + available.y).coerceIn(-30f, 0f)
                topBarOffset = newOffset
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(Unit) {
        selectedFilter = "All"
    }

    LaunchedEffect(selectedFilter) {
        artistMode = selectedFilter == "Artists"

        when (selectedFilter) {
            "Artists" -> homeViewModel.getAllArtists()
            else -> {
                homeViewModel.fetchFilteredSongs(selectedFilter)
                homeViewModel.getAllArtists()
            }
        }
    }

    LaunchedEffect(songList, selectedFilter) {
        showNoResults = false
        if (songList.isEmpty()) {
            delay(5000)
            if (songList.isEmpty()) showNoResults = true
        }
    }

    BackHandler(enabled = isMenuOpen) {
        mainViewModel.setMenuOpen(false)
    }

    LaunchedEffect(selectedSongId, isFullScreenState) {
        if (selectedSongId == null && !isFullScreenState) {
            mainViewModel.setTopBarContent {
                FilterRow(
                    selectedFilter = selectedFilter,
                    onFilterChange = { selectedFilter = it }
                )
            }
        } else {
            mainViewModel.setTopBarContent {}
        }
    }


    val fetchArtists by homeViewModel.fetchArtists.collectAsState()
    val songRepo = remember { SongRepository(FirebaseFirestore.getInstance()) }

    LaunchedEffect(fetchArtists) {
        if (fetchArtists != null) {
            songRepo.getAllArtists { fetched ->
                artistList = fetched
            }
            homeViewModel.resetFetchArtists()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = if (isFullScreenState) 0.dp else 60.dp)
            .nestedScroll(nestedScrollConnection)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (selectedSongId == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(0, topBarOffset.roundToInt()) }
                        .zIndex(1f)
                ) {}
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    selectedSongId == null && songList.isEmpty() && showNoResults -> {
                        // εμφάνιση μηνύματος No results
                    }

                    selectedSongId == null && songList.isEmpty() -> LoadingView()

                    selectedSongId == null -> {
                        if (artistMode) {
                            val artistCards = artistList
                                .take(6) // μόνο 6 αν θες
                                .map { it to "artist:$it" }

                            CardsView(
                                songList = artistCards,
                                homeViewModel = homeViewModel,
                                selectedTitle = selectedTitle,
                                onSongClick = { tag ->
                                    val artist = tag.removePrefix("artist:")
                                    navController.navigate("artist/${Uri.encode(artist)}")
                                }
                            )
                        } else {
                            val combinedList = if (selectedFilter == "All")
                                artistList.take(6).map { it to "artist:$it" } + songList
                            else
                                songList

                        Box(modifier = Modifier.padding(bottom = 40.dp)) {

                            CardsView(
                                songList = combinedList,
                                homeViewModel = homeViewModel,
                                selectedTitle = selectedTitle,
                                onSongClick = { tag ->
                                    if (tag.startsWith("artist:")) {
                                        val artist = tag.removePrefix("artist:")
                                        navController.navigate("artist/${Uri.encode(artist)}")
                                    } else {
                                        homeViewModel.selectSong(tag)
                                    }
                                }
                            )
                        }
                    }
                    }

                    else -> {
                        DetailedSongView(
                            songId = selectedSongId!!,
                            onBack = {
                                homeViewModel.clearSelectedSong()
                                homeViewModel.setFullScreen(false)
                            },
                            navController = navController,
                            mainViewModel = mainViewModel,
                            homeViewModel = homeViewModel,
                            userViewModel = userViewModel
                        )
                    }
                }
            }
        }
    }

}
