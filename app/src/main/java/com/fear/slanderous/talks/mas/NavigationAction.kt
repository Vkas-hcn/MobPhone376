package com.fear.slanderous.talks.mas

sealed class NavigationAction {
    object JunkClean : NavigationAction()
    object ImageClean : NavigationAction()
    object FileClean : NavigationAction()
    object Settings : NavigationAction()
}