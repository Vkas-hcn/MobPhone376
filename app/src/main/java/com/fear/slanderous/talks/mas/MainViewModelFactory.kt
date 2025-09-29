package com.fear.slanderous.talks.mas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fear.slanderous.talks.mas.MainViewModel
import com.fear.slanderous.talks.mas.NavigationManager
import com.fear.slanderous.talks.mas.StorageInfoManager

class MainViewModelFactory(
    private val storageInfoManager: StorageInfoManager,
    private val navigationManager: NavigationManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(storageInfoManager, navigationManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}