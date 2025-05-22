package com.unipi.george.chordshub.viewmodels.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.unipi.george.chordshub.repository.AuthRepository
import com.unipi.george.chordshub.repository.UserStatsRepository
import com.unipi.george.chordshub.utils.UserSessionManager
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
