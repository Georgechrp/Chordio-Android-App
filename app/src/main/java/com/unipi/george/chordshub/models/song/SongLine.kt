package com.unipi.george.chordshub.models.song

import androidx.annotation.Keep

@Keep
data class SongLine(
    var lineNumber: Int = 0,
    var text: String = "",
    var chords: List<ChordPosition> = emptyList(),
    var chordLine: String? = null
)


data class ChordPosition(
    var chord: String = "",
    var position: Int = 0
)


fun generateChordLine(text: String, word: String, chord: String): String? {
    val index = text.indexOf(word)
    return if (index != -1) {
        " ".repeat(index) + chord
    } else null
}

