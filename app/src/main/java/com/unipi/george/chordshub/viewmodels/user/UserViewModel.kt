package com.unipi.george.chordshub.viewmodels.user

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.unipi.george.chordshub.models.User
import com.unipi.george.chordshub.models.song.Song
import com.unipi.george.chordshub.repository.StorageRepository
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
            if (document.exists()) {
                val recentIds = document.get("recentSongs") as? List<String> ?: emptyList()
                val validIds = recentIds.filter { it.isNotBlank() }.take(10)

                if (validIds.isEmpty()) {
                    _recentSongs.value = emptyList()
                    return@addOnSuccessListener
                }

                db.collection("songs")
                    .whereIn(FieldPath.documentId(), validIds)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val songList = snapshot.documents.mapNotNull { it.toObject(Song::class.java)?.apply { id = it.id } }
                        val orderedList = validIds.mapNotNull { id -> songList.find { it.id == id } }
                        _recentSongs.value = orderedList
                    }
                    .addOnFailureListener {
                        Log.e("Firestore", "Error fetching songs: ${it.message}")
                    }

            } else {
                _recentSongs.value = emptyList()
            }
        }.addOnFailureListener { e ->
            Log.e("Firestore", "⚠Failed to fetch user data: ${e.message}")
        }
    }



    fun addRecentSong(userId: String, songId: String) {
        if (songId.isBlank()) return  // Προστασία από άκυρο ID

        val userRef = db.collection("users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            val recentSongs = document.get("recentSongs") as? List<String> ?: emptyList()
            val updatedSongs = (listOf(songId) + recentSongs.filter { it != songId }).take(10)

            userRef.update("recentSongs", updatedSongs)
        }.addOnFailureListener {
            userRef.set(mapOf("recentSongs" to listOf(songId)), SetOptions.merge())
        }
    }



}
