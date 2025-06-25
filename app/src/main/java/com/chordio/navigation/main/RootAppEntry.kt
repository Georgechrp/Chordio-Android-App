package com.chordio.navigation.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.chordio.AppContainer
import com.chordio.components.LoadingView
import com.chordio.navigation.auth.AuthNav
import com.chordio.repository.firestore.SongRepository
import com.chordio.screens.auth.welcomeuser.WelcomeScreen
import com.chordio.screens.slidemenu.viewprofile.getProfileImageUrl
import com.chordio.ui.theme.ChordsHubTheme
import com.chordio.utils.ObserveUserSession
import com.chordio.viewmodels.MainViewModel
import com.chordio.viewmodels.SettingsViewModelFactory
import com.chordio.viewmodels.SongViewModelFactory
import com.chordio.viewmodels.StorageViewModel
import com.chordio.viewmodels.auth.AuthViewModel
import com.chordio.viewmodels.auth.SessionViewModel
import com.chordio.viewmodels.seconds.SongViewModel
import com.chordio.viewmodels.user.SettingsViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

/*
 *
 * - Applies the selected theme (light/dark) based on user preferences
 * - Observes the user's session status
 * - Decides whether to show the authenticated MainScaffold (main app) or the AuthFlowNavGraph (login/register)
 * - Instantiates SettingsViewModel with AppSettingsPreferences
 *
 */


@Composable
fun RootAppEntry(sessionViewModel: SessionViewModel) {
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(AppContainer.appSettingsPreferences)
    )
    val authViewModel: AuthViewModel = viewModel()

    val darkMode = settingsViewModel.darkMode.value
    val isUserLoggedInState = sessionViewModel.isUserLoggedInState

    val storageViewModel = remember { StorageViewModel() }
    val imageUrl by storageViewModel.profileImageUrl

    val isCheckingSession = remember { mutableStateOf(true) }
    val splashDone = remember { mutableStateOf(false) }

    val songViewModel: SongViewModel = viewModel(
        factory = SongViewModelFactory(SongRepository(FirebaseFirestore.getInstance()))
    )


    LaunchedEffect(Unit) {
        authViewModel.validateSession { isValid ->
            isUserLoggedInState.value = isValid
            isCheckingSession.value = false
        }
    }
    val userId = authViewModel.getUserId()
    val mainViewModel: MainViewModel = viewModel()

    LaunchedEffect(userId) {
        userId?.let {
            getProfileImageUrl(it)?.let { url ->
                mainViewModel.setProfileImageUrl(url)
            }
        }
    }


    LaunchedEffect(Unit) {
        delay(2000)
        authViewModel.getUserId()?.let {
            storageViewModel.loadProfileImage(it)
        }
        splashDone.value = true
    }

    ChordsHubTheme(darkTheme = darkMode) {
        val navController = rememberNavController()
        ObserveUserSession(sessionViewModel)

        when {
            !splashDone.value -> {
                WelcomeScreen { splashDone.value = true }
            }
            isCheckingSession.value -> {
                LoadingView()
            }

            isUserLoggedInState.value -> {
                MainScaffold(navController, authViewModel, sessionViewModel, imageUrl, songViewModel)
            }

            else -> {
                AuthNav(navController, authViewModel, isUserLoggedInState)
            }
        }
    }
}


