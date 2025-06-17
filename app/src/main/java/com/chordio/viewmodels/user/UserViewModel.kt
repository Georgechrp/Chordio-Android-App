package com.chordio.viewmodels.user

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.chordio.models.User
import com.chordio.models.song.RecentSongEntry
import com.chordio.models.song.Song
import com.chordio.repository.StorageRepository
import kotlinx.coroutines.launch


class UserViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _userState = mutableStateOf<User?>(null)
    val userState: State<User?> = _userState

    val userId: String? get() = _userState.value?.uid

    private val _recentSongs = mutableStateOf<List<Song>>(emptyList())
    val recentSongs: State<List<Song>> = _recentSongs


    fun setUser(user: User?) {
        _userState.value = user
    }

    private val _profileImageUrl = mutableStateOf<String?>(null)
    val profileImageUrl: State<String?> = _profileImageUrl

    private var recentSongMap: Map<String, Long> = emptyMap()  // Key = song.id
    fun getRecentSongMap(): Map<String, Long> = recentSongMap

    fun updateProfileImage(userId: String, imageUri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val url = StorageRepository().uploadImageToFirebaseStorage(imageUri, userId)
            if (url != null) {
                updateUserProfileImageInFirestore(userId, url)
                _profileImageUrl.value = url
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    private fun updateUserProfileImageInFirestore(userId: String, imageUrl: String) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
        userRef.update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                Log.d("Firestore", "Profile image updated successfully!")
            }
            .addOnFailureListener {
                Log.e("Firestore", "Failed to update profile image: ${it.message}")
            }
    }

    fun fetchRecentSongs(userId: String) {
        val userRef = db.collection("users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            val recentData = document.get("recentSongs") as? List<Map<String, Any>> ?: emptyList()
            val recentEntries = recentData.mapNotNull {
                val id = it["songId"] as? String
                val ts = (it["timestamp"] as? Long) ?: System.currentTimeMillis()
                if (id != null) RecentSongEntry(id, ts) else null
            }.sortedByDescending { it.timestamp }

            val ids = recentEntries.map { it.songId }

            if (ids.isEmpty()) {
                _recentSongs.value = emptyList()
                return@addOnSuccessListener
            }

            db.collection("songs")
                .whereIn(FieldPath.documentId(), ids)
                .get()
                .addOnSuccessListener { snapshot ->
                    val songs = snapshot.documents.mapNotNull {
                        it.toObject(Song::class.java)?.apply { id = it.id }
                    }

                    val orderedSongs = recentEntries.mapNotNull { entry ->
                        songs.find { it.id == entry.songId }?.let { it to entry.timestamp }
                    }

                    _recentSongs.value = orderedSongs.map { it.first }
                    recentSongMap = orderedSongs.associate { it.first.id to it.second }

                }
        }
    }





    fun addRecentSong(userId: String, songId: String) {
        val userRef = db.collection("users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            val recentData = document.get("recentSongs") as? List<Map<String, Any>> ?: emptyList()

            val existing = recentData.mapNotNull {
                val id = it["songId"] as? String
                val ts = (it["timestamp"] as? Long) ?: System.currentTimeMillis()
                if (id != null) RecentSongEntry(id, ts) else null
            }

            val updated = listOf(RecentSongEntry(songId)) +
                    existing.filter { it.songId != songId }
                        .sortedByDescending { it.timestamp }
                        .take(30)

            val toStore = updated.map { mapOf("songId" to it.songId, "timestamp" to it.timestamp) }

            userRef.update("recentSongs", toStore)
                .addOnFailureListener {
                    userRef.set(mapOf("recentSongs" to toStore), SetOptions.merge())
                }
        }
    }

    fun addRecentSongWithDate(userId: String, songId: String, daysAgo: Long) {
        val userRef = db.collection("users").document(userId)

        val timestamp = System.currentTimeMillis() - daysAgo * 24 * 60 * 60 * 1000  

        userRef.get().addOnSuccessListener { document ->
            val recentData = document.get("recentSongs") as? List<Map<String, Any>> ?: emptyList()

            val existing = recentData.mapNotNull {
                val id = it["songId"] as? String
                val ts = (it["timestamp"] as? Long) ?: System.currentTimeMillis()
                if (id != null) RecentSongEntry(id, ts) else null
            }

            val updated = listOf(RecentSongEntry(songId, timestamp)) +
                    existing.filter { it.songId != songId }
                        .sortedByDescending { it.timestamp }
                        .take(30)

            val toStore = updated.map { mapOf("songId" to it.songId, "timestamp" to it.timestamp) }

            userRef.update("recentSongs", toStore)
                .addOnSuccessListener {
                    Log.d("Firestore", "✅ Προστέθηκε τραγούδι με custom ημερομηνία")
                }
                .addOnFailureListener {
                    userRef.set(mapOf("recentSongs" to toStore), SetOptions.merge())
                }
        }
    }



}
