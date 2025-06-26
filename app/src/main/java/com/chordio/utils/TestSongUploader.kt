// path: com/chordio/utils/TestSongUploader.kt
package com.chordio.utils

import com.chordio.models.song.ChordPosition
import com.chordio.models.song.Song
import com.chordio.models.song.SongLine
import com.chordio.repository.firestore.SongRepository

suspend fun uploadTestSong(songRepository: SongRepository, userId: String): Boolean {
    val testSong = Song(
        title = "Εκείνη",
        artist = "Foivos Delivorias",
        key = "F",
        bpm = 70,
        genres = listOf("Greek", "Ballad"),
        createdAt = System.currentTimeMillis().toString(),
        creatorId = userId,
        lyrics = listOf(
            SongLine(
                lineNumber = 0,
                text = "Γεννιέσαι την έχεις μητέρα πηδάς στον αέρα σκας στο πάτωμα",
                chords = listOf(
                    ChordPosition("F", 0),
                    ChordPosition("Em", 22),
                    ChordPosition("Dm", 36),
                    ChordPosition("C", 49)
                )
            ),
            SongLine(
                lineNumber = 1,
                text = "Εκείνη σε βάζει στην κούνια στα μάτια σαπούνια και γαλάκτωμα",
                chords = emptyList()
            ),
            SongLine(
                lineNumber = 2,
                text = "Σου δείχνει πως κάνει η πάπια και μοιάζει με κάποια που ’χες γκόμενα",
                chords = emptyList()
            ),
            SongLine(
                lineNumber = 3,
                text = "Στο μέλλον με τ’ άσπρα φωτάκια και με τ’ αστεράκια τα φλεγόμενα",
                chords = emptyList()
            )
        )
    )

    val customId = "ekeini-foivos-delivorias"

    return try {
        val success = songRepository.uploadSongWithId(testSong, customId)
        println("✅ Uploaded test song with ID: $customId")
        success
    } catch (e: Exception) {
        println("❌ Upload error: ${e.message}")
        false
    }
}
