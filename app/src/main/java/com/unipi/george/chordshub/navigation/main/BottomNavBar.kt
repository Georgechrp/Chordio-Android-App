package com.unipi.george.chordshub.navigation.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import com.unipi.george.chordshub.navigation.AppScreens
import com.unipi.george.chordshub.ui.theme.BottomNavBackground
import com.unipi.george.chordshub.viewmodels.MainViewModel

/*
*   Main Navigate between Home, Search, Library
*   Hide the bar when the screen is full-screen
*/
@Composable
fun BottomNavBar(
    navController: NavController,
    mainViewModel: MainViewModel
) {
    val isVisible by mainViewModel.bottomBarVisible.collectAsState()

    if (!isVisible) return

    val items = listOf(
        AppScreens.Home,
        AppScreens.Search,
        AppScreens.Library
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        color = BottomNavBackground,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 20.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                val isSelected = currentRoute == screen.route

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            navController.navigate(screen.route) {
                                popUpTo(screen.route) { inclusive = true }
                                launchSingleTop = false
                                restoreState = true
                            }
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = when (screen) {
                            is AppScreens.Home -> Icons.Filled.Home
                            is AppScreens.Search -> Icons.Filled.Search
                            is AppScreens.Library -> Icons.Filled.LibraryMusic
                            else -> Icons.Filled.Home
                        },
                        contentDescription = screen.route,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(28.dp)
                    )

                    AnimatedVisibility(visible = isSelected) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(3.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        )
                    }
                }
            }
        }
    }
}
