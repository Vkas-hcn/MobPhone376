package com.fear.slanderous.talks.sm


import com.fear.slanderous.talks.sm.JunkCategory
import com.fear.slanderous.talks.sm.JunkFile

interface JunkCleanView {
    // 扫描相关
    fun showScanning(progress: Int, currentPath: String)
    fun showScanCompleted(totalFiles: Int, totalSize: Long)
    fun showScanError(error: String)

    // 清理相关
    fun showCleaning(progress: Int, currentFile: String)
    fun showCleanCompleted(deletedCount: Int, deletedSize: Long)
    fun showCleanError(error: String)

    // UI更新
    fun updateCategories(categories: List<JunkCategory>)
    fun updateTotalSize(formattedSize: String)
    fun updateSelectedSize(formattedSize: String)
    fun enableCleanButton(enable: Boolean)

    // UI状态
    fun showProgressBar(show: Boolean, progress: Int = 0)
    fun updateScanningPath(path: String)
    fun showCleanButton(show: Boolean)
    fun updateBackgroundForJunk(hasJunk: Boolean)

    // 导航
    fun navigateToCleanSuccess(deletedSize: Long)
    fun showToast(message: String)
}