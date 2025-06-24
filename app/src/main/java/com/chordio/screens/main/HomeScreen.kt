package com.chordio.screens.main

import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.chordio.components.FilterRow
import com.chordio.components.CardsView
import com.chordio.components.LoadingView
import com.chordio.screens.viewsong.DetailedSongView
import com.chordio.viewmodels.MainViewModel
import com.chordio.viewmodels.auth.AuthViewModel
import com.chordio.viewmodels.main.HomeViewModel
import com.chordio.viewmodels.main.SearchViewModel
import com.chordio.viewmodels.seconds.SongViewModel
import com.chordio.viewmodels.user.UserViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    mainViewModel: MainViewModel,
    searchViewModel: SearchViewModel,
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel,
    navController: NavController,
    songViewModel: SongViewModel
) {
    val selectedSongId by homeViewModel.selectedSongId.collectAsState()
    val songList by homeViewModel.songList.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    val selectedTitle = remember { mutableStateOf<String?>(null) }
    var artistMode by remember { mutableStateOf(false) }
    val artistList by searchViewModel.artistList.collectAsState()
    val isMenuOpen by mainViewModel.isMenuOpen
    val isFullScreenState by homeViewModel.isFullScreen.collectAsState()
    var topBarOffset by rememberSaveable { mutableFloatStateOf(0f) }
    var showNoResults by remember { mutableStateOf(false) }
    val isFullScreen = remember { mutableStateOf(false) }
    val searchResults by searchViewModel.searchResults.collectAsState()
    val favoriteGenres = remember { mutableStateListOf<String>() }

    LaunchedEffect(isFullScreen.value) {
        mainViewModel.setTopBarVisible(!isFullScreen.value)
        mainViewModel.setBottomBarVisible(!isFullScreen.value)
    }

    LaunchedEffect(Unit) {
        val userId = authViewModel.getUserId()
        if (userId != null) {
            userViewModel.fetchTopGenres(userId) { genres ->
                favoriteGenres.clear()
                favoriteGenres.addAll(genres)
                Log.d("UI", "Loaded favorite genres: $genres")
            }
        }
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
                searchViewModel.searchByGenre(selectedFilter)
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

    LaunchedEffect(selectedFilter) {
        artistMode = selectedFilter == "Artists"

        when (selectedFilter) {
            "Artists" -> homeViewModel.getAllArtists()
            "All" -> {
                searchViewModel.clearSearchResults()
                homeViewModel.fetchFilteredSongs("All")
            }
            else -> {
                searchViewModel.searchByGenre(selectedFilter)
            }
        }
    }


    LaunchedEffect(Unit) {
        searchViewModel.fetchAllArtists()
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
                                //.take(7) // μόνο 6 αν θες
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

                            val combinedList = when (selectedFilter) {
                                "All" -> {
                                    val artistCards = artistList.take(6).map { it to "artist:$it" }
                                    val songCards = songList.take(8)
                                    val favoritesCard = if (favoriteGenres.isNotEmpty())
                                        listOf("Αγαπημένα Τραγούδια" to "artist:Αγαπημένα Τραγούδια")
                                    else emptyList()

                                    artistCards + songCards + favoritesCard
                                }

                                else -> searchResults.map {
                                    "${it.first} - ${it.second}" to it.second
                                }
                            }




                            Box(modifier = Modifier.padding(bottom = 40.dp)) {
                                CardsView(
                                    songList = combinedList,
                                    homeViewModel = homeViewModel,
                                    selectedTitle = selectedTitle,
                                    onSongClick = { tag ->
                                        if (tag.startsWith("artist:")) {
                                            val artist = tag.removePrefix("artist:")
                                            navController.navigate("artist/${Uri.encode(artist)}")
                                        } else if (tag.isNotBlank()) {
                                            Log.d("CardsView", "✅ Selecting song ID: $tag")
                                            homeViewModel.selectSong(tag)
                                        } else {
                                            Log.w("CardsView", "⚠️ Attempted to select empty song ID — ignored")
                                        }
                                    }
                                )



                        }
                    }
                    }

                    else -> {
                        DetailedSongView(
                            songViewModel = songViewModel,
                            songId = selectedSongId!!,
                            onBack = {
                                homeViewModel.clearSelectedSong()
                                homeViewModel.setFullScreen(false)
                            },
                            navController = navController,
                            mainViewModel = mainViewModel,
                            homeViewModel = homeViewModel,
                            userViewModel = userViewModel,
                            authViewModel = authViewModel
                        )
                    }
                }
            }
        }
    }

}
