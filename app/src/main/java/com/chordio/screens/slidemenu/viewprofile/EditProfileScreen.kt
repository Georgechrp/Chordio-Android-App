package com.chordio.screens.slidemenu.viewprofile

import  androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chordio.R
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.CoroutineScope
import android.net.Uri
import androidx.compose.ui.res.stringResource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.chordio.viewmodels.auth.AuthViewModel
import kotlinx.coroutines.tasks.await

@Composable
fun EditProfileScreen(navController: NavController, userId: String, onDismiss: () -> Unit, authViewModel: AuthViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Αποθηκεύουμε την αρχική κατάσταση
    val initialUsername = remember { authViewModel.username ?: "Unknown" }
    var newUsername by remember { mutableStateOf(initialUsername) }

    val initialImageUri = remember { mutableStateOf<Uri?>(null) }
    var selectedImage by remember { mutableStateOf<Uri?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            ProfileCard(
                authViewModel = authViewModel,
                newUsername = newUsername.toString(),
                onUsernameChange = { newUsername = it },
                onSave = {
                    keyboardController?.hide()

                    val usernameChanged = newUsername != initialUsername
                    val imageChanged = selectedImage != initialImageUri.value

                    saveProfileChanges(
                        userId,
                        newUsername.toString(),
                        selectedImage,
                        snackbarHostState,
                        coroutineScope,
                        usernameChanged,
                        imageChanged
                    )
                },
                onCancel = {
                    keyboardController?.hide()
                    onDismiss()
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun ProfileCard(
    authViewModel: AuthViewModel,
    newUsername: String,
    onUsernameChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val userId = authViewModel.getUserId() ?: return
    Card(
        modifier = Modifier.width(320.dp).padding(24.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UsernameInputField(newUsername, onUsernameChange)
            Spacer(modifier = Modifier.height(20.dp))
            SaveButton(onSave)
            CancelButton(onCancel)
        }
    }
}

suspend fun getProfileImageUrl(userId: String): String? {
    return try {
        val snapshot = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .await()

        snapshot.getString("profileImageUrl")
    } catch (e: Exception) {
        null
    }
}


suspend fun uploadImageToFirebase(imageUri: Uri, userId: String): String {
    val storageRef = FirebaseStorage.getInstance().reference
    val imageRef = storageRef.child("profile_pictures/$userId.jpg")

    return try {
        imageRef.putFile(imageUri).await() //  Ανεβάζουμε την εικόνα
        imageRef.downloadUrl.await().toString() //  Παίρνουμε το URL και το επιστρέφουμε
    } catch (e: Exception) {
        throw Exception("Failed to upload image: ${e.message}")
    }
}

fun saveProfileChanges(
    userId: String,
    newUsername: String,
    newImage: Uri?,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    usernameChanged: Boolean,
    imageChanged: Boolean
) {
    coroutineScope.launch {
        try {
            var imageUrl: String? = null

            // Αν υπάρχει νέα εικόνα, την ανεβάζουμε στο Firebase Storage
            if (imageChanged && newImage != null) {
                imageUrl = uploadImageToFirebase(newImage, userId)
            }

            //  Ενημέρωση Firestore με το νέο username και το image URL
            val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

            val updates = mutableMapOf<String, Any>()
            if (usernameChanged) updates["username"] = newUsername
            if (imageUrl != null) updates["profileImageUrl"] = imageUrl

            if (updates.isNotEmpty()) {
                userRef.update(updates).await()
            }

            //  Εμφάνιση μηνύματος επιτυχίας
            val message = when {
                usernameChanged && imageChanged -> "Username and profile picture updated!"
                usernameChanged -> "Username updated successfully!"
                imageChanged -> "Profile picture updated successfully!"
                else -> "No changes were made."
            }

            snackbarHostState.showSnackbar(message)

        } catch (e: Exception) {
            snackbarHostState.showSnackbar("Error updating profile: ${e.message}")
        }
    }
}


@Composable
fun UsernameInputField(newUsername: String, onUsernameChange: (String) -> Unit) {
    OutlinedTextField(
        value = newUsername,
        onValueChange = onUsernameChange,
        label = { Text(stringResource(R.string.username_text)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun SaveButton(onSave: () -> Unit) {
    Button(
        onClick = onSave,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.save_changes_text))
    }
}

@Composable
fun CancelButton(onCancel: () -> Unit) {
    TextButton(
        onClick = onCancel
    ) {
        Text(stringResource(R.string.cancel_button_text), color = Color.Gray)
    }
}
