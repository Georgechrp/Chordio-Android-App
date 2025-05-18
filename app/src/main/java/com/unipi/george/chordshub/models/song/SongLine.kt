package com.unipi.george.chordshub.models.song

import androidx.annotation.Keep

@Keep
data class SongLine(
    val lineNumber: Int,
    val text: String,
    val chords: List<ChordPosition> = emptyList(),
    val chordLine: String? = null
)

data class ChordPosition(
    val chord: String,
    val position: Int
)

fun generateChordLine(text: String, word: String, chord: String): String? {
    val index = text.indexOf(word)
    return if (index != -1) {
        " ".repeat(index) + chord
    } else null
}

