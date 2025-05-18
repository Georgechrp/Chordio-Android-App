package com.unipi.george.chordshub.viewmodels.user

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.unipi.george.chordshub.models.User
import com.unipi.george.chordshub.repository.AuthRepository
import kotlinx.coroutines.launch


class UserViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()


    private val _userState = mutableStateOf<User?>(null)
    val userState: State<User?> = _userState

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error


    val userId: String? get() = _userState.value?.uid

    private val _recentSongs = mutableStateOf<List<String>>(emptyList())
    val recentSongs: State<List<String>> = _recentSongs



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

    init {
        loadUserData()
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
    private fun loadUserData() {
        val uid = AuthRepository.getUserId()
        if (uid != null) {
            _isLoading.value = true
            AuthRepository.getUserFromFirestore(uid) { user ->
                _userState.value = user
                _isLoading.value = false
            }
        } else {
            _error.value = "User not logged in."
        }
    }

    fun loginUser(email: String, password: String, onSuccess: () -> Unit) {
        _isLoading.value = true
        AuthRepository.signInUser(email, password) { success, errorMessage ->
            if (success) {
                loadUserData()  // Φόρτωση δεδομένων χρήστη μετά την επιτυχή σύνδεση
                onSuccess()
            } else {
                _error.value = errorMessage ?: "Login failed."
            }
            _isLoading.value = false
        }
    }

    fun logoutUser() {
        AuthRepository.logoutUser()
        _userState.value = null
    }
}
