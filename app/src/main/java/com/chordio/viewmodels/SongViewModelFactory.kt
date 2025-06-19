package com.chordio.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chordio.repository.UserStatsRepository
import com.chordio.repository.firestore.SongRepository
import com.chordio.viewmodels.seconds.SongViewModel
import com.google.firebase.firestore.FirebaseFirestore

class SongViewModelFactory(
    private val songRepository: SongRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SongViewModel::class.java)) {
            val userStatsRepo = UserStatsRepository(FirebaseFirestore.getInstance())
            return SongViewModel(songRepository, userStatsRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
