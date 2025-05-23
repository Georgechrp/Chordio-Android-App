package com.unipi.george.chordshub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.unipi.george.chordshub.repository.firestore.SongRepository
import com.unipi.george.chordshub.viewmodels.seconds.SongViewModel

class SongViewModelFactory(
    private val songRepository: SongRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SongViewModel::class.java)) {
            return SongViewModel(songRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
