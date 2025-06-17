package com.chordio.viewmodels.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import com.chordio.models.User
import com.chordio.repository.AuthRepository

class AuthViewModel : ViewModel() {

    private val _isUserLoggedIn = mutableStateOf(AuthRepository.getUserId() != null)
    val isUserLoggedIn: State<Boolean> = _isUserLoggedIn

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _username = mutableStateOf<String?>(null)
    val username: State<String?> = _username

    private val _role = mutableStateOf<String?>(null)
    val role: State<String?> = _role

    private val _fullName = mutableStateOf<String?>(null)
    val fullName: State<String?> = _fullName

    private val _email = mutableStateOf<String?>(null)
    val email: State<String?> = _email

    private val _deleteMessage = mutableStateOf<String?>(null)
    val deleteMessage: State<String?> = _deleteMessage

    fun loadProfileData() {
        val uid = AuthRepository.getUserId()
        if (uid != null) {
            _fullName.value = AuthRepository.getFullName()
            _email.value = AuthRepository.getUserEmail()
            AuthRepository.getUserRoleFromFirestore(uid) { userRole ->
                _role.value = userRole
            }
            AuthRepository.getUsernameFromFirestore(uid) { userUsername ->
                _username.value = userUsername
            }


        }
    }

    fun updateUsername(newUsername: String, onResult: (Boolean) -> Unit) {
        val uid = AuthRepository.getUserId()
        if (uid != null) {
            AuthRepository.updateUsernameInFirestore(uid, newUsername) { success ->
                if (success) {
                    _username.value = newUsername
                }
                onResult(success)
            }
        }
    }

    fun deleteUserAccount(onResult: (Boolean) -> Unit) {
        AuthRepository.deleteUserAccount { success, message ->
            if (success) {
                _deleteMessage.value = null
                onResult(true)
            } else {
                _deleteMessage.value = message
                onResult(false)
            }
        }
    }

    fun logout(onResult: () -> Unit) {
        AuthRepository.logoutUser()
        _isUserLoggedIn.value = false
        _fullName.value = null
        _email.value = null
        onResult()
    }

    fun loginUser(email: String, password: String, onSuccess: () -> Unit) {
        _isLoading.value = true
        AuthRepository.signInUser(email, password) { success, errorMessage ->
            if (success) {
                refreshSessionState()
                onSuccess()
            } else {
                _error.value = errorMessage ?: "Login failed."
            }
            _isLoading.value = false
        }
    }

    fun signUpUser(fullName: String, email: String, password: String, onResult: (Boolean, String?) -> Unit ) {
        _isLoading.value = true

        AuthRepository.signUpUser(email, password, fullName) { success, errorMessage ->
            if (success) {
                val uid = AuthRepository.getUserId()
                if (uid != null) {
                    AuthRepository.saveUserToFirestore(uid, fullName, email, "user") { firestoreSuccess ->
                        _isLoading.value = false
                        if (firestoreSuccess) {
                            onResult(true, null)
                        } else {
                            onResult(false, "Failed to save user info.")
                        }
                    }
                } else {
                    _isLoading.value = false
                    onResult(false, "User ID is null.")
                }
            } else {
                _isLoading.value = false
                onResult(false, errorMessage ?: "Sign up failed.")
            }
        }
    }

    fun validateSession(onResult: (Boolean) -> Unit) {
        val user = AuthRepository.getCurrentUser()
        if (user == null) {
            _isUserLoggedIn.value = false
            onResult(false)
            return
        }

        user.reload().addOnCompleteListener { task ->
            val isValid = task.isSuccessful && AuthRepository.getCurrentUser() != null
            if (!isValid) {
                logout {}
            }
            _isUserLoggedIn.value = isValid
            onResult(isValid)
        }
    }

    fun getUserId(): String? {
        return AuthRepository.getUserId()
    }

    fun resetPassword(email: String, onResult: (Boolean, String?) -> Unit) {
        AuthRepository.resetPassword(email) { success, errorMessage ->
            onResult(success, errorMessage)
        }
    }

    fun loadUserData(onUserLoaded: (User?) -> Unit) {
        val uid = AuthRepository.getUserId()
        if (uid != null) {
            _isLoading.value = true
            AuthRepository.getUserFromFirestore(uid) { user ->
                onUserLoaded(user)
                _isLoading.value = false
            }
        } else {
            _error.value = "User not logged in."
        }
    }

    private fun refreshSessionState() {
        _isUserLoggedIn.value = AuthRepository.getUserId() != null
        _fullName.value = AuthRepository.getFullName()
        _email.value = AuthRepository.getUserEmail()
    }

}
