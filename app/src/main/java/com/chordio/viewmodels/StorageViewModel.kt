package com.chordio.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import com.chordio.repository.StorageRepository

class StorageViewModel(
    private val storageRepository: StorageRepository = StorageRepository()
) : ViewModel() {

    private val _profileImageUrl = mutableStateOf<String?>(null)
    val profileImageUrl: State<String?> = _profileImageUrl

    fun loadProfileImage(userId: String) {
        if (_profileImageUrl.value == null) {
            viewModelScope.launch {
                _profileImageUrl.value = storageRepository.getProfileImageUrl(userId)
            }
        }
    }
}
