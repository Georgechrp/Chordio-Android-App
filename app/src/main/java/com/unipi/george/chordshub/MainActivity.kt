package com.unipi.george.chordshub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import com.unipi.george.chordshub.navigation.main.RootAppEntry
import com.unipi.george.chordshub.sharedpreferences.AppSettingsPreferences
import com.unipi.george.chordshub.viewmodels.user.SessionViewModel
import androidx.compose.ui.graphics.toArgb


class MainActivity : ComponentActivity() {

    private lateinit var sessionViewModel: SessionViewModel  //late initializing sessionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        AppContainer.appSettingsPreferences = AppSettingsPreferences(this)
        sessionViewModel = ViewModelProvider(this)[SessionViewModel::class.java]

        setContent {
            RootAppEntry(sessionViewModel)

            val statusBarColor = MaterialTheme.colorScheme.background.toArgb()

            SideEffect {
                window.statusBarColor = statusBarColor
                WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
            }
        }
    }


    override fun onStop() {
        super.onStop()
        sessionViewModel.endSession(isChangingConfigurations)
    }
}

