package com.chordio.navigation.main

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.chordio.viewmodels.main.HomeViewModel
import com.chordio.viewmodels.MainViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.chordio.components.MyAppTopBar
import com.chordio.screens.slidemenu.ProfileMenu
import com.chordio.viewmodels.auth.AuthViewModel
import com.chordio.viewmodels.auth.SessionViewModel
import com.chordio.viewmodels.seconds.SongViewModel
import com.chordio.viewmodels.user.UserViewModel

/*
 * Here we have the Main UI
 * - Navigate to MainNavGraph((navigation to all Screens))
 * - Handle the manu state from back button
 * - Prints Bottom Nav Bar when we are in the mainScreens
 */

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScaffold(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    sessionViewModel: SessionViewModel,
    profileImageUrl: String?,
    songViewModel: SongViewModel
) {
    val mainViewModel: MainViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()
    val isMenuOpen by mainViewModel.isMenuOpen
    val isFullScreen by homeViewModel.isFullScreen.collectAsState()
    val bottomBarExcludedScreens = setOf("detailedSongView/{songTitle}")
    val topBarContent = mainViewModel.topBarContent.value
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val topBarScreens = listOf("Home", "Search", "Library")

    BackHandler(enabled = isMenuOpen) {
        Log.d("BackHandler", "Back button pressed - Closing Menu")
        mainViewModel.setMenuOpen(false)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { _ ->
        val topBarVisible by mainViewModel.topBarVisible.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (topBarVisible) Modifier.systemBarsPadding() else Modifier)
        )
        {
            MainNavGraph(
                navController = navController,
                mainViewModel = mainViewModel,
                sessionViewModel = sessionViewModel,
                songViewModel = songViewModel
            )

            val showTopBar by mainViewModel.topBarVisible.collectAsState()

            if (currentRoute in topBarScreens && showTopBar) {
                MyAppTopBar(
                    imageUrl = profileImageUrl,
                    onMenuClick = { mainViewModel.setMenuOpen(true) }
                ) {
                    topBarContent?.invoke(this)
                }
            }



            val showBottomBar by homeViewModel.showBottomBar.collectAsState()

            if (!isFullScreen && currentRoute !in bottomBarExcludedScreens) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .align(Alignment.BottomCenter)
                        .zIndex(1f) // same zIndex as TopBar
                ) {
                    if (showBottomBar) {
                        BottomNavBar(navController = navController, mainViewModel = mainViewModel)
                    }
                }
            }

            // ProfileMenu always on top
            ProfileMenu(
                mainViewModel = mainViewModel,
                userViewModel = userViewModel,
                authViewModel = authViewModel,
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f) // Το μεγαλύτερο zIndex για να καλύπτει TopBar και BottomNav
            )
        }
    }
}

