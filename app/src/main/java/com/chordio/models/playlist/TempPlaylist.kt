package com.chordio.models.playlist


data class TempPlaylist(
    val userId: String = "",
    val songs: List<String> = emptyList(),
    val currentSongId: String = ""
)
