package com.chordio.screens.main

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.chordio.R
import com.chordio.components.CardsView
import com.chordio.components.SongCard
import com.chordio.screens.viewsong.DetailedSongView
import com.chordio.utils.QRCodeScannerButton
import com.chordio.viewmodels.main.SearchViewModel
import com.chordio.viewmodels.main.HomeViewModel
import com.chordio.viewmodels.MainViewModel
import com.chordio.viewmodels.auth.AuthViewModel
import com.chordio.viewmodels.user.UserViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    navController: NavController,
    onFullScreenChange: (Boolean) -> Unit,
) {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedSongId by viewModel.selectedSongId.collectAsState()
    val isMenuOpen by mainViewModel.isMenuOpen
    val userViewModel: UserViewModel = viewModel()
    val isFullScreen = remember { mutableStateOf(false) }
    LaunchedEffect(searchText.text) {
        if (searchText.text.isEmpty()) {
            viewModel.clearSearchResults()
        }
    }
    LaunchedEffect(isFullScreen) {
        if (!isFullScreen.value) {
            mainViewModel.setTopBarContent {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.search_text),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    QRCodeScannerButton(viewModel = viewModel)
                }
            }
        } else {
            mainViewModel.setTopBarContent {}
        }
    }
    LaunchedEffect(isFullScreen.value) {
        mainViewModel.setTopBarVisible(!isFullScreen.value)
        mainViewModel.setBottomBarVisible(!isFullScreen.value)
    }


    BackHandler {
        if (isMenuOpen) {
            Log.d("BackHandler", "Back button pressed - Closing Menu")
            mainViewModel.setMenuOpen(false)
        } else if (searchText.text.isNotEmpty()) {
            Log.d("BackHandler", "Back button pressed - Clearing Search")
            searchText = TextFieldValue("")
            viewModel.clearSearchResults()
        } else {
            Log.d("BackHandler", "Back button pressed - Exiting SearchScreen")
            navController.popBackStack()
        }
    }


    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background ).padding(top = if (isFullScreen.value) 0.dp else 56.dp)) {
        if (selectedSongId == null) {
            SearchContent(
                searchText = searchText,
                onSearchTextChange = {
                    searchText = it
                    viewModel.searchSongs(it.text)
                },
                searchResults = searchResults,
                onSongSelect = { viewModel.selectSong(it) },
                viewModel = viewModel
            )
        } else {
            DetailedSongView(
                songId = selectedSongId!!,
                onBack = {
                    onFullScreenChange(false)
                    viewModel.clearSelectedSong()
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


@Composable
fun SearchContent(
    searchText: TextFieldValue,
    onSearchTextChange: (TextFieldValue) -> Unit,
    searchResults: List<Triple<String, String, String>>,
    onSongSelect: (String) -> Unit,
    viewModel: SearchViewModel,
) {
    val topSongs by viewModel.topSongs.collectAsState()
    val genres by viewModel.genres.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // SearchBar
        item {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                SearchBar(searchText, onSearchTextChange, viewModel)
            }
        }


        // Only show TopSongs & Genres when no search query
        if (searchText.text.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.top_song),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }

            items(topSongs) { song ->
                SongCard(
                    title = song.title,
                    artistName = song.artist,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    cardHeight = 80.dp,
                    onClick = { onSongSelect(song.id) }
                )
            }

            if (genres.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.Discover_something_new_text) ,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 600.dp)
                    ) {
                        CardsView(
                            songList = genres.map { genre -> genre to "genre:$genre" },
                            homeViewModel = remember { HomeViewModel() },
                            selectedTitle = remember { mutableStateOf<String?>(null) },
                            columns = 2,
                            onSongClick = { genreId ->
                                viewModel.searchByGenre(genreId.removePrefix("genre:"))
                            }
                        )
                    }
                }

            }
        }

        // Search Results
        items(searchResults) { song ->
            ListItem(
                modifier = Modifier.clickable { onSongSelect(song.second) },
                headlineContent = { Text(song.first, color = MaterialTheme.colorScheme.onSurface) },
                supportingContent = {
                    Text("Καλλιτέχνης: ${song.second}\n🔍 Αντιστοίχιση: ${song.third}")
                }
            )
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}


@Composable
fun SearchBar(
    searchText: TextFieldValue,
    onSearchTextChange: (TextFieldValue) -> Unit,
    viewModel: SearchViewModel
) {
    TextField(
        value = searchText,
        onValueChange = onSearchTextChange,
        label = { Text(
            text = stringResource(R.string.Are_u_looking_for_something_text),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        ) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = {
            //QRCodeScannerButton(viewModel)
        }
    )
}
