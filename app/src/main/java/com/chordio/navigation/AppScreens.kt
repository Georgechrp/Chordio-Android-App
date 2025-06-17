package com.chordio.navigation
import android.net.Uri

sealed class AppScreens(val route: String) {

    // ---------- Main Sections ----------
    data object Home : AppScreens("Home")
    data object Search : AppScreens("Search")
    data object Upload : AppScreens("Upload")
    data object Library : AppScreens("Library")
    data object Profile : AppScreens("Profile")

    // ---------- Auth ----------
    data object Login : AppScreens("Login")
    data object SignUp : AppScreens("SignUp")
    data object ForgotPassword : AppScreens("forgot_password?email={email}") {
        fun createRoute(email: String): String =
            "forgot_password?email=${Uri.encode(email)}"
    }



    // ---------- Extras ----------
    data object Settings : AppScreens("Settings")
    data object Recents : AppScreens("Recents")
    data object Stats: AppScreens("Stats")
}
