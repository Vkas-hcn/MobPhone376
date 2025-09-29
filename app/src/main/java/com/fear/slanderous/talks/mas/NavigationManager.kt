package com.fear.slanderous.talks.mas

import android.content.Context
import android.content.Intent
import com.fear.slanderous.talks.dwj.TssDwj
import com.fear.slanderous.talks.ohs.fx.TssFx
import com.fear.slanderous.talks.sm.TssSm
import com.fear.slanderous.talks.zp.TssZp

interface NavigationCallback {
    fun onNavigationRequested(action: NavigationAction)
}

class NavigationManager(private val context: Context) {

    fun navigate(action: NavigationAction) {
        val intent = when (action) {
            is NavigationAction.JunkClean -> Intent(context, TssSm::class.java)
            is NavigationAction.ImageClean -> Intent(context, TssZp::class.java)
            is NavigationAction.FileClean -> Intent(context, TssDwj::class.java)
            is NavigationAction.Settings -> Intent(context, TssFx::class.java)
        }
        context.startActivity(intent)
    }
}