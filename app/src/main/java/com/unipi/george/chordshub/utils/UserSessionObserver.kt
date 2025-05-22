package com.unipi.george.chordshub.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.unipi.george.chordshub.repository.AuthRepository
import com.unipi.george.chordshub.viewmodels.auth.SessionViewModel

@Composable
fun ObserveUserSession(sessionViewModel: SessionViewModel) {
    val userId = AuthRepository.getUserId()
    LaunchedEffect(userId) {
        sessionViewModel.handleUserSession(userId)
    }
}
