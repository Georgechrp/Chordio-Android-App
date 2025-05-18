package com.unipi.george.chordshub.repository.firestore

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.unipi.george.chordshub.models.song.ChordPosition
import com.unipi.george.chordshub.models.song.FirestoreSongDTO
import com.unipi.george.chordshub.models.song.Song
import com.unipi.george.chordshub.models.song.SongCardItem
import com.unipi.george.chordshub.models.song.SongLine
import com.unipi.george.chordshub.models.song.generateChordLine
import kotlinx.coroutines.tasks.await

class SongRepository(private val db: FirebaseFirestore) {

    suspend fun getSongDataAsync(songId: String): Song? {
        Log.d("Firestore", "Fetching song data for ID: $songId")

        return try {
            val document = db.collection("songs").document(songId).get().await()
            if (!document.exists()) {
                Log.e("Firestore", "No song found with ID: $songId")
                return null
            }

            val title = document.getString("title") ?: "Unknown Title"
            val artist = document.getString("artist") ?: "Unknown Artist"
            val key = document.getString("key") ?: "Unknown Key"
            val bpm = document.getLong("bpm")?.toInt()
            val genres = document.get("genres") as? List<String>
            val createdAt = document.getString("createdAt")
            val creatorId = document.getString("creatorId")

            // 🔹 Μετατροπή lyrics σε List<SongLine>
            val lyricsList = document.get("lyrics") as? List<Map<String, Any>>
            val lyrics = lyricsList?.map { item ->
                SongLine(
                    lineNumber = (item["lineNumber"] as? Long)?.toInt() ?: 0,
                    text = item["text"] as? String ?: "",
                    chords = (item["chords"] as? List<Map<String, Any>>)?.mapNotNull { chord ->
                        val chordName = chord["chord"] as? String
                        val position = (chord["position"] as? Long)?.toInt()
                        if (chordName != null && position != null) ChordPosition(chordName, position) else null
                    } ?: emptyList()
                )
            } ?: emptyList()

            Log.d("Firestore", "✅ Song loaded successfully: $title")
            return Song(
                title = title,
                artist = artist,
                key = key,
                bpm = bpm,
                genres = genres,
                createdAt = createdAt,
                creatorId = creatorId,
                lyrics = lyrics
            )
        } catch (e: Exception) {
            Log.e("Firestore", " Firestore Error: ${e.message}")
            return null
        }
    }

    suspend fun addSongData(songId: String, song: Song) {
        val songMap = hashMapOf(
            "title" to song.title,
            "artist" to song.artist,
            "key" to song.key,
            "bpm" to song.bpm,
            "genres" to song.genres,
            "createdAt" to song.createdAt,
            "creatorId" to song.creatorId,
            "lyrics" to song.lyrics?.map { line ->
                mapOf(
                    "lineNumber" to line.lineNumber,
                    "text" to line.text,
                    "chords" to line.chords.map { chord ->
                        mapOf("chord" to chord.chord, "position" to chord.position)
                    }
                )
            }
        )

        try {
            db.collection("songs").document(songId).set(songMap).await()
            Log.d("Firestore", "✅ Song added successfully: $songId")
        } catch (e: Exception) {
            Log.e("Firestore", " Error adding song", e)
        }
    }

    fun getFilteredSongs(filter: String, callback: (List<Pair<String, String>>) -> Unit) {
        println("🔍 Querying Firestore with filter: $filter")

        // Declare as Query instead of CollectionReference
        var query: Query = db.collection("songs")

        if (filter != "All") {
            // Reassign the variable with the filtered Query
            query = query.whereArrayContains("genres", filter)
        }

        query.get()
            .addOnSuccessListener { result ->
                val songList = result.documents.mapNotNull { doc ->
                    val title = doc.getString("title")
                    val artist = doc.getString("artist") ?: ""
                    val id = doc.id
                    val genres = doc.get("genres") as? List<String>
                    println("Genres for song $title: $genres")
                    if (title != null) "$title - $artist" to id else null
                }
                println("🔥 Firestore returned ${songList.size} results for filter: $filter")
                callback(songList)
            }
            .addOnFailureListener { exception ->
                println(" Firestore error: ${exception.message}")
            }
    }

    fun getRandomSongs(limit: Int, callback: (List<Pair<String, String>>) -> Unit) {
        db.collection("songs")
            .limit(limit.toLong())
            .get()
            .addOnSuccessListener { result ->
                val songList = result.documents.mapNotNull { doc ->
                    val title = doc.getString("title")
                    val id = doc.id
                    if (title != null) title to id else null
                }
                callback(songList)
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching random songs: ${exception.message}")
                callback(emptyList())
            }
    }

    fun getSongsByArtistName(artistName: String, callback: (List<Song>) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("songs")
            .whereEqualTo("artist", artistName)
            .get()
            .addOnSuccessListener { result ->
                println("✅ Fetched ${result.size()} documents for artist: $artistName")
                val songs =  result.mapNotNull {
                    try {
                        it.toObject(FirestoreSongDTO::class.java).toSong()
                    } catch (e: Exception) {
                        println("Error parsing song: ${e.localizedMessage}")
                        null
                    }
                }
                callback(songs)
            }
            .addOnFailureListener { e ->
                println("Firebase query failed: $e")
                callback(emptyList())
            }
    }

