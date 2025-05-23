package com.unipi.george.chordshub.navigation.main


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.unipi.george.chordshub.AppContainer
import com.unipi.george.chordshub.navigation.auth.AuthNav
import com.unipi.george.chordshub.screens.auth.welcomeuser.WelcomeScreen
import com.unipi.george.chordshub.ui.theme.ChordsHubTheme
import com.unipi.george.chordshub.utils.ObserveUserSession
import com.unipi.george.chordshub.viewmodels.SettingsViewModelFactory
import com.unipi.george.chordshub.viewmodels.StorageViewModel
import com.unipi.george.chordshub.viewmodels.auth.AuthViewModel
import com.unipi.george.chordshub.viewmodels.auth.SessionViewModel
import com.unipi.george.chordshub.viewmodels.user.SettingsViewModel
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

    val isLoading = remember { mutableStateOf(true) }

    val splashDone = remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        delay(2000) // 3000
        authViewModel.getUserId()?.let {
            storageViewModel.loadProfileImage(it)
        }
        isLoading.value = false
    }


    ChordsHubTheme(darkTheme = darkMode) {
        val navController = rememberNavController()

        ObserveUserSession(sessionViewModel)


        when {
            !splashDone.value -> WelcomeScreen { splashDone.value = true }
            isUserLoggedInState.value -> MainScaffold(navController, authViewModel, imageUrl)
            else -> AuthNav(navController, authViewModel, isUserLoggedInState)
        }

    }
}

