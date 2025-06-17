package com.chordio.repository.firestore

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class PlaylistRepository(private val db: FirebaseFirestore) {

    fun createPlaylist(userId: String, playlistName: String, callback: (Boolean) -> Unit) {
        val userDocRef = db.collection("users").document(userId)

        val newPlaylist = hashMapOf(
            "name" to playlistName,
            "songs" to emptyList<String>()
        )

        userDocRef.update("playlists", FieldValue.arrayUnion(newPlaylist))
            .addOnSuccessListener {
                Log.d("Firestore", "Playlist '$playlistName' created successfully.")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error creating playlist: ${e.message}")
                callback(false)
            }
    }

    fun getUserPlaylistsWithSongs(userId: String, callback: (Map<String, List<String>>) -> Unit) {
        val userDocRef = db.collection("users").document(userId)

        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val playlists = document.get("playlists") as? List<Map<String, Any>>
                    val playlistMap = mutableMapOf<String, List<String>>()

                    playlists?.forEach { playlist ->
                        val playlistName = playlist["name"] as? String ?: "Άγνωστη Playlist"
                        val songs = playlist["songs"] as? List<String> ?: emptyList()
                        playlistMap[playlistName] = songs
                    }

                    callback(playlistMap)
                } else {
                    callback(emptyMap())
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching playlists with songs: ${e.message}")
                callback(emptyMap())
            }
    }

    fun addSongToPlaylist(userId: String, playlistName: String, songTitle: String, callback: (Boolean) -> Unit) {
        val userDocRef = db.collection("users").document(userId)

        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val playlists = document.get("playlists") as? MutableList<Map<String, Any>> ?: mutableListOf()

                    // Βρίσκουμε την αντίστοιχη playlist
                    val updatedPlaylists = playlists.map { playlist ->
                        if (playlist["name"] == playlistName) {
                            val updatedSongs = (playlist["songs"] as? MutableList<String>) ?: mutableListOf()
                            if (!updatedSongs.contains(songTitle)) {
                                updatedSongs.add(songTitle)
                            }
                            playlist.toMutableMap().apply { put("songs", updatedSongs) }
                        } else {
                            playlist
                        }
                    }

                    userDocRef.update("playlists", updatedPlaylists)
                        .addOnSuccessListener { callback(true) }
                        .addOnFailureListener { callback(false) }
                } else {
                    callback(false)
                }
            }
            .addOnFailureListener { callback(false) }
    }

    fun removeSongFromPlaylist(userId: String, playlistName: String, songTitle: String, callback: (Boolean) -> Unit) {
        val userDocRef = db.collection("users").document(userId)

        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val playlists = document.get("playlists") as? MutableList<Map<String, Any>> ?: mutableListOf()

                    // Βρίσκουμε την playlist και αφαιρούμε το τραγούδι
                    val updatedPlaylists = playlists.map { playlist ->
                        if (playlist["name"] == playlistName) {
                            val updatedSongs = (playlist["songs"] as? MutableList<String>) ?: mutableListOf()
                            updatedSongs.remove(songTitle)
                            playlist.toMutableMap().apply { put("songs", updatedSongs) }
                        } else {
                            playlist
                        }
                    }

                    userDocRef.update("playlists", updatedPlaylists)
                        .addOnSuccessListener { callback(true) }
                        .addOnFailureListener { callback(false) }
                } else {
                    callback(false)
                }
            }
            .addOnFailureListener { callback(false) }
    }

    fun deletePlaylist(userId: String, playlistName: String, callback: (Boolean) -> Unit) {
        val userDocRef = db.collection("users").document(userId)

        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val playlists = document.get("playlists") as? MutableList<Map<String, Any>> ?: mutableListOf()

                    // Διαγράφουμε την playlist από τη λίστα
                    val updatedPlaylists = playlists.filter { it["name"] != playlistName }

                    userDocRef.update("playlists", updatedPlaylists)
                        .addOnSuccessListener { callback(true) }
                        .addOnFailureListener { callback(false) }
                } else {
                    callback(false)
                }
            }
            .addOnFailureListener { callback(false) }
    }

    fun renamePlaylist(userId: String, oldName: String, newName: String, callback: (Boolean) -> Unit) {
        val userDocRef = db.collection("users").document(userId)

        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val playlists = document.get("playlists") as? MutableList<Map<String, Any>> ?: mutableListOf()
                    val updatedPlaylists = playlists.map {
                        if (it["name"] == oldName) {
                            it.toMutableMap().apply { put("name", newName) }
                        } else it
                    }
                    userDocRef.update("playlists", updatedPlaylists)
                        .addOnSuccessListener { callback(true) }
                        .addOnFailureListener { callback(false) }
                } else callback(false)
            }
            .addOnFailureListener { callback(false) }
    }

}