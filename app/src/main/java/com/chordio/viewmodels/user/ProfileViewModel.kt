package com.chordio.viewmodels.user

import android.net.Uri

/*
class ProfileViewModel(private val userId: String) : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val username = AuthRepository.fullNameState.value ?: "Unknown"
            val imageUrl = getProfileImageUrl(userId)

            _profileState.value = ProfileState(username, imageUrl, null)
        }
    }

    fun updateUsername(newUsername: String) {
        _profileState.value = _profileState.value.copy(username = newUsername)
    }

    fun updateSelectedImage(uri: Uri?) {
        _profileState.value = _profileState.value.copy(selectedImage = uri)
    }

    fun saveChanges(snackbarHostState: SnackbarHostState) {
        viewModelScope.launch {
            try {
                val state = _profileState.value
                var imageUrl: String? = state.profileImageUrl

                if (state.selectedImage != null) {
                    imageUrl = uploadImageToFirebase(state.selectedImage, userId)
                }

                val updates = mutableMapOf<String, Any>()
                if (state.username != AuthRepository.fullNameState.value) updates["username"] = state.username
                if (imageUrl != null) updates["profileImageUrl"] = imageUrl

                if (updates.isNotEmpty()) {
                    FirebaseFirestore.getInstance().collection("users").document(userId).update(updates).await()
                }

                snackbarHostState.showSnackbar("Profile updated successfully!")

            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error updating profile: ${e.message}")
            }
        }
    }
}
*/

data class ProfileState(
    val username: String = "Unknown",
    val profileImageUrl: String? = null,
    val selectedImage: Uri? = null
)
