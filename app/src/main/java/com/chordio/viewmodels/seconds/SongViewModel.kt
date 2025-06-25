package com.chordio.viewmodels.seconds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chordio.models.song.Song
import com.chordio.repository.UserStatsRepository
import com.chordio.repository.firestore.SongRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SongViewModel(private val songRepo: SongRepository, private val userStatsRepository: UserStatsRepository) : ViewModel() {

    private val _songState = MutableStateFlow<Song?>(null)
    val songState: StateFlow<Song?> = _songState

    private val _viewsCount = MutableStateFlow<Int?>(null)
    val viewsCount: StateFlow<Int?> = _viewsCount

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

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

    // SongViewModel.kt
    suspend fun getSongById(songId: String): Song? {
        println("[getSongById] Looking up: $songId")
        return songRepo.getSongDataAsync(songId)
    }



    fun loadSong(songId: String, fallbackToTitle: Boolean = true) {
        viewModelScope.launch {
            _isLoading.value = true
            _songState.value = null  // clear previous to avoid flicker

            var song = songRepo.getSongDataAsync(songId)
            if (song == null && fallbackToTitle) {
                song = songRepo.getSongByTitle(songId)
            }

            _songState.value = song
            _isLoading.value = false
        }
    }

    fun updateLocalSong(updated: Song) {
        _songState.value = updated
    }
    fun onSongOpened(userId: String, song: Song) {
        userStatsRepository.incrementTotalSongsViewed(userId)
        userStatsRepository.incrementGenreAndArtistClick(userId, song.genres.toString(), song.artist)
    }
    fun clearSongState() {
        _songState.value = null
    }

    fun getTopSongs(limit: Int = 20, callback: (List<Song>) -> Unit) {
        viewModelScope.launch {
            songRepo.getTopSongs(limit) { songCardItems ->
                viewModelScope.launch {
                    val fullSongs = songCardItems.mapNotNull { item ->
                        async {
                            songRepo.getSongById(item.id)
                        }
                    }.mapNotNull { deferred ->
                        deferred.await()
                    }
                    callback(fullSongs)
                }
            }
        }
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
