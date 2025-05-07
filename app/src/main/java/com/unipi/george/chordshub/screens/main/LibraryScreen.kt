package com.unipi.george.chordshub.screens.main

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.unipi.george.chordshub.R
import com.unipi.george.chordshub.components.FilterRow
import com.unipi.george.chordshub.viewmodels.main.LibraryViewModel
import com.unipi.george.chordshub.viewmodels.MainViewModel
import com.unipi.george.chordshub.viewmodels.main.SearchViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(navController: NavController, mainViewModel: MainViewModel, onMenuClick: () -> Unit, profileImageUrl: String?) {
    val viewModel: LibraryViewModel = viewModel()
    val playlists by viewModel.playlists.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var playlistName by remember { mutableStateOf("") }
    var showAddSongDialog by remember { mutableStateOf(false) }
    var selectedPlaylist by remember { mutableStateOf<String?>(null) }
    var songTitle by remember { mutableStateOf("") }
    val duplicateError = remember { mutableStateOf(false) }
    val showBottomSheet = remember { mutableStateOf(false) }

    var showRenameDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    val profileImage by mainViewModel.profileImageUrl.collectAsState()
    //var selectedFilter by remember { mutableStateOf("All") }
    val selectedFilter by viewModel.selectedFilter.collectAsState()


    val searchViewModel: SearchViewModel = viewModel()
    val searchText = remember { mutableStateOf(TextFieldValue("")) }
    val searchResults by searchViewModel.searchResults.collectAsState()

    LaunchedEffect(searchText.value.text) {
        if (searchText.value.text.isBlank()) {
            searchViewModel.clearSearchResults()
        } else {
            searchViewModel.searchSongs(searchText.value.text)
        }
    }


    LaunchedEffect(Unit) {
        mainViewModel.setTopBarContent {
            Text(stringResource(R.string.Library_text), style = MaterialTheme.typography.headlineSmall)
        }
    }
    LaunchedEffect(Unit) {
        mainViewModel.setTopBarContent {
            Text(stringResource(R.string.Library_text), style = MaterialTheme.typography.headlineSmall)
        }
        viewModel.fetchFilteredPlaylists("All")
    }


    Box(modifier = Modifier.fillMaxSize().padding(top = 56.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FilterRow(
                selectedFilter = selectedFilter,
                onFilterChange = { viewModel.fetchFilteredPlaylists(it) },
                filters = listOf("All", "Artists", "Playlists", "Downloaded")
            )
            Spacer(modifier = Modifier.height(8.dp))


            if (playlists.isEmpty()) {
                Text("Δεν υπάρχουν playlists ακόμα.")
            } else {
                playlists.forEach { (playlist, songs) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .combinedClickable(
                                onClick = {
                                    navController.navigate("playlist_detail/${Uri.encode(playlist)}")
                                },
                                onLongClick = {
                                    selectedPlaylist = playlist
                                    showBottomSheet.value = true
                                }
                            )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(playlist, style = MaterialTheme.typography.bodyLarge)

                                IconButton(onClick = {
                                    selectedPlaylist = playlist
                                    showBottomSheet.value = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More Options"
                                    )
                                }
                            }

                            /*songs.forEach { song ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    //Text(song, style = MaterialTheme.typography.bodyMedium)
                                    *//*TextButton(onClick = {
                                        viewModel.removeSongFromPlaylist(playlist, song) {}
                                    }) {
                                        Text("❌")
                                    }*//*
                                }
                            }*/
                        }
                    }

                }
            }

        }

        // FloatingActionButton
        FloatingActionButton(onClick = {
            val existingNames = playlists.keys

            // Βρίσκουμε το μικρότερο διαθέσιμο όνομα
            var nextNumber = 1
            while (existingNames.contains("My Playlist #$nextNumber")) {
                nextNumber++
            }

            playlistName = "My Playlist #$nextNumber"
            showDialog = true
        },
            modifier = Modifier.padding(bottom = 86.dp, end = 16.dp).align(Alignment.BottomEnd)
        )
        {
            Text("+", style = MaterialTheme.typography.headlineSmall)
        }
    }



    // Διάλογος για δημιουργία playlist
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.Create_a_playlist_text)) },
            text = {
                Column {
                    Text(stringResource(R.string.give_a_name_to_playlist))

                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = {
                            playlistName = it
                            duplicateError.value = false // reset error όταν ο χρήστης αλλάζει κάτι
                        },
                        label = { Text(stringResource(R.string.name_of_playlist_text)) }
                    )
                    if (duplicateError.value) {
                        Text(
                            text = stringResource(R.string.already_exists_the_name_of_playlist),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (playlistName.isNotBlank()) {
                        if (playlistName in playlists.keys) {
                            duplicateError.value = true
                        } else {
                            viewModel.createPlaylist(playlistName) { success ->
                                if (success) {
                                    showDialog = false
                                    playlistName = ""
                                    duplicateError.value = false
                                }
                            }
                        }
                    }
                }) {
                    Text(stringResource(R.string.create_text))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel_button_text))
                }
            }
        )
    }

    if (showAddSongDialog && selectedPlaylist != null) {
        AlertDialog(
            onDismissRequest = { showAddSongDialog = false },
            title = { Text("Αναζήτηση Τραγουδιού") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SearchBar(
                        searchText = searchText.value,
                        onSearchTextChange = { searchText.value = it },
                        viewModel = searchViewModel
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn {
                        items(searchResults) { song ->
                            ListItem(
                                headlineContent = { Text(song.first) },
                                supportingContent = {
                                    Text("Καλλιτέχνης: ${song.second}\n🔍 ${song.third}")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addSongToPlaylist(
                                            selectedPlaylist!!,
                                            song.second
                                        ) { success ->
                                            if (success) {
                                                showAddSongDialog = false
                                                searchText.value = TextFieldValue("")
                                                searchViewModel.clearSearchResults()
                                            }
                                        }
                                    }
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = {
                    showAddSongDialog = false
                    searchText.value = TextFieldValue("")
                    searchViewModel.clearSearchResults()
                }) {
                    Text(stringResource(R.string.cancel_button_text))
                }
            }
        )
    }

    //Playlists's options
    if (showBottomSheet.value && selectedPlaylist != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet.value = false
                selectedPlaylist = null
            },
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = selectedPlaylist ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                TextButton(onClick = {
                    newPlaylistName = selectedPlaylist ?: ""
                    showRenameDialog = true
                    showBottomSheet.value = false
                }) {
                    Text(stringResource(R.string.rename_playlist_text))
                }



                TextButton(onClick = {
                    showAddSongDialog = true
                    showBottomSheet.value = false
                }) {
                    Text(stringResource(R.string.add_song_text2))
                }

                TextButton(onClick = {
                    viewModel.deletePlaylist(selectedPlaylist!!) {}
                    showBottomSheet.value = false
                }) {
                    Text(stringResource(R.string.delete_playlist_text))
                }
            }
        }
    }

    //Rename dialog
    if (showRenameDialog && selectedPlaylist != null) {
        AlertDialog(
            onDismissRequest = {
                showRenameDialog = false
                newPlaylistName = ""
            },
            title = { Text(stringResource(R.string.rename_playlist_text)) },
            text = {
                Column {
                    Text(stringResource(R.string.give_a_name_to_playlist))
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text(stringResource(R.string.new_name_text)) }
                    )
                    if (newPlaylistName in playlists.keys && newPlaylistName != selectedPlaylist) {
                        Text(
                            text = stringResource(R.string.already_exists_the_name_of_playlist),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPlaylistName.isNotBlank()
                        && newPlaylistName != selectedPlaylist
                        && newPlaylistName !in playlists.keys
                    ) {
                        viewModel.renamePlaylist(selectedPlaylist!!, newPlaylistName) {
                            showRenameDialog = false
                            selectedPlaylist = null
                            newPlaylistName = ""
                        }
                    }
                }) {
                    Text(stringResource(R.string.save_button_text))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRenameDialog = false
                    newPlaylistName = ""
                }) {
                    Text(stringResource(R.string.cancel_button_text))
                }
            }
        )
    }

}


