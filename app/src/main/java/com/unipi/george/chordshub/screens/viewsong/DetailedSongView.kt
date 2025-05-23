package com.unipi.george.chordshub.screens.viewsong

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.unipi.george.chordshub.models.song.SongLine
import com.unipi.george.chordshub.models.song.Song
import com.unipi.george.chordshub.utils.saveCardContentAsPdf
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.unipi.george.chordshub.R
import com.unipi.george.chordshub.components.CardsView
import com.unipi.george.chordshub.repository.firestore.SongRepository
import com.unipi.george.chordshub.repository.firestore.TempPlaylistRepository
import com.unipi.george.chordshub.sharedpreferences.TransposePreferences
import com.unipi.george.chordshub.utils.QRCodeDialog
import com.unipi.george.chordshub.viewmodels.main.HomeViewModel
import com.unipi.george.chordshub.viewmodels.MainViewModel
import com.unipi.george.chordshub.viewmodels.SongViewModelFactory
import com.unipi.george.chordshub.viewmodels.auth.AuthViewModel
import com.unipi.george.chordshub.viewmodels.main.LibraryViewModel
import com.unipi.george.chordshub.viewmodels.seconds.SongViewModel
import com.unipi.george.chordshub.viewmodels.seconds.TempPlaylistViewModel
import com.unipi.george.chordshub.viewmodels.user.UserViewModel
import kotlinx.coroutines.delay


@Composable
fun DetailedSongView(
    songId: String,
    onBack: () -> Unit,
    repository: TempPlaylistRepository = TempPlaylistRepository(FirebaseFirestore.getInstance()),
    navController: NavController,
    mainViewModel: MainViewModel,
    homeViewModel: HomeViewModel,
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel
) {

    val transposeValue = remember { mutableStateOf(0) }
    val isScrolling = remember { mutableStateOf(false) }
    val scrollSpeed = remember { mutableFloatStateOf(30f) }
    val isSpeedControlVisible = remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val showDialog = remember { mutableStateOf(false) }
    val showQRCodeDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val transposePreferences = remember { TransposePreferences(context) }
    val tempPlaylistViewModel = remember { TempPlaylistViewModel(repository) }
    val userId = authViewModel.getUserId()
    val showAddToPlaylistDialog = remember { mutableStateOf(false) }
    val isFullScreen = remember { mutableStateOf(false) }
    val songViewModel: SongViewModel = viewModel(
        factory = SongViewModelFactory(SongRepository(FirebaseFirestore.getInstance()))
    )
    val songState by songViewModel.songState.collectAsState()



    LaunchedEffect(songId) {
        val savedTranspose = transposePreferences.getTransposeValue(songId)
        transposeValue.value = savedTranspose

        songViewModel.loadSong(songId)

        userId?.let { id ->
            userViewModel.addRecentSong(id, songId)
        }

        songViewModel.registerSongView(songId)
    }


    LaunchedEffect(isScrolling.value, scrollSpeed.floatValue) {
        while (isScrolling.value) {
            val step = (scrollSpeed.floatValue / 10).coerceIn(1f, 20f)
            listState.animateScrollBy(step)
            delay((1000 / scrollSpeed.floatValue).toLong())
        }
    }

    LaunchedEffect(songId) {
       // songRepo.addSampleSongs()
    }



    fun applyTranspose() {
        val updated = songState?.lyrics?.map { line ->
            line.copy(
                chords = line.chords.map { chord ->
                    chord.copy(chord = getNewKey(chord.chord, transposeValue.value))
                }
            )
        }?.let {
            songState?.copy(lyrics = it)
        }

        updated?.let {
            songViewModel.updateLocalSong(it)
        }
        transposePreferences.saveTransposeValue(songId, transposeValue.value)


    }

    LaunchedEffect(isFullScreen.value) {
        mainViewModel.setBottomBarVisible(!isFullScreen.value)
        mainViewModel.setTopBarVisible(!isFullScreen.value)
    }
    BackHandler {
        if (isFullScreen.value) {
            isFullScreen.value = false
            mainViewModel.setBottomBarVisible(true)
            mainViewModel.setTopBarVisible(true)
        } else {
            onBack()
        }
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background )
            .clickable {
                isFullScreen.value = !isFullScreen.value
            }

    )
    {
        if (songState == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading...")
            }
        } else {
            val songData = songState!!

            Card(
                modifier = if (isFullScreen.value) Modifier.fillMaxSize() else Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(if (isFullScreen.value) 0.dp else 16.dp),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isFullScreen.value) 0.dp else 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SongInfoPlace(
                            title = songData.title ?: "No Title",
                            artist = songData.artist ?: "Unknown Artist",
                            isFullScreen = isFullScreen.value,
                            navController ,
                            modifier = Modifier.weight(1f)
                        )
                        OptionsPlace(
                            isScrolling = isScrolling,
                            isSpeedControlVisible = isSpeedControlVisible,
                            showDialog = showDialog,
                            showQRCodeDialog = showQRCodeDialog,
                            tempPlaylistViewModel = tempPlaylistViewModel,
                            homeViewModel = homeViewModel
                        )

                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ControlSpeed(scrollSpeed, isSpeedControlVisible)

                    Spacer(modifier = Modifier.height(8.dp))

                    SongLyricsView(songLines = songData.lyrics ?: emptyList(), listState = listState)

                    OptionsDialog(
                        showDialog = showDialog,
                        transposeValue = transposeValue,
                        onTransposeChange = {
                            applyTranspose()
                        },
                        context = LocalContext.current,
                        songTitle = songState?.title ?: "Untitled",
                        songLyrics = songState?.lyrics ?: emptyList(),
                        showAddToPlaylistDialog = showAddToPlaylistDialog

                    )
                    QRCodeDialog(showQRCodeDialog, songId)
                    if (showAddToPlaylistDialog.value) {
                        val libraryViewModel: LibraryViewModel = viewModel()
                        val playlists by libraryViewModel.playlists.collectAsState()
                        val selectedPlaylist = remember { mutableStateOf<String?>(null) }

                        AlertDialog(
                            onDismissRequest = { showAddToPlaylistDialog.value = false },
                            title = { Text("Επιλογή Playlist") },
                            text = {
                                Column {
                                    if (playlists.isEmpty()) {
                                        Text("Δεν υπάρχουν playlists.")
                                    } else {
                                        playlists.keys.forEach { playlistName ->
                                            Text(
                                                text = playlistName,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        selectedPlaylist.value = playlistName
                                                    }
                                                    .padding(8.dp),
                                                color = if (selectedPlaylist.value == playlistName)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))

                                    OutlinedButton(
                                        onClick = {
                                            // Δημιουργεί νέα playlist με default όνομα
                                            val baseName = "My Playlist"
                                            val existingNames = playlists.keys
                                            var counter = 1
                                            var newName = "$baseName #$counter"
                                            while (newName in existingNames) {
                                                counter++
                                                newName = "$baseName #$counter"
                                            }

                                            libraryViewModel.createPlaylist(newName) { success ->
                                                if (success) {
                                                    selectedPlaylist.value = newName
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("➕ Δημιουργία νέας playlist")
                                    }
                                }


                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        selectedPlaylist.value?.let { playlist ->
                                            songState?.title?.let { title ->
                                                libraryViewModel.addSongToPlaylist(playlist, title) {
                                                    showAddToPlaylistDialog.value = false
                                                }
                                            }
                                        }
                                    }
                                ) {
                                    Text("Προσθήκη")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showAddToPlaylistDialog.value = false
                                }) {
                                    Text("Άκυρο")
                                }
                            }

                        )

                    }

                }
            }
        }
    }

}




