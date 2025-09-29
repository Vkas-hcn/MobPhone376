package com.fear.slanderous.talks.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fear.slanderous.talks.ohs.model.NavigationEvent
import com.fear.slanderous.talks.ohs.model.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TssOneViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState<Int>>(UiState.Loading)
    val uiState: StateFlow<UiState<Int>> = _uiState
    
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: Flow<NavigationEvent> = _navigationEvent
    
    fun startCountdown() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            // 模拟倒计时过程
            for (i in 100 downTo 0 step 5) {
                delay(100) // 每100毫秒更新一次，总共2秒
                _uiState.value = UiState.Success(i)
            }
            
            // 倒计时结束后执行多次跳转
            performMultipleJumps()
        }
    }
    
    private suspend fun performMultipleJumps() {
        // 创建多个跳转意图
        val intents = listOf(
            Intent(Intent.ACTION_MAIN).apply {
                setClassName("com.fear.slanderous.talks", "com.fear.slanderous.talks.mas.TssMater")
            }
        )
        
        // 发送多次跳转事件
        _navigationEvent.emit(
            NavigationEvent.MultipleJump(
                intents = intents,
                delays = listOf(500L, 1000L) // 在每次跳转之间的延迟时间
            )
        )
    }
    

}