package com.chordio.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.chordio.repository.AuthRepository
import com.chordio.viewmodels.auth.SessionViewModel

@Composable
fun ObserveUserSession(sessionViewModel: SessionViewModel) {
    val userId = AuthRepository.getUserId()
    LaunchedEffect(userId) {
        sessionViewModel.handleUserSession(userId)
    }
}
