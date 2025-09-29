package com.fear.slanderous.talks.dwj

interface FileView {
    fun showLoading()
    fun hideLoading()
    fun showFiles(files: List<FileItem>)
    fun showNoFiles()
    fun hideNoFiles()
    fun updateDeleteButton(enabled: Boolean, count: Int)
    fun updateFilters(type: String, size: String, time: String)
    fun showError(message: String)
    fun showToast(message: String)
    fun showFilterDialog(filterType: String, options: Array<String>)
    fun showDeleteConfirmDialog(count: Int)
    fun showSelectionOptionsDialog()
    fun navigateToFinishActivity(result: DeleteResult)
    fun showCleaningDialog()
    fun hideCleaningDialog()
}