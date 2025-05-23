package com.unipi.george.chordshub.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.unipi.george.chordshub.viewmodels.auth.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter your email to reset password")
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            authViewModel.resetPassword(email) { success, errorMessage ->
                message = if (success) "Check your email for reset link" else errorMessage
            }
        }) {
            Text("Reset Password")
        }
        message?.let {
            Text(it, color = Color.Red)
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onBack) {
            Text("Back to Login")
        }
    }
}