    fun getAllArtists(callback: (List<String>) -> Unit) {
        db.collection("songs")
            .get()
            .addOnSuccessListener { result ->
                val artists = result.mapNotNull {
                    it.getString("artist")?.trim()
                }.distinct().sorted()

                callback(artists)
            }
            .addOnFailureListener { e ->
                println(" Failed to fetch artists: $e")
                callback(emptyList())
            }
    }

    suspend fun getSongByTitle(title: String): Song? {
        return try {
            val querySnapshot = db.collection("songs")
                .whereEqualTo("title", title)
                .get()
                .await()

            val document = querySnapshot.documents.firstOrNull()
            document?.toObject(FirestoreSongDTO::class.java)?.toSong()
        } catch (e: Exception) {
            Log.e("Firestore", " Error fetching song by title: ${e.message}")
            null
        }
    }

    suspend fun incrementSongViewCount(songId: String) {
        try {
            val songRef = db.collection("songs").document(songId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(songRef)
                val currentCount = snapshot.getLong("viewsCount") ?: 0
                transaction.update(songRef, "viewsCount", currentCount + 1)
            }.await()
            Log.d("Firestore", "View count incremented for $songId")
        } catch (e: Exception) {
            Log.e("Firestore", "Failed to increment view count: ${e.message}")
        }
    }

    fun getTopSongs(limit: Int, callback: (List<SongCardItem>) -> Unit) {
        db.collection("songs")
            .orderBy("viewsCount", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .addOnSuccessListener { result ->
                val songList = result.documents.mapNotNull { doc ->
                    val title = doc.getString("title")
                    val artist = doc.getString("artist") ?: "Άγνωστος Καλλιτέχνης"
                    val id = doc.id
                    if (title != null) SongCardItem(title, artist, id) else null
                }
                callback(songList)
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching top songs: ${exception.message}")
                callback(emptyList())
            }
    }

    fun getGenres(onResult: (List<String>) -> Unit) {
        db.collection("songs")
            .get()
            .addOnSuccessListener { documents ->
                val genres = documents
                    .mapNotNull { it.get("genres") as? List<String> }
                    .flatten()
                    .toSet()
                    .toList()
                Log.d("Firestore", "✅ Genres fetched: $genres")
                onResult(genres)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to fetch genres: ${e.message}")
                onResult(emptyList())
            }
    }

    fun getSongsByGenre(genre: String, callback: (List<Pair<String, String>>) -> Unit) {
        db.collection("songs")
            .whereArrayContains("genres", genre)
            .get()
            .addOnSuccessListener { result ->
                val songs = result.documents.mapNotNull { doc ->
                    val title = doc.getString("title")
                    val artist = doc.getString("artist") ?: ""
                    val id = doc.id
                    if (title != null) "$title - $artist" to id else null
                }
                Log.d("Firestore", "Found ${songs.size} songs for genre: $genre")
                callback(songs)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to fetch songs for genre $genre: ${e.message}")
                callback(emptyList())
            }
    }

    suspend fun addSampleSongs() {
        val lyrics = listOf(
            "Every breath you take" to ("Every" to "G"),
            "And every move you make" to ("make" to "Em"),
            "Every bond you break" to ("break" to "C"),
            "Every step you take" to ("take" to "D"),
            "I'll be watching you" to ("watching" to "G"),

            "Every single day" to ("Every" to "G"),
            "And every word you say" to ("say" to "Em"),
            "Every game you play" to ("play" to "C"),
            "Every night you stay" to ("stay" to "D"),
            "I'll be watching you" to ("watching" to "G"),

            "Oh can't you see" to ("see" to "C"),
            "You belong to me" to ("me" to "D"),
            "How my poor heart aches" to ("aches" to "Bm"),
            "With every step you take" to ("take" to "Em"),

            "Every move you make" to ("make" to "G"),
            "And every vow you break" to ("break" to "Em"),
            "Every smile you fake" to ("fake" to "C"),
            "Every claim you stake" to ("stake" to "D"),
            "I'll be watching you" to ("watching" to "G"),

            "Since you've gone I've been lost without a trace" to ("trace" to "Em"),
            "I dream at night, I can only see your face" to ("face" to "C"),
            "I look around but it's you I can't replace" to ("replace" to "D"),
            "I feel so cold and I long for your embrace" to ("embrace" to "Bm"),
            "I keep crying baby, baby, please..." to ("please" to "Em")
        )

        val song = Song(
            title = "Every Breath You Take",
            artist = "The Police",
            key = "G",
            bpm = 117,
            genres = listOf("Rock", "Soft Rock", "Pop"),
            createdAt = System.currentTimeMillis().toString(),
            creatorId = "admin",
            lyrics = lyrics.mapIndexed { index, (lineText, chordPair) ->
                val (targetWord, chord) = chordPair
                SongLine(
                    lineNumber = index + 1,
                    text = lineText,
                    chordLine = generateChordLine(lineText, targetWord, chord)
                )
            }
        )

        val documentId = "Every_Breath_You_Take_Chords"
        FirebaseFirestore.getInstance()
            .collection("songs")
            .document(documentId)
            .set(song)  // overwrite το document
    }


}