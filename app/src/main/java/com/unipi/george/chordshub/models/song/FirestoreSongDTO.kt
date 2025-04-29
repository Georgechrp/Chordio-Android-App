package com.unipi.george.chordshub.models.song

data class FirestoreSongDTO(
    val title: String = "",
    val artist: String = "",
    val bpm: Int = 0,
    val createdAt: String = "",
    val creatorId: String = "",
    val genres: List<String> = emptyList(),
    val key: String = "",
    val lyrics: List<Map<String, Any>> = emptyList()
) {
    fun toSong(): Song {
        return Song(
            title = title,
            artist = artist,
            bpm = bpm,
            createdAt = createdAt,
            creatorId = creatorId,
            genres = genres,
            key = key,
            lyrics = lyrics.mapNotNull { map ->
                try {
                    val lineNumber = (map["lineNumber"] as? Long)?.toInt() ?: return@mapNotNull null
                    val text = map["text"] as? String ?: return@mapNotNull null
                    val chordsRaw = map["chords"] as? List<Map<String, Any>> ?: emptyList()

                    val chords = chordsRaw.mapNotNull { chordMap ->
                        val chord = chordMap["chord"] as? String ?: return@mapNotNull null
                        val position = (chordMap["position"] as? Long)?.toInt() ?: return@mapNotNull null
                        ChordPosition(chord, position)
                    }

                    SongLine(lineNumber, text, chords)
                } catch (e: Exception) {
                    println("❌ Error parsing line: $e")
                    null
                }
            }
        )
    }
}

