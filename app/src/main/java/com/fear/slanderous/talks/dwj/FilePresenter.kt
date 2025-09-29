package com.fear.slanderous.talks.dwj

interface FilePresenter {
    fun attachView(view: FileView)
    fun detachView()
    fun startScanning()
    fun updateFilter(type: String? = null, size: String? = null, time: String? = null)
    fun toggleFileSelection(position: Int)
    fun selectAllFiles()
    fun clearAllSelections()
    fun deleteSelectedFiles()
    fun onFilterTypeClicked()
    fun onFilterSizeClicked()
    fun onFilterTimeClicked()
    fun onDeleteButtonClicked()
    fun onBackLongClicked()
    fun onFilterOptionSelected(filterType: String, option: String)
    fun onDeleteConfirmed()
    fun onSelectionOptionSelected(option: Int)
}