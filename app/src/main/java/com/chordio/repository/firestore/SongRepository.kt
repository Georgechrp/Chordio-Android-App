package com.chordio.repository.firestore

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.chordio.models.song.ChordPosition
import com.chordio.models.song.FirestoreSongDTO
import com.chordio.models.song.Song
import com.chordio.models.song.SongCardItem
import com.chordio.models.song.SongLine
import kotlinx.coroutines.tasks.await

class SongRepository(private val db: FirebaseFirestore) {

    suspend fun getSongDataAsync(songId: String): Song? {
        if (songId.isBlank()) {
            Log.w("Firestore", "⚠️ getSongDataAsync called with empty song ID — skipping Firestore call.")
            return null
        }

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
            val bpm = document.getLong("bpm")?.toInt() ?: 0
            val genres = document.get("genres") as? List<String> ?: emptyList()
            val createdAt = document.getString("createdAt") ?: ""
            val creatorId = document.getString("creatorId") ?: ""

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

            Log.d("Firestore", "Song loaded successfully: $title")
            return Song(
                id = document.id,
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
            Log.e("Firestore", "Firestore Error: ${e.message}")
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
            Log.d("Firestore", " Song added successfully: $songId")
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
                println(" Fetched ${result.size()} documents for artist: $artistName")
                val songs = result.mapNotNull {
                    try {
                        val dto = it.toObject(FirestoreSongDTO::class.java)
                        val viewsCount = it.getLong("viewsCount")?.toInt()
                        dto?.toSong()?.copy(id = it.id, viewsCount = viewsCount)
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

    suspend fun getSongById(songId: String): Song? {
        return try {
            val doc = db.collection("songs").document(songId).get().await()
            doc.toObject(FirestoreSongDTO::class.java)?.toSong()?.copy(id = doc.id)
        } catch (e: Exception) {
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
                Log.d("Firestore", " Genres fetched: $genres")
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

    suspend fun uploadSong(song: Song): Boolean {
        return try {
            val songMap = hashMapOf(
                "title" to song.title,
                "artist" to song.artist,
                "key" to song.key,
                "bpm" to song.bpm,
                "genres" to song.genres,
                "createdAt" to song.createdAt,
                "creatorId" to song.creatorId,
                "lyrics" to song.lyrics.map { line ->
                    hashMapOf(
                        "lineNumber" to line.lineNumber,
                        "text" to line.text,
                        "chords" to line.chords.map {
                            hashMapOf(
                                "chord" to it.chord,
                                "position" to it.position
                            )
                        },
                        // Αν δεν υπάρχει chordLine, το δημιουργούμε εδώ
                        "chordLine" to (line.chordLine ?: generateChordLineFromPositions(line.text, line.chords))
                    )
                }
            )

            db.collection("songs")
                .add(songMap)
                .await()

            true
        } catch (e: Exception) {
            Log.e("Firestore", "❌ Error uploading song: ${e.message}")
            false
        }
    }


    private fun generateChordLineFromPositions(text: String, chords: List<ChordPosition>): String {
        val result = CharArray(text.length.coerceAtLeast(1)) { ' ' }

        for (chord in chords.sortedBy { it.position }) {
            val pos = chord.position.coerceIn(0, result.lastIndex)
            val chordText = chord.chord
            for (i in chordText.indices) {
                if (pos + i < result.size) {
                    result[pos + i] = chordText[i]
                }
            }
        }

        return String(result)
    }

    suspend fun getSongViewsCount(songId: String): Int? {
        return try {
            val doc = db.collection("songs").document(songId).get().await()
            doc.getLong("viewsCount")?.toInt()
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching views count: ${e.message}")
            null
        }
    }

    suspend fun getSongsByGenres(genres: List<String>): List<Song> {
        val firestore = FirebaseFirestore.getInstance()
        val songsCollection = firestore.collection("songs")
        val results = mutableListOf<Song>()

        for (genre in genres) {
            val snapshot = songsCollection.whereArrayContains("genres", genre).get().await()
            results.addAll(snapshot.documents.mapNotNull { it.toObject(Song::class.java)?.copy(id = it.id) })
        }

        return results.distinctBy { it.id }  // remove duplicates
    }


    suspend fun getAllSongsOfGenre(genre: String): List<Song> {
        return try {
            db.collection("songs")
                .whereArrayContains("genres", genre)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    val id = doc.id
                    val song = getSongDataAsync(id)
                    song
                }
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching songs of genre '$genre': ${e.message}")
            emptyList()
        }
    }

}