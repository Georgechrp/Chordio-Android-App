package com.chordio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import com.chordio.navigation.main.RootAppEntry
import com.chordio.sharedpreferences.AppSettingsPreferences
import com.chordio.viewmodels.auth.SessionViewModel
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

            val backgroundColor = MaterialTheme.colorScheme.background
            val statusBarColor = backgroundColor.toArgb()
            val isLightTheme = backgroundColor.luminance() > 0.5f

            SideEffect {
                window.statusBarColor = statusBarColor
                WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = isLightTheme
            }
        }


    }


    override fun onStop() {
        super.onStop()
        sessionViewModel.endSession(isChangingConfigurations)
    }
}

