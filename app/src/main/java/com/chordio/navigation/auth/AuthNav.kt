package com.chordio.navigation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.chordio.navigation.AppScreens
import com.chordio.screens.auth.LoginScreen
import com.chordio.screens.auth.SignUpScreen
import com.chordio.screens.auth.ForgotPasswordScreen
import com.chordio.viewmodels.auth.AuthViewModel

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
            }
        }
        composable(AppScreens.SignUp.route) {
            SignUpScreen(
                navController = navController,
                onLoginSuccess = { isUserLoggedInState.value = true }
            )
        }
        composable(
            route = "forgot_password?email={email}",
            arguments = listOf(navArgument("email") {
                defaultValue = ""
                nullable = true
            })
        ) { backStackEntry ->
            val emailArg = backStackEntry.arguments?.getString("email") ?: ""
            ForgotPasswordScreen(
                authViewModel = authViewModel,
                prefilledEmail = emailArg,
                onBack = { navController.popBackStack() }
            )
        }

    }
}
