package com.chordio.screens.slidemenu.viewprofile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.chordio.R
import com.chordio.viewmodels.auth.AuthViewModel
import com.chordio.viewmodels.auth.SessionViewModel

@Composable
fun ProfileScreen(navController: NavController, sessionViewModel : SessionViewModel) {
    val showDeleteDialog = remember { mutableStateOf(false) }
    val showLogoutDialog = remember { mutableStateOf(false) }
    val showEditUsernameDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    val fullName by authViewModel.fullName
    val email by authViewModel.email
    val username by authViewModel.username
    val deleteMessage by authViewModel.deleteMessage

    LaunchedEffect(Unit) {
        authViewModel.loadProfileData()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Top
        ) {
            ProfileCard(fullName, email, username, showLogoutDialog, showDeleteDialog, showEditUsernameDialog, deleteMessage)
        }
    }

    if (showDeleteDialog.value) {
        ConfirmActionDialog(
            title = stringResource(R.string.delete_acc_text),
            message = stringResource(R.string.Are_you_sure_you_want_to_delete_account),
            onConfirm = {
                authViewModel.deleteUserAccount { success ->
                    showDeleteDialog.value = false
                    if (success) {
                        authViewModel.logout {
                            Toast.makeText(context, "Account Deleted", Toast.LENGTH_SHORT).show()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    } else {
                        Toast.makeText(context, deleteMessage ?: "Unknown error", Toast.LENGTH_SHORT).show()
                    }
                }

            },
            onDismiss = { showDeleteDialog.value = false }
        )
    }

    if (showLogoutDialog.value) {
        ConfirmActionDialog(
            title = stringResource(R.string.logout_text),
            message = stringResource(R.string.Are_you_sure_you_want_to_logout),
            onConfirm = {
                authViewModel.logout {
                    sessionViewModel.endSession(false) // ή sessionViewModel.handleUserSession(null)
                    showLogoutDialog.value = false
                }


            },
            onDismiss = { showLogoutDialog.value = false }
        )
    }

    if (showEditUsernameDialog.value) {
        EditUsernameDialog(
            currentUsername = username,
            onConfirm = { newUsername ->
                authViewModel.updateUsername(newUsername) { success ->
                    if (success) {
                        Toast.makeText(context, "Username updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to update username", Toast.LENGTH_SHORT).show()
                    }
                    showEditUsernameDialog.value = false
                }

                showEditUsernameDialog.value = false
            },
            onDismiss = { showEditUsernameDialog.value = false }
        )
    }
}

@Composable
fun ProfileCard(
    fullName: String?,
    email: String?,
    username: String?,
    showLogoutDialog: MutableState<Boolean>,
    showDeleteDialog: MutableState<Boolean>,
    showEditUsernameDialog: MutableState<Boolean>,
    deleteMessage: String?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            InfoRow(label = stringResource(R.string.full_name), value = fullName)
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            EditableInfoRow(label = stringResource(R.string.username_text), value = username) {
                showEditUsernameDialog.value = true
            }
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            InfoRow(label = stringResource(R.string.email), value = email)

            Spacer(modifier = Modifier.height(440.dp))

            OutlinedButton(
                onClick = { showLogoutDialog.value = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(stringResource(R.string.logout_text))
            }

            OutlinedButton(
                onClick = { showDeleteDialog.value = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Account Icon",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(stringResource(R.string.delete_acc_text))
            }

            deleteMessage?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun EditUsernameDialog(currentUsername: String?, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var newUsername by remember { mutableStateOf(currentUsername ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_username_text)) },
        text = {
            TextField(
                value = newUsername,
                onValueChange = { newUsername = it },
                label = { Text(stringResource(R.string.username_text)) }
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(newUsername) }) { Text(stringResource(R.string.save_button_text)) }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text(stringResource(R.string.cancel_button_text)) }
        }
    )
}

@Composable
fun InfoRow(label: String, value: String?) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value ?: "-",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun EditableInfoRow(label: String, value: String?, onEditClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value ?: "-",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        IconButton(onClick = onEditClick) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
        }
    }
}

@Composable
fun ConfirmActionDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onConfirm) { Text(stringResource(R.string.yes_text)) }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text(stringResource(R.string.no_thanks_text)) }
        }
    )
}
