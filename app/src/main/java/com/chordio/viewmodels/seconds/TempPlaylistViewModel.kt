package com.chordio.viewmodels.seconds

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.chordio.repository.firestore.TempPlaylistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class TempPlaylistState(
    val playlistId: String? = null,
    val songIds: List<String> = emptyList(),
    val currentSongId: String? = null,
    val isLoading: Boolean = true
)

class TempPlaylistViewModel(repository: TempPlaylistRepository) : ViewModel() {
    private val autoCreatePlalylistRepo = TempPlaylistRepository(FirebaseFirestore.getInstance())
    private val _state = MutableStateFlow(TempPlaylistState())
    val state: StateFlow<TempPlaylistState> = _state


    private val _isBottomSheetVisible = MutableStateFlow(false)
    val isBottomSheetVisible: StateFlow<Boolean> = _isBottomSheetVisible

    fun showBottomSheet() {
        _isBottomSheetVisible.value = true
    }

    fun hideBottomSheet() {
        _isBottomSheetVisible.value = false
    }


    fun createPlaylist(userId: String, firstSongId: String) {
        _state.value = _state.value.copy(isLoading = true)

        autoCreatePlalylistRepo.createTempPlaylist(userId, firstSongId) { playlistId ->
            if (playlistId != null) {
                _state.value = TempPlaylistState(
                    songIds = listOf(firstSongId),
                    currentSongId = firstSongId,
                    isLoading = false
                )
            } else {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun loadPlaylist(playlistId: String) {
        _state.value = _state.value.copy(isLoading = true)

        autoCreatePlalylistRepo.getTempPlaylist(playlistId) { playlist ->
            if (playlist != null) {
                _state.value = TempPlaylistState(
                    playlistId = playlistId,
                    songIds = playlist.songs,
                    currentSongId = playlist.currentSongId,
                    isLoading = false
                )
            } else {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun addSong(songId: String) {
        val playlistId = _state.value.playlistId ?: return
        autoCreatePlalylistRepo.addSongToTempPlaylist(playlistId, songId)

        val updatedList = _state.value.songIds + songId
        _state.value = _state.value.copy(songIds = updatedList)
    }

    fun playSong(songId: String) {
        val playlistId = _state.value.playlistId ?: return
        autoCreatePlalylistRepo.setCurrentSongInTempPlaylist(playlistId, songId)

        _state.value = _state.value.copy(currentSongId = songId)
    }

    fun deletePlaylist() {
        val playlistId = _state.value.playlistId ?: return
        autoCreatePlalylistRepo.deleteTempPlaylist(playlistId) { success ->
            if (success) {
                _state.value = TempPlaylistState()
            }
        }
    }
}
