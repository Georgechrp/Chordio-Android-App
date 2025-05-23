package com.unipi.george.chordshub.viewmodels.seconds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unipi.george.chordshub.models.song.Song
import com.unipi.george.chordshub.repository.firestore.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SongViewModel(private val songRepo: SongRepository) : ViewModel() {

    private val _songState = MutableStateFlow<Song?>(null)
    val songState: StateFlow<Song?> = _songState

    fun loadSong(songId: String, fallbackToTitle: Boolean = true) {
        viewModelScope.launch {
            var song = songRepo.getSongDataAsync(songId)
            if (song == null && fallbackToTitle) {
                song = songRepo.getSongByTitle(songId)
            }

            _songState.value = song
        }
    }
    fun updateLocalSong(updated: Song) {
        _songState.value = updated
    }


    fun registerSongView(songId: String) {
        viewModelScope.launch {
            songRepo.incrementSongViewCount(songId)
        }
    }
}
