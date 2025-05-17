package com.unipi.george.chordshub.models.song

data class Song(
    val title: String?,
    val artist: String?,
    val key: String?,
    val bpm: Int?,
    val genres: List<String>? = null,
    val createdAt: String?,
    val creatorId: String?,
    val lyrics: List<SongLine>,
)


data class SongCardItem(
    val title: String,
    val artist: String,
    val id: String
)