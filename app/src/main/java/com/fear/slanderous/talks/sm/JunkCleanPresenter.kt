package com.fear.slanderous.talks.sm

import kotlinx.coroutines.*

interface JunkCleanPresenter {
    fun attachView(view: JunkCleanView)
    fun detachView()
    fun startScan()
    fun startClean()
    fun toggleCategorySelection(category: JunkCategory)
    fun toggleCategoryExpansion(category: JunkCategory)
    fun toggleFileSelection(file: JunkFile, category: JunkCategory)
}


class JunkCleanPresenterImpl(
    private val repository: JunkFileRepository
) : JunkCleanPresenter {

    private var view: JunkCleanView? = null
    private val presenterScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var categories: MutableList<JunkCategory> = mutableListOf()
    private var totalSize: Long = 0L
    private var selectedSize: Long = 0L
    private var isScanning = false

    init {
        initializeCategories()
    }

    override fun attachView(view: JunkCleanView) {
        this.view = view
        updateView()
    }

    override fun detachView() {
        view = null
        presenterScope.cancel()
    }

    override fun startScan() {
        if (isScanning) return

        isScanning = true
        view?.showProgressBar(true, 0)

        presenterScope.launch {
            repository.scanJunkFiles(object : ScanProgressListener {
                override fun onScanStarted() {
                    CoroutineScope(Dispatchers.Main).launch {
                        view?.showScanning(0, "Starting scan...")
                    }

                }

                override fun onScanProgress(progress: Int, currentPath: String) {
                    view?.showScanning(progress, currentPath)
                    view?.showProgressBar(true, progress)
                }

                override fun onFileFound(junkFile: JunkFile) {
                    addJunkFile(junkFile)
                }

                override fun onScanCompleted(totalFiles: Int, totalSize: Long) {
                    isScanning = false
                    view?.showProgressBar(false)
                    view?.showScanCompleted(totalFiles, totalSize)
                    selectAllFiles()
                    expandCategoriesWithFiles()

                    // Update UI based on scan results
                    if (totalSize > 0) {
                        view?.updateBackgroundForJunk(true)
                        view?.showCleanButton(true)
                    } else {
                        view?.showCleanButton(false)
                    }

                    updateView()
                }

                override fun onScanError(error: Throwable) {
                    isScanning = false
                    view?.showProgressBar(false)
                    view?.showScanError(error.message ?: "Unknown error")
                }
            })
        }
    }

    override fun startClean() {
        val selectedFiles = getSelectedFiles()
        if (selectedFiles.isEmpty()) return

        view?.enableCleanButton(false)

        presenterScope.launch {
            repository.cleanFiles(selectedFiles, object : CleanProgressListener {
                override fun onCleanStarted() {
                    CoroutineScope(Dispatchers.Main).launch {
                        view?.showCleaning(0, "Starting clean...")
                    }
                }

                override fun onCleanProgress(progress: Int, currentFile: String) {
                    view?.showCleaning(progress, currentFile)
                }

                override fun onCleanCompleted(deletedCount: Int, deletedSize: Long) {
                    view?.showCleanCompleted(deletedCount, deletedSize)
                    view?.navigateToCleanSuccess(deletedSize)
                }

                override fun onCleanError(error: Throwable) {
                    view?.enableCleanButton(true)
                    view?.showCleanError(error.message ?: "Unknown error")
                }
            })
        }
    }

    override fun toggleCategorySelection(category: JunkCategory) {
        val index = categories.indexOfFirst { it.type == category.type }
        if (index != -1) {
            val updatedCategory = categories[index].copy(
                isSelected = !category.isSelected
            )
            updatedCategory.files.forEach { it.isSelected = updatedCategory.isSelected }
            categories[index] = updatedCategory
            updateSelectedSize()
            updateView()
        }
    }

    override fun toggleCategoryExpansion(category: JunkCategory) {
        val index = categories.indexOfFirst { it.type == category.type }
        if (index != -1) {
            categories[index] = categories[index].copy(
                isExpanded = !category.isExpanded
            )
            updateView()
        }
    }

    override fun toggleFileSelection(file: JunkFile, category: JunkCategory) {
        val categoryIndex = categories.indexOfFirst { it.type == category.type }
        if (categoryIndex != -1) {
            val fileIndex = categories[categoryIndex].files.indexOfFirst { it.path == file.path }
            if (fileIndex != -1) {
                categories[categoryIndex].files[fileIndex].isSelected = !file.isSelected
                val hasSelectedFiles = categories[categoryIndex].files.any { it.isSelected }
                categories[categoryIndex] = categories[categoryIndex].copy(
                    isSelected = hasSelectedFiles
                )
                updateSelectedSize()
                updateView()
            }
        }
    }

    private fun initializeCategories() {
        categories = JunkFileTypeMy.values().map { type ->
            JunkCategory(type = type)
        }.toMutableList()
        totalSize = 0L
        selectedSize = 0L
    }

    private fun addJunkFile(junkFile: JunkFile) {
        val categoryIndex = categories.indexOfFirst { it.type == junkFile.type }
        if (categoryIndex != -1) {
            categories[categoryIndex].files.add(junkFile)
            totalSize += junkFile.size
            updateView()
        }
    }

    private fun selectAllFiles() {
        categories.forEach { category ->
            category.files.forEach { file ->
                file.isSelected = true
            }
            if (category.files.isNotEmpty()) {
                category.isSelected = true
            }
        }
        updateSelectedSize()
    }

    private fun expandCategoriesWithFiles() {
        categories.forEach { category ->
            if (category.files.isNotEmpty()) {
                category.isExpanded = true
            }
        }
    }

    private fun updateSelectedSize() {
        selectedSize = categories.sumOf { it.selectedSize }
    }

    private fun getSelectedFiles(): List<JunkFile> {
        return categories.flatMap { category ->
            category.files.filter { it.isSelected }
        }
    }

    private fun updateView() {
        view?.apply {
            updateCategories(categories.toList())
            updateTotalSize(FileUtils.formatFileSize(totalSize))
            updateSelectedSize(FileUtils.formatFileSize(selectedSize))
            enableCleanButton(selectedSize > 0)
        }
    }
}