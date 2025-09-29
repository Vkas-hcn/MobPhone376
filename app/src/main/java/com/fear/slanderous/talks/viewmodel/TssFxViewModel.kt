package com.fear.slanderous.talks.viewmodel

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fear.slanderous.talks.ohs.model.NavigationEvent
import com.fear.slanderous.talks.ohs.model.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TssFxViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState<String>>(UiState.Loading)
    val uiState: StateFlow<UiState<String>> = _uiState
    
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: Flow<NavigationEvent> = _navigationEvent
    
    fun openInAppBrowser(url: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Success(url)
                // 发送打开浏览器事件
                val intents = listOf(
                    Intent(Intent.ACTION_VIEW, Uri.parse(url)),
                )
                
                _navigationEvent.emit(
                    NavigationEvent.MultipleJump(
                        intents = intents,
                        delays = listOf(500L, 1000L)
                    )
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to open browser: ${e.message}")
            }
        }
    }
    
    fun shareApp() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Success("Sharing app...")
                // 发送分享事件
                _navigationEvent.emit(
                    NavigationEvent.StartActivity(
                        Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Check out this app: https://play.google.com/store/apps/details?id=com.fear.slanderous.talks")
                            putExtra(Intent.EXTRA_SUBJECT, "Check out this app")
                        }
                    )
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to share app: ${e.message}")
            }
        }
    }
    
    fun navigateBack() {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.Finish)
        }
    }
}