@Composable
fun SongInfoPlace(
    title: String,
    artist: String,
    isFullScreen: Boolean,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = if (isFullScreen) 18.sp else 16.sp
        )
        Text(
            text = artist,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = if (isFullScreen) 16.sp else 14.sp,
                textDecoration = TextDecoration.Underline,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.clickable {
                navController.navigate("artist/$artist")
            }
        )
    }
}


@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OptionsPlace(
    isScrolling: MutableState<Boolean>,
    isSpeedControlVisible: MutableState<Boolean>,
    showDialog: MutableState<Boolean>,
    showQRCodeDialog: MutableState<Boolean>,
    tempPlaylistViewModel : TempPlaylistViewModel,
    homeViewModel: HomeViewModel
) {
    val userId = UserViewModel().userId
    LaunchedEffect(userId) {
        if (userId != null) {
            tempPlaylistViewModel.loadPlaylist(userId)
        }
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .combinedClickable(
                    onClick = { isScrolling.value = !isScrolling.value },
                    onLongClick = { isSpeedControlVisible.value = true }
                )
                .padding(8.dp)
        ) {
            Icon(
                imageVector = if (isScrolling.value) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isScrolling.value) "Pause Auto-Scroll" else "Start Auto-Scroll"
            )
        }

        Box(
            modifier = Modifier
                .clickable { showQRCodeDialog.value = true }
                .padding(3.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.generateqrcode),
                contentDescription = "Share via QR",
                modifier = Modifier.size(24.dp)
            )
        }

        IconButton(onClick = {
            tempPlaylistViewModel.showBottomSheet()
        }) {
            Icon(Icons.Default.QueueMusic, contentDescription = "Playlist")
        }

        IconButton(onClick = { showDialog.value = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More Options"
            )
        }
        val showBottomSheet by tempPlaylistViewModel.isBottomSheetVisible.collectAsState()

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    tempPlaylistViewModel.hideBottomSheet()
                }
            ) {
                CardsView(
                    songList = tempPlaylistViewModel.state.value.songIds.map { songId -> Pair(songId, songId) },
                    homeViewModel = homeViewModel,
                    selectedTitle = remember { mutableStateOf<String?>(null) }
                )

            }
        }
    }

}


