package com.unipi.george.chordshub.screens.slidemenu.options

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.unipi.george.chordshub.R
import com.unipi.george.chordshub.components.LoadingView
import com.unipi.george.chordshub.screens.viewsong.DetailedSongView
import com.unipi.george.chordshub.viewmodels.MainViewModel
import com.unipi.george.chordshub.viewmodels.auth.AuthViewModel
import com.unipi.george.chordshub.viewmodels.main.HomeViewModel
import com.unipi.george.chordshub.viewmodels.user.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentsScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel
) {
    val recentSongs by userViewModel.recentSongs
    val userId = userViewModel.userId
    var isLoading by remember { mutableStateOf(true) }
    val selectedSongId by homeViewModel.selectedSongId.collectAsState()
    val mainViewModel = remember { MainViewModel() }
    // Fetch recent songs κατά την είσοδο στην οθόνη
    LaunchedEffect(userId) {
        if (userId != null) {
            userViewModel.fetchRecentSongs(userId)
        }
        isLoading = false
    }

    // Αν έχει επιλεγεί τραγούδι, δείχνουμε την λεπτομέρεια του
    if (selectedSongId != null) {
        DetailedSongView(
            songId = selectedSongId!!,
            onBack = {
                homeViewModel.clearSelectedSong()
            },
            navController = navController,
            mainViewModel = mainViewModel,
            homeViewModel = homeViewModel,
            userViewModel = userViewModel,
            authViewModel = authViewModel
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.recent_text)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Πίσω")
                    }
                }

            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when {
                isLoading -> {
                    LoadingView()
                }

                recentSongs.isEmpty() -> {
                    Text(
                        "Δεν υπάρχουν πρόσφατα τραγούδια.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                }

                else -> {
                    Text(
                        text = "Τα τελευταία τραγούδια που είδες",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )


                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 80.dp)
                    )  {
                        items(recentSongs) { song ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable {
                                        homeViewModel.selectSong(song)
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                            ) {
                                Text(
                                    text = song,
                                    modifier = Modifier.padding(16.dp),
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
