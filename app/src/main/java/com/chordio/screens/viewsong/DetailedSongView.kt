package com.chordio.screens.viewsong

import android.annotation.SuppressLint
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
import com.chordio.models.song.SongLine
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
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
import com.chordio.R
import com.chordio.components.CardsView
import com.chordio.components.LoadingView
import com.chordio.models.song.ChordPosition
import com.chordio.repository.firestore.SongRepository
import com.chordio.repository.firestore.TempPlaylistRepository
import com.chordio.sharedpreferences.TransposePreferences
import com.chordio.utils.QRCodeDialog
import com.chordio.utils.uploadTestSong
import com.chordio.viewmodels.main.HomeViewModel
import com.chordio.viewmodels.MainViewModel
import com.chordio.viewmodels.auth.AuthViewModel
import com.chordio.viewmodels.main.LibraryViewModel
import com.chordio.viewmodels.seconds.SongViewModel
import com.chordio.viewmodels.seconds.TempPlaylistViewModel
import com.chordio.viewmodels.user.UserViewModel
import kotlinx.coroutines.delay


@Composable
fun DetailedSongView(
    songViewModel: SongViewModel,
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

   /* val songViewModel: SongViewModel = viewModel(
        factory = SongViewModelFactory(SongRepository(FirebaseFirestore.getInstance()))
    )*/
    val isLoading by songViewModel.isLoading.collectAsState()
    val songState by songViewModel.songState.collectAsState()


    LaunchedEffect(Unit) {
        val userId = authViewModel.getUserId()
        if (userId != null) {
           // val success = uploadTestSong(SongRepository(FirebaseFirestore.getInstance()), userId)
            //println("Upload success: $success")
        }
    }


    LaunchedEffect(songState) {
        if (userId != null && songState != null) {
            songViewModel.onSongOpened(userId, songState!!)
        }
    }
    LaunchedEffect(songId) {
        songViewModel.loadSong(songId)
    }

    if (songState == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            LoadingView()
        }
        return
    }
    DisposableEffect(Unit) {
        onDispose {
            songViewModel.clearSongState()
        }
    }


    LaunchedEffect(songId) {
        val savedTranspose = transposePreferences.getTransposeValue(songId)
        transposeValue.value = savedTranspose

        if (songViewModel.songState.value?.id != songId) {
            songViewModel.loadSong(songId)
        }

        userId?.let { id -> userViewModel.addRecentSong(id, songId) }
        songViewModel.registerSongView(songId)
    }




    LaunchedEffect(isScrolling.value, scrollSpeed.floatValue) {
        while (isScrolling.value) {
            val step = (scrollSpeed.floatValue / 10).coerceIn(1f, 20f)
            listState.animateScrollBy(step)
            delay((1000 / scrollSpeed.floatValue).toLong())
        }
    }

   /* LaunchedEffect(Unit) {
        val testSong = userId?.let {
            Song(
                title = "Μια συνουσία μυστική",
                artist = "Πυξ Λαξ",
                key = "Am",
                bpm = 88,
                genres = listOf("Greek", "Rock", "Alternative"),
                createdAt = System.currentTimeMillis().toString(),
                creatorId = it,
                lyrics = listOf(
                    SongLine(
                        lineNumber = 0,
                        text = "Άγνωστοι φόβοι στο μυαλό σου πολεμούν",
                        chords = listOf(ChordPosition("Am", 0), ChordPosition("G", 11), ChordPosition("F", 16), ChordPosition("G", 18), ChordPosition("Am", 20), ChordPosition("G", 29), ChordPosition("F", 41), ChordPosition("G", 43)),
                        chordLine = null
                    ),
                    SongLine(
                        lineNumber = 1,
                        text = "με τις σκιές που θολά σε καλύπτουν κάθε βράδυ",
                        chords = listOf(ChordPosition("Am", 0), ChordPosition("G", 10), ChordPosition("F", 20), ChordPosition("G", 30), ChordPosition("Am", 42), ChordPosition("G", 45), ChordPosition("F", 49), ChordPosition("G", 51)),
                        chordLine = null
                    ),
                    SongLine(
                        lineNumber = 2,
                        text = "Ανυποψίαστοι περαστικοί γελούν μαζί σου",
                        chords = listOf(ChordPosition("Am", 0), ChordPosition("G", 11), ChordPosition("F", 25), ChordPosition("G", 27), ChordPosition("Am", 39), ChordPosition("G", 41), ChordPosition("F", 45), ChordPosition("G", 47)),
                        chordLine = null
                    ),
                    SongLine(
                        lineNumber = 3,
                        text = "μα τους ανέχεσαι παθητικά με τη σιωπή σου",
                        chords = listOf(ChordPosition("Am", 0), ChordPosition("G", 10), ChordPosition("F", 26), ChordPosition("G", 28), ChordPosition("Am", 43), ChordPosition("G", 45), ChordPosition("F", 49), ChordPosition("G", 51)),
                        chordLine = null
                    ),
                    SongLine(
                        lineNumber = 4,
                        text = "Μια συνουσία μυστική μια συνουσία μυστική της διαφθοράς",
                        chords = listOf(ChordPosition("Am", 0), ChordPosition("G", 17), ChordPosition("F", 30), ChordPosition("G", 32), ChordPosition("Am", 35), ChordPosition("G", 51), ChordPosition("F", 55), ChordPosition("G", 60)),
                        chordLine = null
                    ),
                    SongLine(
                        lineNumber = 5,
                        text = "χώροι στενοί μες στη μιζέρια δε νιώθεις τίποτα",
                        chords = listOf(ChordPosition("Am", 0), ChordPosition("G", 24), ChordPosition("F", 37), ChordPosition("G", 44), ChordPosition("Am", 54), ChordPosition("G", 58), ChordPosition("F", 62), ChordPosition("G", 64)),
                        chordLine = null
                    ),
                    SongLine(
                        lineNumber = 6,
                        text = "Πετάς τα ρούχα σου ψηλά γυμνός μες τη χαρά σου",
                        chords = listOf(ChordPosition("Am", 0), ChordPosition("G", 23), ChordPosition("F", 35), ChordPosition("G", 38), ChordPosition("Am", 42), ChordPosition("G", 54), ChordPosition("F", 57), ChordPosition("G", 60)),
                        chordLine = null
                    ),
                    SongLine(
                        lineNumber = 7,
                        text = "κρατάς τη λύπη σου μακριά δε νιώθεις τίποτα",
                        chords = listOf(ChordPosition("Am", 0), ChordPosition("G", 20), ChordPosition("F", 34), ChordPosition("G", 38), ChordPosition("Am", 49), ChordPosition("G", 53), ChordPosition("F", 56), ChordPosition("G", 58)),
                        chordLine = null
                    )
                )
            )
        }

        testSong?.let { song ->
            val firestore = FirebaseFirestore.getInstance()
            val docId = "pyx_lax_den_niotheis_tipota"

            firestore.collection("songs").document(docId).set(song)
                .addOnSuccessListener {
                    Log.d("Upload", " Uploaded 'Δεν Νιώθεις Τίποτα' with ID: $docId")
                }
                .addOnFailureListener { e ->
                    Log.e("Upload", "❌ Failed to upload 'Δεν Νιώθεις Τίποτα': $e")
                }
        }
    }*/






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
        if (isLoading || songState == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingView()
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
                            homeViewModel = homeViewModel,
                            userId = userId
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
    homeViewModel: HomeViewModel,
    userId: String?
) {
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
                    ChordText(songLine = line, onChordClick = { /* Handle if needed */ })
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

                    /*Button(
                        onClick = {
                            saveCardContentAsPdf(context, songTitle, songLyrics)
                            showDialog.value = false
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(stringResource(R.string.Save_as_pdf_text))
                    }*/
                    Button(
                        onClick = {
                            showAddToPlaylistDialog.value = true
                            showDialog.value = false
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(stringResource(R.string.add_to_playlist))
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

fun getNewKey(originalChord: String, transpose: Int): String {
    // Πλήρης λίστα ημιτονίων (με διέσεις και υφέσεις)
    val semitoneMap = listOf(
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    )
    val flatToSharp = mapOf("Db" to "C#", "Eb" to "D#", "Gb" to "F#", "Ab" to "G#", "Bb" to "A#")

    // Regex για να διαχωρίσουμε root note + remainder
    val match = Regex("^([A-Ga-g][b#]?)(.*)").find(originalChord) ?: return originalChord
    var (root, suffix) = match.destructured
    root = root.replaceFirstChar { it.uppercaseChar() } // normalize e.g. "a" → "A"

    // Αν είναι ύφεση, μετατρέπουμε σε δίεση
    if (root in flatToSharp) root = flatToSharp[root]!!

    val index = semitoneMap.indexOf(root)
    if (index == -1) return originalChord

    val newIndex = (index + transpose + 12) % 12
    val newRoot = semitoneMap[newIndex]

    return newRoot + suffix
}



@Composable
fun ChordText(songLine: SongLine, onChordClick: (String) -> Unit) {
    val chordLine = generateChordLine(songLine.text, songLine.chords)
    val lyricsLine = songLine.text

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 4.dp)) {

        Text(
            text = chordLine,
            fontSize = 16.sp,
            color = Color.Red,
            fontFamily = FontFamily.Monospace
        )

        Text(
            text = lyricsLine,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace
        )
    }
}
fun generateChordLine(text: String, chords: List<ChordPosition>): String {
    val lineLength = text.length
    val chordLineArray = CharArray(lineLength.coerceAtLeast(1)) { ' ' }

    for (chord in chords.sortedBy { it.position }) {
        val pos = chord.position.coerceIn(0, lineLength - 1)
        val chordText = chord.chord

        // Τοποθετούμε το chord στο string, χωρίς να ξεχειλίζει
        for (i in chordText.indices) {
            if (pos + i < chordLineArray.size) {
                chordLineArray[pos + i] = chordText[i]
            }
        }
    }

    return String(chordLineArray)
}


