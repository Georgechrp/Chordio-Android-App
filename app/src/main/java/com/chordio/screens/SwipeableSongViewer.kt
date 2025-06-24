package com.chordio.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.google.accompanist.pager.*
import com.chordio.screens.viewsong.DetailedSongView
import com.chordio.viewmodels.MainViewModel
import com.chordio.viewmodels.auth.AuthViewModel
import com.chordio.viewmodels.main.HomeViewModel
import com.chordio.viewmodels.seconds.SongViewModel
import com.chordio.viewmodels.user.UserViewModel

@Composable
fun SwipeableSongViewer(
    songViewModel: SongViewModel,
    songs: List<String>,
    initialSongId: String,
    navController: NavHostController,
    mainViewModel: MainViewModel,
    homeViewModel: HomeViewModel,
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel,
    onExit: () -> Unit
) {
   /* val initialIndex = songs.indexOf(initialSongId).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialIndex)
    HorizontalPager(
        count = songs.size,
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        DetailedSongView(
            songViewModel = songViewModel,
            songId = songs[page],
            onBack = onExit,
            navController = navController,
            mainViewModel = mainViewModel,
            homeViewModel = homeViewModel,
            userViewModel = userViewModel,
            authViewModel = authViewModel)
    }*/
}
