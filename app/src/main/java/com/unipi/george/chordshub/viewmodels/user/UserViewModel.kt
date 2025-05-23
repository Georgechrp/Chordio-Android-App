package com.unipi.george.chordshub.viewmodels.user

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.unipi.george.chordshub.models.User
import com.unipi.george.chordshub.repository.StorageRepository
import kotlinx.coroutines.launch


class UserViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _userState = mutableStateOf<User?>(null)
    val userState: State<User?> = _userState

    val userId: String? get() = _userState.value?.uid

    private val _recentSongs = mutableStateOf<List<String>>(emptyList())
    val recentSongs: State<List<String>> = _recentSongs

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
                val recentSongs = document.get("recentSongs") as? List<String> ?: emptyList()
                _recentSongs.value = recentSongs
            } else {
                _recentSongs.value = emptyList() // Κενή λίστα αν δεν υπάρχουν τραγούδια
            }
        }.addOnFailureListener { e ->
            println("Σφάλμα κατά την ανάκτηση των πρόσφατων τραγουδιών: ${e.message}")
        }
    }

    fun addRecentSong(userId: String, songTitle: String) {
        val userRef = db.collection("users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val recentSongs = document.get("recentSongs") as? List<String> ?: emptyList()

                // Αν το τραγούδι υπάρχει ήδη, το αφαιρούμε πριν το προσθέσουμε ξανά (ώστε να πάει στην αρχή)
                val updatedSongs = (listOf(songTitle) + recentSongs.filter { it != songTitle })
                    .take(10) // Διατηρούμε μόνο τις τελευταίες 8 καταχωρήσεις

                userRef.update("recentSongs", updatedSongs)
                    .addOnSuccessListener {
                        println(" Το τραγούδι '$songTitle' προστέθηκε στα πρόσφατα τραγούδια!")
                    }
                    .addOnFailureListener { e ->
                        println(" Σφάλμα κατά την προσθήκη τραγουδιού: ${e.message}")
                    }
            } else {
                // Αν δεν υπάρχει το array, δημιουργείται νέο
                userRef.set(mapOf("recentSongs" to listOf(songTitle)), SetOptions.merge())
                    .addOnSuccessListener {
                        println(" Η λίστα recentSongs δημιουργήθηκε με επιτυχία!")
                    }
                    .addOnFailureListener { e ->
                        println(" Σφάλμα κατά τη δημιουργία recentSongs: ${e.message}")
                    }
            }
        }.addOnFailureListener { e ->
            println(" Σφάλμα κατά την ανάκτηση δεδομένων χρήστη: ${e.message}")
        }
    }


}
