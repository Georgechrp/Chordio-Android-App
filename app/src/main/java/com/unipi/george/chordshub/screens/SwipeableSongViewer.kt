package com.unipi.george.chordshub.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.pager.*
import com.unipi.george.chordshub.screens.viewsong.DetailedSongView
import com.unipi.george.chordshub.viewmodels.MainViewModel
import com.unipi.george.chordshub.viewmodels.auth.AuthViewModel
import com.unipi.george.chordshub.viewmodels.main.HomeViewModel
import com.unipi.george.chordshub.viewmodels.seconds.SongViewModel
import com.unipi.george.chordshub.viewmodels.user.UserViewModel

@Composable
fun SwipeableSongViewer(
    songs: List<String>,
    initialSongId: String,
    navController: NavHostController,
    mainViewModel: MainViewModel,
    homeViewModel: HomeViewModel,
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel,
    onExit: () -> Unit
) {
    val initialIndex = songs.indexOf(initialSongId).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialIndex)
    HorizontalPager(
        count = songs.size,
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        DetailedSongView(
            songId = songs[page],
            onBack = onExit,
            navController = navController,
            mainViewModel = mainViewModel,
            homeViewModel = homeViewModel,
            userViewModel = userViewModel,
            authViewModel = authViewModel
        )
    }
}
