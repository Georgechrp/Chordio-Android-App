package com.chordio.viewmodels.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.firestore.FirebaseFirestore
import com.chordio.models.song.SongCardItem
import com.chordio.repository.firestore.SearchRepository
import com.chordio.repository.firestore.SongRepository

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

    private val _genres = MutableStateFlow<List<String>>(emptyList())
    val genres: StateFlow<List<String>> = _genres

    private val _artistList = MutableStateFlow<List<String>>(emptyList())
    val artistList: StateFlow<List<String>> = _artistList

    fun fetchAllArtists() {
        songRepo.getAllArtists { list ->
            _artistList.value = list
        }
    }


    private fun fetchGenres() {
        songRepo.getGenres { genreList ->
            _genres.value = genreList
        }
    }
    fun searchByGenre(genre: String) {
        songRepo.getSongsByGenre(genre) { songs ->
            _searchResults.value = songs.map { (titleArtist, id) ->
                val parts = titleArtist.split(" - ")
                val title = parts.getOrNull(0) ?: titleArtist
                val artist = parts.getOrNull(1) ?: "Άγνωστος"
                Triple(title, artist, genre)
            }
        }
    }




    init {
        fetchRandomSongs()
        fetchTopSongs()
        fetchGenres()
    }

    private fun fetchTopSongs(limit: Int = 5) {
        songRepo.getTopSongs(limit) { songs ->
            _topSongs.value = songs
        }
    }


    fun clearSearchResults() {
        _searchResults.value = emptyList()
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
