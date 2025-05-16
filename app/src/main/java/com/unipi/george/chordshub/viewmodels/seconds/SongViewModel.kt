package com.unipi.george.chordshub.viewmodels.seconds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unipi.george.chordshub.repository.firestore.SongRepository
import kotlinx.coroutines.launch

class SongViewModel(private val songRepository: SongRepository) : ViewModel() {

    fun registerSongView(songId: String) {
        viewModelScope.launch {
            songRepository.incrementSongViewCount(songId)
        }
    }
}
