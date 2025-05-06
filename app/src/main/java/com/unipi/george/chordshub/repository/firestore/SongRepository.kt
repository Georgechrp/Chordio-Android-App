package com.unipi.george.chordshub.repository.firestore

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.unipi.george.chordshub.models.song.ChordPosition
import com.unipi.george.chordshub.models.song.FirestoreSongDTO
import com.unipi.george.chordshub.models.song.Song
import com.unipi.george.chordshub.models.song.SongLine
import kotlinx.coroutines.tasks.await

class SongRepository(private val db: FirebaseFirestore) {

    fun getSongTitles(callback: (List<Pair<String, String>>) -> Unit) {
        db.collection("songs")
            .get()
            .addOnSuccessListener { result ->
                val titlesAndIds = mutableListOf<Pair<String, String>>()
                for (document in result) {
                    val title = document.getString("title")
                    val id = document.id
                    if (title != null && id != null) {
                        titlesAndIds.add(Pair(title, id))
                    }
                }
                callback(titlesAndIds)
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
                callback(emptyList())
            }
    }

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
                    val id = doc.id
                    val genres = doc.get("genres") as? List<String>
                    println("Genres for song $title: $genres")
                    if (title != null) title to id else null
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

    fun getSongsByArtist(artistName: String, callback: (List<Song>) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("artists")
            .document(artistName)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val songIds = document.get("songs") as? List<String>
                    if (!songIds.isNullOrEmpty()) {
                        // Fetch each song by its ID
                        val songs = mutableListOf<Song>()
                        val tasks = songIds.map { songId ->
                            db.collection("songs").document(songId).get()
                        }

                        Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                            .addOnSuccessListener { snapshots ->
                                for (snapshot in snapshots) {
                                    snapshot.toObject(Song::class.java)?.let { songs.add(it) }
                                }
                                callback(songs)
                            }
                            .addOnFailureListener { e ->
                                println("Error fetching song documents: $e")
                                callback(emptyList())
                            }
                    } else {
                        callback(emptyList())
                    }
                } else {
                    callback(emptyList())
                }
            }
            .addOnFailureListener { e ->
                println("Error fetching artist document: $e")
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


}