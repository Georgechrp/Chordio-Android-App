package com.chordio.repository.firestore

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.chordio.models.playlist.TempPlaylist

class TempPlaylistRepository(private val db: FirebaseFirestore) {

    fun createTempPlaylist(userId: String, firstSongId: String, callback: (Boolean) -> Unit) {
        val playlistData = mapOf(
            "songs" to listOf(firstSongId),
            "currentSongId" to firstSongId,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users").document(userId)
            .update("temp_playlist", playlistData)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }


    fun addSongToTempPlaylist(userId: String, songId: String) {
        db.collection("users").document(userId)
            .update("temp_playlist.songs", FieldValue.arrayUnion(songId))
    }


    fun setCurrentSongInTempPlaylist(userId: String, songId: String) {
        db.collection("users").document(userId)
            .update("temp_playlist.currentSongId", songId)
    }


    fun getTempPlaylist(userId: String, callback: (TempPlaylist?) -> Unit) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                val map = doc.get("temp_playlist") as? Map<*, *>
                if (map != null) {
                    val songs = map["songs"] as? List<String> ?: emptyList()
                    val currentSongId = map["currentSongId"] as? String ?: ""
                    callback(TempPlaylist(userId = userId, songs = songs, currentSongId = currentSongId))
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }


    fun deleteTempPlaylist(userId: String, callback: (Boolean) -> Unit) {
        db.collection("users").document(userId)
            .update("temp_playlist", FieldValue.delete())
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
}