@Composable
fun ControlSpeed(
    scrollSpeed: MutableState<Float>,
    isSpeedControlVisible: MutableState<Boolean>
){
    if (isSpeedControlVisible.value) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.scroll_speed_text), fontSize = 14.sp)
                IconButton(onClick = { isSpeedControlVisible.value = false }) {
                    Text(stringResource(R.string.X_Text))
                }
            }
            Slider(
                value = scrollSpeed.value,
                onValueChange = { scrollSpeed.value = it },
                valueRange = 10f..100f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SongLyricsView(
    songLines: List<SongLine>,
    listState: LazyListState
) {
    val snackbarHostState = remember { mutableStateOf<String?>(null) }

    Box {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            items(songLines) { line ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    RenderSongLine(songLine = line)

                }
            }
        }

        snackbarHostState.value?.let { message ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter),
                action = {
                    Text("OK", modifier = Modifier.clickable { snackbarHostState.value = null })
                }
            ) {
                Text(message)
            }
        }
    }
}

@Composable
fun OptionsDialog(
    showDialog: MutableState<Boolean>,
    transposeValue: MutableState<Int>,
    onTransposeChange: () -> Unit,
    context: Context,
    songTitle: String,
    songLyrics: List<SongLine>,
    showAddToPlaylistDialog : MutableState<Boolean>
) {

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(stringResource((R.string.Options_header))) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.Transpose_by_text), fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                if (transposeValue.value > -11) {
                                    transposeValue.value -= 1
                                    onTransposeChange()
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("🔽")
                        }

                        TextField(
                            value = transposeValue.value.toString(),
                            onValueChange = {},
                            singleLine = true,
                            readOnly = true,
                            modifier = Modifier.width(80.dp)
                        )

                        Button(
                            onClick = {
                                if (transposeValue.value < 11) {
                                    transposeValue.value += 1
                                    onTransposeChange()
                                }
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("🔼")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            saveCardContentAsPdf(context, songTitle, songLyrics)
                            showDialog.value = false
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(stringResource(R.string.Save_as_pdf_text))
                    }
                    Button(
                        onClick = {
                            showAddToPlaylistDialog.value = true
                            showDialog.value = false
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Add to Playlist")
                    }

                }
            },
            confirmButton = {
                Text(
                    text = "OK",
                    modifier = Modifier.clickable { showDialog.value = false }
                )
            }
        )
    }
}

fun getNewKey(originalKey: String, transpose: Int): String {
    val sharpNotes = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    // Βρίσκουμε το index της συγχορδίας
    val currentIndex = sharpNotes.indexOf(originalKey)
    if (currentIndex == -1) return originalKey

    // Υπολογισμός νέου index με circular logic
    val newIndex = (currentIndex + transpose + 12) % 12

    return sharpNotes[newIndex]
}


@Composable
fun ChordText(
    songLine: SongLine,
    onChordClick: (String) -> Unit
) {
    val text = songLine.text
    val chordsInLine = songLine.chords.sortedBy { it.position }

    val annotatedString = buildAnnotatedString {
        var currentPos = 0

        chordsInLine.forEach { chord ->
            val relativePosition = chord.position.coerceAtMost(text.length)
            while (currentPos < relativePosition) {
                append(" ")
                currentPos++
            }
            pushStringAnnotation(tag = "chord", annotation = chord.chord)
            withStyle(style = SpanStyle(color = Color.Red, fontSize = 18.sp)) {
                append(chord.chord)
            }
            pop()
            currentPos += chord.chord.length + 1
        }
        append("\n$text")
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Start),
        modifier = Modifier.fillMaxWidth().padding(4.dp),
        onClick = { offset ->
            val clickedAnnotations = annotatedString.getStringAnnotations(
                tag = "chord",
                start = offset,
                end = offset
            )
            if (clickedAnnotations.isNotEmpty()) {
                onChordClick(clickedAnnotations.first().item)
            }
        }
    )
}


@Composable
fun RenderSongLine(songLine: SongLine) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        songLine.chordLine?.let {
            Text(
                text = it,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = songLine.text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

