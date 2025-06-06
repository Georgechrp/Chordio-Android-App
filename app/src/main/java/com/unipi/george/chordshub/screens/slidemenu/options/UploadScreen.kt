package com.unipi.george.chordshub.screens.slidemenu.options

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.unipi.george.chordshub.R
import com.unipi.george.chordshub.models.song.ChordPosition
import com.unipi.george.chordshub.models.song.Song
import com.unipi.george.chordshub.models.song.SongLine
import com.unipi.george.chordshub.viewmodels.seconds.UploadViewModel
import com.unipi.george.chordshub.viewmodels.user.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(navController: NavController, userViewModel: UserViewModel) {
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }
    var bpm by remember { mutableStateOf("") }
    var lyrics by remember { mutableStateOf("") }
    var chords by remember { mutableStateOf("") }

    val uploadViewModel: UploadViewModel = viewModel()
    val uploadSuccess by uploadViewModel.uploadSuccess.collectAsState()

    LaunchedEffect(uploadSuccess) {
        if (uploadSuccess) {
            uploadViewModel.resetStatus()
            navController.popBackStack()
        }
    }

    val errorMessage by uploadViewModel.errorMessage.collectAsState()
    errorMessage?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 8.dp)
        )
    }


    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.add_song_text),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },

                        navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Πίσω")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.song_title), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = artist,
                onValueChange = { artist = it },
                label = { Text(stringResource(R.string.song_artist)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = key,
                onValueChange = { key = it },
                label = { Text(stringResource(R.string.song_key)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = bpm,
                onValueChange = { bpm = it },
                label =  { Text("BPM") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = lyrics,
                onValueChange = { lyrics = it },
                label = { Text(stringResource(R.string.song_lyrics)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = chords,
                onValueChange = { chords = it },
                label = { Text(stringResource(R.string.song_chords)) },
                modifier = Modifier.fillMaxWidth()
            )


            Button(
                onClick = {
                    val currentUserId = userViewModel.userId ?: return@Button

                    val songId = title.replace(" ", "_").lowercase()

                    val songLines = lyrics.split("\n").mapIndexed { index, line ->
                        SongLine(
                            lineNumber = index + 1,
                            text = line,
                            chords = chords.split(",").mapNotNull { chordData ->
                                val parts = chordData.split("-")
                                if (parts.size == 2) {
                                    val chord = parts[0]
                                    val position = parts[1].toIntOrNull()
                                    if (position != null) ChordPosition(chord, position) else null
                                } else null
                            }
                        )
                    }

                    val song = Song(
                        id = songId,
                        title = title,
                        artist = artist,
                        key = key,
                        bpm = bpm.toIntOrNull() ?: 0,
                        genres = listOf("User Added"),
                        createdAt = System.currentTimeMillis().toString(),
                        creatorId = currentUserId,
                        lyrics = songLines
                    )

                    uploadViewModel.uploadSong(songId, song)
                }
                ,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.submit_song),
                    color = MaterialTheme.colorScheme.onPrimary
                )

            }
        }
    }
}
