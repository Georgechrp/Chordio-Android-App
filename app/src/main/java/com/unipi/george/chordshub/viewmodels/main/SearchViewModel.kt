package com.unipi.george.chordshub.viewmodels.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.firestore.FirebaseFirestore
import com.unipi.george.chordshub.models.song.SongCardItem
import com.unipi.george.chordshub.repository.firestore.SearchRepository
import com.unipi.george.chordshub.repository.firestore.SongRepository

class SearchViewModel : ViewModel() {
    private val songRepo = SongRepository(FirebaseFirestore.getInstance())
    private val searchRepo = SearchRepository(FirebaseFirestore.getInstance())

    private val _searchResults = MutableStateFlow<List<Triple<String, String, String>>>(emptyList())
    val searchResults: StateFlow<List<Triple<String, String, String>>> = _searchResults

    private val _selectedSongId = MutableStateFlow<String?>(null)
    val selectedSongId: StateFlow<String?> = _selectedSongId

    private val _randomSongs = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val randomSongs: StateFlow<List<Pair<String, String>>> = _randomSongs


    private val _topSongs = MutableStateFlow<List<SongCardItem>>(emptyList())
    val topSongs: StateFlow<List<SongCardItem>> = _topSongs


    init {
        fetchRandomSongs() // Φέρνουμε 5 τυχαία τραγούδια κατά την εκκίνηση
        fetchTopSongs()
    }

    fun fetchTopSongs(limit: Int = 5) {
        songRepo.getTopSongs(limit) { songs ->
            _topSongs.value = songs
        }
    }


    fun clearSearchResults() {
        _searchResults.value = emptyList() // Αδειάζει τη λίστα των αποτελεσμάτων
    }

    fun searchSongs(query: String) {
        if (query.isEmpty()) {
            _searchResults.value = emptyList()
            return
        }

        searchRepo.searchSongs(query) { results ->
            _searchResults.value = results
        }
    }

    fun selectSong(songId: String) {
        _selectedSongId.value = songId
    }

    fun clearSelectedSong() {
        _selectedSongId.value = null
    }

    private fun fetchRandomSongs() {
        songRepo.getRandomSongs(5) { songs ->
            _randomSongs.value = songs
        }
    }
}
