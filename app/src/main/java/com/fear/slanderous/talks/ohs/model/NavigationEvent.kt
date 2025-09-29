package com.fear.slanderous.talks.ohs.model

import android.content.Intent


sealed class NavigationEvent {
    data class StartActivity(val intent: Intent) : NavigationEvent()
    data class MultipleJump(
        val intents: List<Intent>,
        val delays: List<Long> = List(intents.size - 1) { 1000L }
    ) : NavigationEvent()
    
    object Finish : NavigationEvent()
}