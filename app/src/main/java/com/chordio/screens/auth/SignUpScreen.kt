package com.chordio.screens.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.chordio.R
import com.chordio.components.LoadingView
import com.chordio.navigation.AppScreens
import com.chordio.viewmodels.auth.AuthViewModel

@Composable
fun SignUpScreen(navController: NavController, onLoginSuccess: () -> Unit) {
    // Αρχικοποίηση των states
    val fullName = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    val context = LocalContext.current


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SignUpInputFields(
            fullName = fullName,
            email = email,
            password = password,
            confirmPassword = confirmPassword
        )
        Spacer(modifier = Modifier.height(16.dp))

        SignUpActions(
            fullName = fullName,
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            navController = navController,
            context = context,
            onLoginSuccess = onLoginSuccess
        )
    }
}

@Composable
fun SignUpInputFields(
    fullName: MutableState<String>,
    email: MutableState<String>,
    password: MutableState<String>,
    confirmPassword: MutableState<String>
) {
    Text(
        text = stringResource(R.string.sign_in_to),
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(50.dp))
    TextField(
        value = fullName.value,
        onValueChange = { fullName.value = it },
        label = { Text(stringResource(R.string.full_name)) },
        singleLine = true,
        maxLines = 1
    )

    Spacer(modifier = Modifier.height(8.dp))

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
        label = { Text("Password") },
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true,
        maxLines = 1
    )
    Spacer(modifier = Modifier.height(8.dp))

    TextField(
        value = confirmPassword.value,
        onValueChange = { confirmPassword.value = it },
        label = { Text("Confirm Password") },
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true,
        maxLines = 1
    )
}

@Composable
fun SignUpActions(
    authViewModel: AuthViewModel = viewModel(),
    fullName: MutableState<String>,
    email: MutableState<String>,
    password: MutableState<String>,
    confirmPassword: MutableState<String>,
    navController: NavController,
    context: Context,
    onLoginSuccess: () -> Unit
) {
    val isLoading = remember { mutableStateOf(false) }


    Button(onClick = {
        if (fullName.value.isBlank() || email.value.isBlank() || password.value.isBlank() || confirmPassword.value.isBlank()) {
            Toast.makeText(context, context.getString(R.string.please_fill_fields), Toast.LENGTH_SHORT).show()
            return@Button
        }

        if (password.value != confirmPassword.value) {
            Toast.makeText(context, context.getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show()
            return@Button
        }
        isLoading.value = true
        authViewModel.signUpUser(
            fullName.value,
            email.value,
            password.value
        ) { success, error ->
            isLoading.value = false
            if (success) {
                Toast.makeText(context, "Account created!", Toast.LENGTH_SHORT).show()
                onLoginSuccess()
            } else {
                Toast.makeText(context, error ?: "Sign up failed.", Toast.LENGTH_SHORT).show()
            }
        }

    }) {
        Text(stringResource(R.string.create_account))
    }
    if (isLoading.value) {
        LoadingView()
        return
    }
    Spacer(modifier = Modifier.height(8.dp))

    TextButton(onClick = {
        Log.d("SignUpScreen", "Navigating to Login: ${AppScreens.Login.route}")
        navController.navigate(AppScreens.Login.route)
    }) {
        Text(stringResource(R.string.already_have_account))
    }




}
