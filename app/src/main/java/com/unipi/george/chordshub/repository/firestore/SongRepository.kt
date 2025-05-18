package com.unipi.george.chordshub.repository.firestore

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.unipi.george.chordshub.models.song.ChordPosition
import com.unipi.george.chordshub.models.song.FirestoreSongDTO
import com.unipi.george.chordshub.models.song.Song
import com.unipi.george.chordshub.models.song.SongCardItem
import com.unipi.george.chordshub.models.song.SongLine
import kotlinx.coroutines.tasks.await

class SongRepository(private val db: FirebaseFirestore) {

    suspend fun getSongDataAsync(songId: String): Song? {
        Log.d("Firestore", "Fetching song data for ID: $songId")

        return try {
            val document = db.collection("songs").document(songId).get().await()
            if (!document.exists()) {
                Log.e("Firestore", "❌ No song found with ID: $songId")
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
            Log.e("Firestore", "❌ Firestore Error: ${e.message}")
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
            Log.e("Firestore", "❌ Error adding song", e)
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
                println("❌ Firestore error: ${exception.message}")
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
                Log.e("Firestore", "❌ Error fetching random songs: ${exception.message}")
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
                        println("❌ Error parsing song: ${e.localizedMessage}")
                        null
                    }
                }
                callback(songs)
            }
            .addOnFailureListener { e ->
                println("❌ Firebase query failed: $e")
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
                println("❌ Failed to fetch artists: $e")
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
            Log.e("Firestore", "❌ Error fetching song by title: ${e.message}")
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
            Log.d("Firestore", "✅ View count incremented for $songId")
        } catch (e: Exception) {
            Log.e("Firestore", "❌ Failed to increment view count: ${e.message}")
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
                Log.e("Firestore", "❌ Error fetching top songs: ${exception.message}")
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
                Log.e("Firestore", "❌ Failed to fetch genres: ${e.message}")
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
                Log.d("Firestore", "✅ Found ${songs.size} songs for genre: $genre")
                callback(songs)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "❌ Failed to fetch songs for genre $genre: ${e.message}")
                callback(emptyList())
            }
    }

    suspend fun addSampleSongs() {
        val sampleSongs = listOf(

            Song(
                title = "Let Her Go",
                artist = "Passenger",
                key = "C",
                bpm = 75,
                genres = listOf("Folk", "Acoustic"),
                createdAt = System.currentTimeMillis().toString(),
                creatorId = "admin",
                lyrics = listOf(
                    SongLine(1, "Well you only need the light when it's burning low", listOf()),
                    SongLine(2, "Only miss the sun when it starts to snow", listOf()),
                    SongLine(3, "Only know you love her when you let her go", listOf())
                )
            )
           ,
            Song(
                title = "Raging Fire",
                artist = "Iron Claw",
                key = "E",
                bpm = 190,
                genres = listOf("Thrash Metal", "Metal"),
                createdAt = System.currentTimeMillis().toString(),
                creatorId = "admin",
                lyrics = listOf(
                    SongLine(1, "Screaming thunder cracks the sky,", chords = listOf(ChordPosition("E5", 0))),
                    SongLine(2, "Blazing steel, we ride or die,", chords = listOf(ChordPosition("C5", 1))),
                    SongLine(3, "No remorse, no turning back,", chords = listOf(ChordPosition("D5", 2))),
                    SongLine(4, "We attack, we attack!", chords = listOf(ChordPosition("E5", 3))),
                    SongLine(5, "🔥 Raging fire inside our veins,", chords = emptyList()),
                    SongLine(6, "Burning madness breaks the chains!", chords = emptyList())
                )
            )
        )

        sampleSongs.forEachIndexed { index, song ->
            val id = db.collection("songs").document().id
            addSongData(id, song)
        }
    }


}