package com.chordio.viewmodels.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.chordio.repository.AuthRepository
import com.chordio.repository.UserStatsRepository
import com.chordio.utils.UserSessionManager
import kotlinx.coroutines.launch

class SessionViewModel : ViewModel() {

    private val sessionManager = UserSessionManager(UserStatsRepository(FirebaseFirestore.getInstance()))

    val isUserLoggedInState = AuthRepository.isUserLoggedInState

    fun handleUserSession(userId: String?) {
        viewModelScope.launch {
            if (!userId.isNullOrEmpty()) {
                sessionManager.startSession(userId)
            } else {
                sessionManager.endSession(false)
            }
        }
    }

    fun endSession(isChangingConfigurations: Boolean) {
        sessionManager.endSession(isChangingConfigurations)
    }
}
