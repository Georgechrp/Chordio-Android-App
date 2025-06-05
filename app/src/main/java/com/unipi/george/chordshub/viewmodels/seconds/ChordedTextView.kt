package com.unipi.george.chordshub.viewmodels.seconds

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore

// ---------- Models ----------
data class WordChord(
    val word: String = "",
    val chord: String? = null
)


data class SongLineWordBased(
    val words: List<WordChord> = emptyList()
)


data class Song(
    var id: String = "",
    val title: String = "",
    val artist: String = "",
    val lyrics: List<SongLineWordBased> = emptyList()
)

// ---------- ViewModel ----------
class SongViewModel2 : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    var song by mutableStateOf<Song?>(null)
        private set

    fun fetchSong(songId: String) {
        db.collection("songs").document(songId).get()
            .addOnSuccessListener { doc ->
                doc?.toObject(Song::class.java)?.let {
                    Log.d("Firestore", "✅ Song fetched: ${it.title}")
                    song = it
                }
            }
            .addOnFailureListener {
                Log.e("Firestore", "❌ Failed to fetch song", it)
            }
    }

    fun uploadTestSong(): String {
        val testSong = Song(
            title = "Wonderwall",
            artist = "Oasis",
            lyrics = listOf(
                SongLineWordBased(
                    words = listOf(
                        WordChord("Today", "Em"),
                        WordChord("is"),
                        WordChord("gonna"),
                        WordChord("be"),
                        WordChord("the"),
                        WordChord("day", "G"),
                        WordChord("that"),
                        WordChord("they're"),
                        WordChord("gonna"),
                        WordChord("throw", "D"),
                        WordChord("it"),
                        WordChord("back", "A7sus4"),
                        WordChord("to"),
                        WordChord("you")
                    )
                ),
                SongLineWordBased(
                    words = listOf(
                        WordChord("By", "Em"),
                        WordChord("now"),
                        WordChord("you"),
                        WordChord("should've"),
                        WordChord("somehow", "G"),
                        WordChord("realized"),
                        WordChord("what"),
                        WordChord("you"),
                        WordChord("gotta", "D"),
                        WordChord("do", "A7sus4")
                    )
                )
            )
        )
        val docRef = db.collection("songs").document()
        testSong.id = docRef.id
        docRef.set(testSong)
        return testSong.id
    }
}

// ---------- UI ----------
@Composable
fun WordBasedChordLine(line: SongLineWordBased) {
    val mono = TextStyle(fontFamily = FontFamily.Monospace)

    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        line.words.forEach { wordChord ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(IntrinsicSize.Min)
            ) {
                Text(
                    text = wordChord.chord ?: "",
                    style = mono.copy(color = Color.Red)
                )
                Text(
                    text = wordChord.word,
                    style = mono
                )
            }
        }
    }
}

@Composable
fun ChordedTextView(song: Song) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(song.title, style = MaterialTheme.typography.titleLarge)
        Text(song.artist, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(12.dp))

        song.lyrics.forEach { line ->
            WordBasedChordLine(line)
        }
    }
}

@Composable
fun SongCard2(songId: String, viewModel: SongViewModel2 = viewModel()) {
    val song = viewModel.song

    LaunchedEffect(songId) {
        viewModel.fetchSong(songId)
    }
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        song?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight() // ✅ ή βάλε fixed height αν χρειάζεται
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    ChordedTextView(song = it)
                }
            }

        }
    }
}
