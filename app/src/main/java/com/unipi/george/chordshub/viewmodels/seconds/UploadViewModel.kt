package com.unipi.george.chordshub.viewmodels.seconds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.unipi.george.chordshub.models.song.Song
import com.unipi.george.chordshub.repository.firestore.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UploadViewModel : ViewModel() {
    private val songRepo = SongRepository(FirebaseFirestore.getInstance())

    private val _uploadSuccess = MutableStateFlow(false)
    val uploadSuccess: StateFlow<Boolean> = _uploadSuccess

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun uploadSong(songId: String, song: Song) {
        viewModelScope.launch {
            try {
                songRepo.addSongData(songId, song)
                _uploadSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Σφάλμα κατά το ανέβασμα: ${e.localizedMessage}"
            }
        }
    }

    fun resetStatus() {
        _uploadSuccess.value = false
        _errorMessage.value = null
    }
}
