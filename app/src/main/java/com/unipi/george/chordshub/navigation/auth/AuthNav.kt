package com.unipi.george.chordshub.navigation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.unipi.george.chordshub.navigation.AppScreens
import com.unipi.george.chordshub.screens.auth.LoginScreen
import com.unipi.george.chordshub.screens.auth.SignUpScreen
import com.unipi.george.chordshub.repository.AuthRepository
import com.unipi.george.chordshub.screens.auth.ForgotPasswordScreen

/*
*   Navigates between the login, sign up & forgot password screens
 */

@Composable
fun AuthNav(
    navController: NavHostController,
    isUserLoggedInState: MutableState<Boolean>
) {
    NavHost(
        navController = navController,
        startDestination = AppScreens.Login.route
    ) {
        composable(AppScreens.Login.route) {
            val fullNameState = AuthRepository.fullNameState
            LoginScreen(navController) {
                isUserLoggedInState.value = true
                fullNameState.value = AuthRepository.getFullName()
            }
        }
        composable(AppScreens.SignUp.route) {
            SignUpScreen(navController)
        }
        composable(AppScreens.ForgotPassword.route) {
            ForgotPasswordScreen(authRepository = AuthRepository, onBack = { navController.popBackStack() })
        }
    }
}
