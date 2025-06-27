package com.chordio.viewmodels.seconds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.chordio.models.song.Song
import com.chordio.repository.firestore.SongRepository
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
                println("🚀 Trying to upload song with ID: $songId")
                songRepo.addSongData(songId, song)
                println("✅ Upload succeeded for song ID: $songId")
                _uploadSuccess.value = true
            } catch (e: Exception) {
                println("❌ Upload failed: ${e.message}")
                _errorMessage.value = "Σφάλμα κατά το ανέβασμα: ${e.localizedMessage}"
            }
        }
    }


    fun resetStatus() {
        _uploadSuccess.value = false
        _errorMessage.value = null
    }
}
