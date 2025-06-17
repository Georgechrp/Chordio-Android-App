package com.chordio.models.song

data class Song(
    var id: String = "",
    var title: String = "",
    var artist: String = "",
    var key: String = "",
    var bpm: Int = 0,
    var genres: List<String> = emptyList(),
    var createdAt: String = "",
    var creatorId: String = "",
    var lyrics: List<SongLine> = emptyList(),
    val viewsCount: Int? = null
)

data class RecentSongEntry(
    val songId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)


data class SongCardItem(
    val title: String,
    val artist: String,
    val id: String
)