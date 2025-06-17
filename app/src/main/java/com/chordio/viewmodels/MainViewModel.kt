package com.chordio.viewmodels

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    private val _isMenuOpen = mutableStateOf(false)
    val isMenuOpen: State<Boolean> = _isMenuOpen

    private val _profileImageUrl = MutableStateFlow<String?>(null)
    val profileImageUrl = _profileImageUrl.asStateFlow()

    private val _topBarContent = mutableStateOf<(@Composable RowScope.() -> Unit)?>(null)
    val topBarContent: State<(@Composable RowScope.() -> Unit)?> = _topBarContent

    fun setTopBarContent(content: @Composable RowScope.() -> Unit) {
        _topBarContent.value = content
    }

    fun setProfileImageUrl(url: String?) {
        _profileImageUrl.value = url
    }

    fun setMenuOpen(value: Boolean) {
        _isMenuOpen.value = value
    }

    private val _bottomBarVisible = MutableStateFlow(true)
    val bottomBarVisible: StateFlow<Boolean> = _bottomBarVisible

    fun setBottomBarVisible(visible: Boolean) {
        _bottomBarVisible.value = visible
    }

    private val _topBarVisible = MutableStateFlow(true)
    val topBarVisible: StateFlow<Boolean> = _topBarVisible

    fun setTopBarVisible(visible: Boolean) {
        _topBarVisible.value = visible
    }


}

