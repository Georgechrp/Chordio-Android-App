package com.unipi.george.chordshub.screens.main

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.unipi.george.chordshub.R
import com.unipi.george.chordshub.screens.viewsong.DetailedSongView
import com.unipi.george.chordshub.utils.QRCodeScannerButton
import com.unipi.george.chordshub.viewmodels.main.SearchViewModel
import com.unipi.george.chordshub.viewmodels.main.HomeViewModel
import com.unipi.george.chordshub.viewmodels.MainViewModel
import com.unipi.george.chordshub.viewmodels.user.UserViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    mainViewModel: MainViewModel,
    homeViewModel: HomeViewModel,
    onMenuClick: () -> Unit,
    navController: NavController,
    isFullScreen: Boolean,
    onFullScreenChange: (Boolean) -> Unit,
    profileImageUrl: String?
) {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedSongId by viewModel.selectedSongId.collectAsState()
    val randomSongs by viewModel.randomSongs.collectAsState()
    val isMenuOpen by mainViewModel.isMenuOpen
    val userViewModel: UserViewModel = viewModel()

    LaunchedEffect(searchText.text) {
        if (searchText.text.isEmpty()) {
            viewModel.clearSearchResults()
        }
    }
    LaunchedEffect(isFullScreen) {
        if (!isFullScreen) {
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


    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background ).padding(top = if (isFullScreen) 0.dp else 56.dp)) {
        if (selectedSongId == null) {
            SearchContent(
                searchText = searchText,
                onSearchTextChange = {
                    searchText = it
                    viewModel.searchSongs(it.text)
                },
                searchResults = searchResults,
                onSongSelect = { viewModel.selectSong(it) },
                viewModel = viewModel,
                isFullScreen = isFullScreen,
                randomSongs = randomSongs
            )
        } else {
            DetailedSongView(
                songId = selectedSongId!!,
                isFullScreenState = isFullScreen,
                onBack = {
                    onFullScreenChange(false)
                    viewModel.clearSelectedSong()
                },
                navController = navController,
                mainViewModel = mainViewModel,
                homeViewModel = homeViewModel,
                userViewModel = userViewModel
            )
        }
    }

}


@Composable
fun TopSongsList(
    topSongs: List<Pair<String, String>>,
    onSongSelect: (String) -> Unit
) {
    if (topSongs.isNotEmpty()) {
        Column {
            Text(
                text = "🔥 Δημοφιλέστερα Τραγούδια",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.padding(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(topSongs) { song ->
                    SongCard(song = song, onSongSelect = onSongSelect)
                }
            }
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
    isFullScreen: Boolean,
    randomSongs: List<Pair<String, String>>
) {
    val topSongs by viewModel.topSongs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(if (isFullScreen) 0.dp else 16.dp)
    ) {
        SearchBar(searchText, onSearchTextChange, viewModel)
        Spacer(modifier = Modifier.height(8.dp))

        if (searchText.text.isEmpty()) {
            TopSongsList(topSongs = topSongs, onSongSelect = onSongSelect)
            Spacer(modifier = Modifier.height(16.dp))

            //RandomSongsList(randomSongs, onSongSelect)
            //Spacer(modifier = Modifier.height(16.dp))
        }

        SearchResultsList(searchResults, onSongSelect)
    }
}

@Composable
fun RandomSongsList(
    randomSongs: List<Pair<String, String>>,
    onSongSelect: (String) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.Discover_something_new_text),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.padding(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(randomSongs) { song ->
                SongCard(song, onSongSelect)
            }
        }
    }
}

@Composable
fun SongCard(song: Pair<String, String>, onSongSelect: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .clickable { onSongSelect(song.second) },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))


    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_music_note),
                contentDescription = "Music Icon",
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 12.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.first,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.next),
                contentDescription = "Go to song",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
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



@Composable
fun SearchResultsList(
    searchResults: List<Triple<String, String, String>>,
    onSongSelect: (String) -> Unit
) {
    LazyColumn {
        items(searchResults) { song ->
            ListItem(
                modifier = Modifier.clickable { onSongSelect(song.second) },
                headlineContent = { Text(song.first, color = MaterialTheme.colorScheme.onSurface)  },
                supportingContent = { Text("Καλλιτέχνης: ${song.second}\n🔍 Αντιστοίχιση: ${song.third}") }
            )
        }
    }
}
