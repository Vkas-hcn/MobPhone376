package com.fear.slanderous.talks.sm

import com.fear.slanderous.talks.R


enum class JunkFileTypeMy(val displayName: String) {
    APP_CACHE("App Cache", ),
    APK_FILES("Apk Files"),
    LOG_FILES("Log Files"),
    TEMP_FILES("Temp Files"),
    OTHER("Other")
}


data class JunkFile(
    val name: String,
    val path: String,
    val size: Long,
    val type: JunkFileTypeMy,
    var isSelected: Boolean = false
) {
    val formattedSize: String by lazy {
        FileUtils.formatFileSize(size)
    }
}

data class JunkCategory(
    val type: JunkFileTypeMy,
    val files: MutableList<JunkFile> = mutableListOf(),
    var isExpanded: Boolean = false,
    var isSelected: Boolean = false
) {
    val name: String get() = type.displayName

    val totalSize: Long
        get() = files.sumOf { it.size }

    val selectedSize: Long
        get() = files.filter { it.isSelected }.sumOf { it.size }

    val formattedTotalSize: String
        get() = FileUtils.formatFileSize(totalSize)

    val hasSelectedFiles: Boolean
        get() = files.any { it.isSelected }
}