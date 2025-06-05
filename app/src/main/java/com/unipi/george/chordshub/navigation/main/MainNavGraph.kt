package com.unipi.george.chordshub.navigation.main

import android.annotation.SuppressLint
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.google.firebase.firestore.FirebaseFirestore
import com.unipi.george.chordshub.navigation.AppScreens
import com.unipi.george.chordshub.repository.firestore.TempPlaylistRepository
import com.unipi.george.chordshub.screens.TempPlaylistManagerScreen
import com.unipi.george.chordshub.screens.main.*
import com.unipi.george.chordshub.screens.viewsong.ArtistScreen
import com.unipi.george.chordshub.screens.viewsong.DetailedSongView
import com.unipi.george.chordshub.screens.slidemenu.viewprofile.EditProfileScreen
import com.unipi.george.chordshub.screens.viewsong.PlaylistDetailScreen
import com.unipi.george.chordshub.screens.slidemenu.viewprofile.ProfileScreen
import com.unipi.george.chordshub.screens.slidemenu.options.RecentsScreen
import com.unipi.george.chordshub.screens.slidemenu.options.SettingsScreen
import com.unipi.george.chordshub.screens.slidemenu.options.UploadScreen
import com.unipi.george.chordshub.screens.slidemenu.options.WeeklyStatsScreen
import com.unipi.george.chordshub.sharedpreferences.AppSettingsPreferences
import com.unipi.george.chordshub.viewmodels.main.HomeViewModel
import com.unipi.george.chordshub.viewmodels.MainViewModel
import com.unipi.george.chordshub.viewmodels.TempPlaylistViewModelFactory
import com.unipi.george.chordshub.viewmodels.auth.AuthViewModel
import com.unipi.george.chordshub.viewmodels.auth.SessionViewModel
import com.unipi.george.chordshub.viewmodels.main.SearchViewModel
import com.unipi.george.chordshub.viewmodels.seconds.SongViewModel
import com.unipi.george.chordshub.viewmodels.seconds.TempPlaylistViewModel
import com.unipi.george.chordshub.viewmodels.user.SettingsViewModel
import com.unipi.george.chordshub.viewmodels.user.UserViewModel

/*
 * Main navigation graph for the app.
 *
 * Defines the navigation structure between all main and secondary screens,
 * including Home, Search, Upload, Library, Settings, Profile, Recents,
 * as well as deep-linked screens like DetailedSongView, ArtistScreen, and PlaylistDetail.
 *
 * It handles:
 * - Full screen state management
 * - Menu visibility (ProfileMenu)
 * - Argument passing (e.g., artistName, playlistName)
 * - ViewModel instantiation and sharing across composables
 */


@Suppress("UNREACHABLE_CODE")
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun MainNavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    sessionViewModel: SessionViewModel
) {
    val homeViewModel: HomeViewModel = viewModel()
    val searchViewModel: SearchViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()


    val appSettingsPreferences = AppSettingsPreferences(navController.context)
    val settingsViewModel = SettingsViewModel(appSettingsPreferences)
    val isMenuOpen by mainViewModel.isMenuOpen
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val repository = TempPlaylistRepository(FirebaseFirestore.getInstance())
    val factory = TempPlaylistViewModelFactory(repository)
    val tempPlaylistViewModel: TempPlaylistViewModel = viewModel(factory = factory)
    val authViewModel: AuthViewModel = viewModel()

    // Κλείσιμο του μενού όταν αλλάζει το BackStack
    LaunchedEffect(navBackStackEntry) {
        if (isMenuOpen) {
            mainViewModel.setMenuOpen(false)
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppScreens.Home.route
    ) {

        composable(AppScreens.Home.route) {
            HomeScreen(
                homeViewModel = homeViewModel,
                mainViewModel = mainViewModel,
                searchViewModel = searchViewModel,
                userViewModel = userViewModel,
                authViewModel = authViewModel,
                navController = navController
            )
        }

        composable(AppScreens.Search.route) {
            SearchScreen(
                viewModel = searchViewModel,
                mainViewModel = mainViewModel,
                authViewModel = authViewModel,
                homeViewModel = homeViewModel,
                navController = navController,
                onFullScreenChange = { homeViewModel.setFullScreen(it) }
            )
        }

        composable(AppScreens.Upload.route) {
            UploadScreen(
                navController = navController,
                userViewModel
            )
        }

        composable(AppScreens.Library.route) {
            LibraryScreen(
                navController = navController,
                mainViewModel = mainViewModel
            )
        }

        composable(AppScreens.Profile.route) {
            ProfileScreen(navController, sessionViewModel )
        }


        composable("artist/{artistName}") { backStackEntry ->
            val artistName = backStackEntry.arguments?.getString("artistName") ?: "Άγνωστος Καλλιτέχνης"
            ArtistScreen(artistName = artistName, navController = navController, mainViewModel = mainViewModel, homeViewModel = homeViewModel, authViewModel = authViewModel)
        }

        composable("edit_profile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            EditProfileScreen(navController, userId, onDismiss = { navController.popBackStack() }, authViewModel)
        }

        composable(AppScreens.Settings.route) {
            SettingsScreen(navController = navController, settingsViewModel = settingsViewModel)
        }

        composable(AppScreens.Recents.route) {
            RecentsScreen(navController = navController, userViewModel = userViewModel, authViewModel = authViewModel, homeViewModel = homeViewModel, searchViewModel = searchViewModel)
        }

        composable("recentsScreen") {
            RecentsScreen(navController, userViewModel, authViewModel, homeViewModel, searchViewModel)
        }

        composable(
            route = "detailedSongView/{songTitle}",
            arguments = listOf(navArgument("songTitle") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val songTitle = backStackEntry.arguments?.getString("songTitle")

            DetailedSongView(
                songId = songTitle ?: return@composable,
                onBack = { navController.popBackStack() },
                navController = navController,
                mainViewModel = mainViewModel,
                homeViewModel = homeViewModel,
                userViewModel = userViewModel,
                authViewModel = authViewModel
            )
        }

        composable("playlist_detail/{playlistName}") { backStackEntry ->
            val name = backStackEntry.arguments?.getString("playlistName") ?: return@composable
            PlaylistDetailScreen(
                playlistName = name,
                onBack = { navController.popBackStack() },
                viewModel = viewModel(),
                mainViewModel = mainViewModel,
                homeViewModel = homeViewModel,
                userViewModel = userViewModel,
                authViewModel = authViewModel,
                navController = navController
            )
        }

        composable("temp_playlist") {
            TempPlaylistManagerScreen(
                tempPlaylistViewModel = tempPlaylistViewModel,
                homeViewModel = homeViewModel
            )
        }

        composable(AppScreens.Stats.route) {
            WeeklyStatsScreen(
                userId = authViewModel.getUserId() ?: return@composable,
                navController = navController
            )
        }

    }
}
