package com.chordio.screens.slidemenu

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.chordio.R
import com.chordio.navigation.AppScreens
import com.chordio.viewmodels.MainViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import com.chordio.components.BlurredBackground
import com.chordio.components.UserProfileImage
import com.chordio.screens.slidemenu.viewprofile.getProfileImageUrl
import com.chordio.viewmodels.auth.AuthViewModel
import com.chordio.viewmodels.user.UserViewModel
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun ProfileMenu(mainViewModel: MainViewModel, userViewModel: UserViewModel, authViewModel: AuthViewModel, navController: NavController, modifier: Modifier = Modifier) {
    val isMenuOpen by mainViewModel.isMenuOpen

    if (isMenuOpen) {
        BlurredBackground { mainViewModel.setMenuOpen(false) }
    }

    AnimatedVisibility(
        visible = isMenuOpen,
        enter = slideInHorizontally { -it },
        exit = slideOutHorizontally { -it },
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10f)
    ) {
        ProfileMenuContent(mainViewModel, userViewModel, authViewModel, navController)
    }

}

@Composable
fun ProfileMenuContent(
    mainViewModel: MainViewModel,
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val userId = authViewModel.getUserId()
    val username = authViewModel.username.value ?: "User"
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var selectedImage by rememberSaveable { mutableStateOf<Uri?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        userId?.let {
            authViewModel.loadProfileData()
            getProfileImageUrl(it)?.let { url ->
                profileImageUrl = url
                mainViewModel.setProfileImageUrl(url)
            }
        }
    }


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            selectedImage = selectedUri

            coroutineScope.launch {
                if (userId != null) {
                    userViewModel.updateProfileImage(userId, selectedUri) { success ->
                        if (success) {
                            val newUrl = userViewModel.profileImageUrl.value
                            profileImageUrl = newUrl
                            mainViewModel.setProfileImageUrl(newUrl ?: "")
                            selectedImage = null
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10f)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -50) {
                        mainViewModel.setMenuOpen(false)
                    }
                }
            }
        ,
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.85f)
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {

                UserProfileImage(
                    imageUrl = profileImageUrl,
                    localImage = selectedImage,
                    size = 50.dp,
                    border = false,
                    placeholderResId = R.drawable.edit_user_image,
                    onClick = { launcher.launch("image/*") }
                )

                UserProfileSection(
                    username = username,
                    mainViewModel = mainViewModel,
                    navController = navController
                )
            }
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            MenuItem(
                icon = Icons.Filled.AddCircleOutline,
                text = stringResource(R.string.add_song_text),
                route = AppScreens.Upload.route,
                mainViewModel = mainViewModel,
                navController = navController
            )
            MenuItem(
                icon = Icons.Filled.History,
                text = stringResource(R.string.recent_text),
                route = AppScreens.Recents.route,
                mainViewModel = mainViewModel,
                navController = navController
            )
            MenuItem(
                icon = Icons.Filled.Settings,
                text = stringResource(R.string.settings_text),
                route = AppScreens.Settings.route,
                mainViewModel = mainViewModel,
                navController = navController
            )
            MenuItem(
                icon = Icons.Filled.QueryStats,
                text = "Stats",
                route = AppScreens.Stats.route,
                mainViewModel = mainViewModel,
                navController = navController
            )
        }
    }
}


//Head for Slide menu
@Composable
fun UserProfileSection(
    username: String,
    mainViewModel: MainViewModel,
    navController: NavController
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Text(
            text = username,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        TextButton(
            onClick = {
                mainViewModel.setMenuOpen(false)
                navController.navigate(AppScreens.Profile.route)
            },
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.height(20.dp)
        ) {
            Text(stringResource(R.string.view_profile_text), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }

}

//3 usages(options) Add a song, Recent, Settings
@Composable
fun MenuItem(
    icon: ImageVector,
    text: String,
    route: String,
    mainViewModel: MainViewModel,
    navController: NavController
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextButton(
            onClick = {
                mainViewModel.setMenuOpen(false)
                navController.navigate(route)
            },
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = text,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = text,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

