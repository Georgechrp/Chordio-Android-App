package com.chordio.viewmodels.seconds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chordio.models.song.Song
import com.chordio.repository.UserStatsRepository
import com.chordio.repository.firestore.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SongViewModel(private val songRepo: SongRepository, private val userStatsRepository: UserStatsRepository) : ViewModel() {

    private val _songState = MutableStateFlow<Song?>(null)
    val songState: StateFlow<Song?> = _songState

    private val _viewsCount = MutableStateFlow<Int?>(null)
    val viewsCount: StateFlow<Int?> = _viewsCount

    fun fetchViewsCount(songId: String) {
        viewModelScope.launch {
            val count = songRepo.getSongViewsCount(songId)
            _viewsCount.value = count
        }
    }

    fun getSongsByArtistName(artistName: String, callback: (List<Song>) -> Unit) {
        viewModelScope.launch {
            songRepo.getSongsByArtistName(artistName, callback)
        }
    }


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
    fun onSongOpened(userId: String, song: Song) {
        userStatsRepository.incrementTotalSongsViewed(userId)
        userStatsRepository.incrementGenreAndArtistClick(userId, song.genres.toString(), song.artist)
    }


    suspend fun uploadSong(song: Song): Boolean {
        return songRepo.uploadSong(song)
    }


    suspend fun getViewsCount(songId: String): Int? {
        return songRepo.getSongViewsCount(songId)
    }


    fun registerSongView(songId: String) {
        viewModelScope.launch {
            songRepo.incrementSongViewCount(songId)
        }
    }

    fun getSongsByGenres(genres: List<String>, callback: (List<Song>) -> Unit) {
        viewModelScope.launch {
            val results = songRepo.getSongsByGenres(genres)
            callback(results)
        }
    }

}
