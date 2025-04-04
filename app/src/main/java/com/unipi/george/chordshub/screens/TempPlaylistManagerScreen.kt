package com.unipi.george.chordshub.screens

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.unipi.george.chordshub.components.CardsView
import com.unipi.george.chordshub.viewmodels.main.HomeViewModel
import com.unipi.george.chordshub.viewmodels.seconds.TempPlaylistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TempPlaylistManagerScreen(
    tempPlaylistViewModel: TempPlaylistViewModel,
    homeViewModel: HomeViewModel,
) {
    val showBottomSheet by tempPlaylistViewModel.isBottomSheetVisible.collectAsState()

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                tempPlaylistViewModel.hideBottomSheet()
            }
        ) {
            // Display the CardsView with songs from tempPlaylist
            CardsView(
                songList = tempPlaylistViewModel.state.value.songIds.map { songId -> Pair(songId, songId) },
                homeViewModel = homeViewModel,
                selectedTitle = remember { mutableStateOf<String?>(null) }
            )
        }
    }

}

