package com.unipi.george.chordshub.navigation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.unipi.george.chordshub.navigation.AppScreens
import com.unipi.george.chordshub.screens.auth.LoginScreen
import com.unipi.george.chordshub.screens.auth.SignUpScreen
import com.unipi.george.chordshub.screens.auth.ForgotPasswordScreen
import com.unipi.george.chordshub.viewmodels.auth.AuthViewModel

/*
*   Navigates between the login, sign up & forgot password screens
 */

@Composable
fun AuthNav(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    isUserLoggedInState: MutableState<Boolean>
) {

    val isLoggedIn = remember {
        mutableStateOf(authViewModel.getUserId() != null)
    }

    val startDestination = if (isLoggedIn.value) {
        AppScreens.Home.route
    } else {
        AppScreens.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppScreens.Login.route) {
            LoginScreen(authViewModel = authViewModel, navController) {
                isUserLoggedInState.value = true
                navController.navigate(AppScreens.Home.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
        composable(AppScreens.SignUp.route) {
            SignUpScreen(navController)
        }
        composable(AppScreens.ForgotPassword.route) {
            ForgotPasswordScreen(authViewModel = authViewModel, onBack = { navController.popBackStack() })
        }
    }
}
