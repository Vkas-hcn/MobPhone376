package com.fear.slanderous.talks.dwj
import kotlinx.coroutines.*
import java.lang.ref.WeakReference

class FilePresenterImpl(
    private val scanFilesUseCase: ScanFilesUseCase,
    private val deleteFilesUseCase: DeleteFilesUseCase,
    private val filterFilesUseCase: FilterFilesUseCase
) : FilePresenter {

    private var viewRef: WeakReference<FileView>? = null
    private val view: FileView? get() = viewRef?.get()

    private val presenterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val allFiles = mutableListOf<FileItem>()
    private val selectedFiles = mutableSetOf<String>()
    private var currentFiles = mutableListOf<FileItem>()
    private var currentFilter = FileFilter()

    private val filterOptions = mapOf(
        "types" to arrayOf("All types", "Image", "Video", "Audio", "Docs", "Download", "Zip"),
        "sizes" to arrayOf("All Size", ">1MB", ">5MB", ">10MB", ">20MB", ">50MB", ">100MB", ">200MB", ">500MB"),
        "times" to arrayOf("All Time", "Within 1 day", "Within 1 week", "Within 1 month", "Within 3 month", "Within 6 month")
    )

    override fun attachView(view: FileView) {
        viewRef = WeakReference(view)
    }

    override fun detachView() {
        presenterScope.cancel()
        viewRef = null
    }

    override fun startScanning() {
        presenterScope.launch {
            view?.showLoading()
            view?.showCleaningDialog()
            delay(2000)
            view?.hideCleaningDialog()

            try {
                val files = withContext(Dispatchers.IO) {
                    scanFilesUseCase()
                }
                allFiles.clear()
                allFiles.addAll(files)
                applyFilters(currentFilter)
            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError(e.message ?: "Unknown error occurred")
            }

        }
    }

    override fun updateFilter(type: String?, size: String?, time: String?) {
        currentFilter = currentFilter.copy(
            type = type ?: currentFilter.type,
            size = size ?: currentFilter.size,
            time = time ?: currentFilter.time
        )
        applyFilters(currentFilter)
    }

    override fun toggleFileSelection(position: Int) {
        if (position !in currentFiles.indices) return

        val file = currentFiles[position]
        val newSelectionState = !file.isSelected

        if (newSelectionState) {
            selectedFiles.add(file.path)
        } else {
            selectedFiles.remove(file.path)
        }

        currentFiles[position] = file.copy(isSelected = newSelectionState)

        val selectedCount = currentFiles.count { it.isSelected }
        view?.showFiles(currentFiles.toList())
        view?.updateDeleteButton(selectedCount > 0, selectedCount)
    }

    override fun selectAllFiles() {
        currentFiles = currentFiles.map { file ->
            selectedFiles.add(file.path)
            file.copy(isSelected = true)
        }.toMutableList()

        view?.showFiles(currentFiles.toList())
        view?.updateDeleteButton(true, currentFiles.size)
    }

    override fun clearAllSelections() {
        selectedFiles.clear()
        currentFiles = currentFiles.map { file ->
            file.copy(isSelected = false)
        }.toMutableList()

        view?.showFiles(currentFiles.toList())
        view?.updateDeleteButton(false, 0)
    }

    override fun deleteSelectedFiles() {
        val selectedFilesList = currentFiles.filter { it.isSelected }
        if (selectedFilesList.isEmpty()) return

        presenterScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    deleteFilesUseCase(selectedFilesList)
                }
                if (result.success) {
                    view?.navigateToFinishActivity(result)
                }
            } catch (e: Exception) {
                view?.showError(e.message ?: "Delete failed")
            }
        }
    }

    override fun onFilterTypeClicked() {
        filterOptions["types"]?.let { options ->
            view?.showFilterDialog("types", options)
        }
    }

    override fun onFilterSizeClicked() {
        filterOptions["sizes"]?.let { options ->
            view?.showFilterDialog("sizes", options)
        }
    }

    override fun onFilterTimeClicked() {
        filterOptions["times"]?.let { options ->
            view?.showFilterDialog("times", options)
        }
    }

    override fun onDeleteButtonClicked() {
        val selectedCount = currentFiles.count { it.isSelected }
        if (selectedCount > 0) {
            view?.showDeleteConfirmDialog(selectedCount)
        }
    }

    override fun onBackLongClicked() {
        view?.showSelectionOptionsDialog()
    }

    override fun onFilterOptionSelected(filterType: String, option: String) {
        when(filterType) {
            "types" -> updateFilter(type = option)
            "sizes" -> updateFilter(size = option)
            "times" -> updateFilter(time = option)
        }
    }

    override fun onDeleteConfirmed() {
        deleteSelectedFiles()
    }

    override fun onSelectionOptionSelected(option: Int) {
        when(option) {
            0 -> selectAllFiles()
            1 -> clearAllSelections()
        }
    }

    private fun applyFilters(filter: FileFilter) {
        val filteredFiles = filterFilesUseCase(allFiles, filter)

        currentFiles = filteredFiles.map { file ->
            file.copy(isSelected = selectedFiles.contains(file.path))
        }.toMutableList()

        view?.hideLoading()
        view?.updateFilters(filter.type, filter.size, filter.time)

        if (currentFiles.isEmpty()) {
            view?.showNoFiles()
            view?.showFiles(emptyList())
        } else {
            view?.hideNoFiles()
            view?.showFiles(currentFiles.toList())
        }

        val selectedCount = currentFiles.count { it.isSelected }
        view?.updateDeleteButton(selectedCount > 0, selectedCount)
    }
}