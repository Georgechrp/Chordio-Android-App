package com.chordio.screens.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.chordio.R
import com.chordio.components.LoadingOverlay
import com.chordio.navigation.AppScreens
import com.chordio.viewmodels.auth.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    navController: NavController,
    onLoginSuccess: () -> Unit
) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val context = LocalContext.current
    val isLoading = authViewModel.isLoading.value
    val error = authViewModel.error.value

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = Color.Red)
            }

            Text(
                text = stringResource(R.string.welcome_text),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(50.dp))

            TextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text(stringResource(R.string.email)) },
                singleLine = true,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text(stringResource(R.string.password)) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                maxLines = 1
            )

            TextButton(onClick = {
                navController.navigate(AppScreens.ForgotPassword.createRoute(email.value))
            }) {
                Text(stringResource(R.string.resetPassword_text))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                if (email.value.isBlank() || password.value.isBlank()) {
                    Toast.makeText(context, context.getString(R.string.please_fill_fields), Toast.LENGTH_SHORT).show()
                    return@Button
                }

                authViewModel.loginUser(email.value, password.value) {
                    onLoginSuccess()
                }
            }) {
                Text(stringResource(R.string.sign_in))
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = {
                navController.navigate(AppScreens.SignUp.route)
            }) {
                Text(stringResource(R.string.dont_have_account))
            }
        }

        if (isLoading) {
            LoadingOverlay()
        }
    }
}
