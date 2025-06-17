package com.chordio.repository

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.auth.FirebaseUser
import com.chordio.models.User

object AuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    @SuppressLint("StaticFieldLeak")
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    val isUserLoggedInState = mutableStateOf(isUserLoggedIn())
    private val fullNameState = mutableStateOf(getFullName())

    fun signInUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    isUserLoggedInState.value = true
                    onResult(true, null)
                } else {
                    onResult(false, handleFirebaseException(task.exception))
                }
            }
    }

    fun signUpUser(email: String, password: String, fullName: String, onResult: (Boolean, String?) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user == null) {
                        onResult(false, "Unexpected error: User is null.")
                        return@addOnCompleteListener
                    }
                    updateUserProfile(fullName) { success, errorMessage ->
                        onResult(success, errorMessage)
                    }
                } else {
                    onResult(false, handleFirebaseException(task.exception))
                }
            }
    }

    fun deleteUserAccount(onResult: (Boolean, String?) -> Unit) {
        val user = firebaseAuth.currentUser
        if (user == null) {
            onResult(false, "No user is currently logged in.")
            return
        }

        // Get the user's UID to delete their Firestore document
        val uid = user.uid

        // First, delete the Firestore document
        firestore.collection("users").document(uid).delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Then, delete the user from Firebase Authentication
                    user.delete()
                        .addOnCompleteListener { deleteTask ->
                            if (deleteTask.isSuccessful) {
                                onResult(true, null)
                            } else {
                                onResult(false, handleFirebaseException(deleteTask.exception))
                            }
                        }
                } else {
                    logError("Failed to delete Firestore document for user", task.exception)
                    onResult(false, "Failed to delete user data from Firestore.")
                }
            }
    }

    fun saveUserToFirestore(uid: String, fullName: String, email: String, role: String, onResult: (Boolean) -> Unit) {
        val user = mapOf(
            "fullName" to fullName,
            "email" to email,
            "role" to role
        )
        firestore.collection("users").document(uid).set(user)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { exception ->
                logError("Failed to save user to Firestore", exception)
                onResult(false)
            }
    }

    private fun updateUserProfile(fullName: String, onResult: (Boolean, String?) -> Unit) {
        val user = firebaseAuth.currentUser
        if (user == null) {
            onResult(false, "User not logged in")
            return
        }
        val profileUpdates = userProfileChangeRequest {
            displayName = fullName
        }
        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, handleFirebaseException(task.exception))
                }
            }
    }

    fun updateUsernameInFirestore(userId: String, newUsername: String, onResult: (Boolean) -> Unit) {
        firestore.collection("users").document(userId)
            .update("username", newUsername)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { exception ->
                logError("Failed to update username in Firestore", exception)
                onResult(false)
            }
    }

    fun getUserRoleFromFirestore(uid: String, onResult: (String?) -> Unit) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")
                    onResult(role) // Επιτυχία, επιστρέφει τον ρόλο
                } else {
                    onResult(null) // Το έγγραφο δεν βρέθηκε, επιστρέφουμε null
                }
            }
            .addOnFailureListener { exception ->
                logError("Failed to fetch user role from Firestore", exception)
                onResult(null) // Σε περίπτωση σφάλματος, επιστρέφουμε null
            }
    }

    fun getUsernameFromFirestore(userId: String, onResult: (String?) -> Unit) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username")
                    onResult(username)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener { exception ->
                logError("Failed to fetch username from Firestore", exception)
                onResult(null)
            }
    }

    private fun handleFirebaseException(exception: Exception?): String {
        return exception?.localizedMessage ?: "An unexpected error occurred"
    }

    private fun logError(message: String, exception: Exception?) {
        println("Error: $message, Exception: ${exception?.localizedMessage}")
    }

    fun getFullName(): String? {
        return firebaseAuth.currentUser?.displayName
    }

    fun getUserEmail(): String? {
        return firebaseAuth.currentUser?.email
    }

    fun logoutUser() {
        firebaseAuth.signOut()
        isUserLoggedInState.value = false
        fullNameState.value = null
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    fun getUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    private fun isUserLoggedIn(): Boolean {
        val loggedIn = firebaseAuth.currentUser != null
        Log.d("AuthRepository", "isUserLoggedIn: $loggedIn")
        return firebaseAuth.currentUser != null
    }

    fun getUserFromFirestore(uid: String, onResult: (User?) -> Unit) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    onResult(user)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener { exception ->
                logError("Failed to fetch user from Firestore", exception)
                onResult(null)
            }
    }

    fun resetPassword(email: String, onResult: (Boolean, String?) -> Unit) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null) // Email στάλθηκε με επιτυχία
                } else {
                    onResult(false, handleFirebaseException(task.exception)) // Σφάλμα
                }
            }
    }

}
