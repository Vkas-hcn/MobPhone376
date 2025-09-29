package com.fear.slanderous.talks.mas

import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import kotlin.math.max

/**
 * 存储信息数据类
 */
data class StorageInfo(
    val freeStorage: String,
    val usedStorage: String,
    val usedPercentage: Int,
    val status: String
)

/**
 * 存储信息回调接口
 */
interface StorageInfoCallback {
    fun onStorageInfoUpdated(storageInfo: StorageInfo)
    fun onStorageInfoError(error: String)
}

/**
 * 存储信息管理器
 * MVP模式下移除LiveData，使用回调模式
 */
class StorageInfoManager(private val context: Context) {

    /**
     * 异步更新存储信息
     * @param callback 回调接口
     */
    fun updateStorageInfo(callback: StorageInfoCallback? = null) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val storageInfo = calculateStorageInfo()
                withContext(Dispatchers.Main) {
                    callback?.onStorageInfoUpdated(storageInfo)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    val errorInfo = StorageInfo(
                        freeStorage = "-- GB",
                        usedStorage = "-- GB",
                        usedPercentage = 0,
                        status = "Unknown"
                    )
                    callback?.onStorageInfoUpdated(errorInfo)
                    callback?.onStorageInfoError("Failed to get storage information")
                }
            }
        }
    }

    /**
     * 计算存储信息
     */
    private fun calculateStorageInfo(): StorageInfo {
        val internalStat = StatFs(Environment.getDataDirectory().path)

        val blockSize = internalStat.blockSizeLong
        val totalBlocks = internalStat.blockCountLong
        val availableBlocks = internalStat.availableBlocksLong

        val totalUserBytes = totalBlocks * blockSize
        val availableBytes = availableBlocks * blockSize
        val usedBytes = totalUserBytes - availableBytes

        val actualTotalBytes = getTotalDeviceStorageAccurate()
        val displayTotalBytes = max(actualTotalBytes, totalUserBytes)
        val displayFreeBytes = availableBytes
        val displayUsedBytes = displayTotalBytes - displayFreeBytes

        val usedPercentage = if (displayTotalBytes > 0) {
            ((displayUsedBytes.toDouble() / displayTotalBytes.toDouble()) * 100).toInt()
        } else {
            0
        }

        val freeStorageFormatted = formatStorageSize(displayFreeBytes)
        val usedStorageFormatted = formatStorageSize(displayUsedBytes)

        val status = when {
            usedPercentage < 50 -> "Excellent"
            usedPercentage < 80 -> "Good"
            usedPercentage < 95 -> "Warning"
            else -> "Critical"
        }

        return StorageInfo(
            freeStorage = freeStorageFormatted.first,
            usedStorage = usedStorageFormatted.first,
            usedPercentage = usedPercentage,
            status = status
        )
    }

    /**
     * 获取设备总存储空间（精确计算）
     */
    private fun getTotalDeviceStorageAccurate(): Long {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val storageStatsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
                return storageStatsManager.getTotalBytes(StorageManager.UUID_DEFAULT)
            }

            val internalStat = StatFs(Environment.getDataDirectory().path)
            val internalTotal = internalStat.blockCountLong * internalStat.blockSizeLong

            val storagePaths = arrayOf(
                Environment.getRootDirectory().absolutePath,
                Environment.getDataDirectory().absolutePath,
                Environment.getDownloadCacheDirectory().absolutePath
            )

            var total: Long = 0
            for (path in storagePaths) {
                val stat = StatFs(path)
                val blockSize = stat.blockSizeLong
                val blockCount = stat.blockCountLong
                total += blockSize * blockCount
            }

            val withSystemOverhead = total + (total * 0.07).toLong()
            max(internalTotal, withSystemOverhead)
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val internalStat = StatFs(Environment.getDataDirectory().path)
                val internalTotal = internalStat.blockCountLong * internalStat.blockSizeLong
                internalTotal + (internalTotal * 0.12).toLong()
            } catch (innerException: Exception) {
                innerException.printStackTrace()
                0L
            }
        }
    }

    /**
     * 格式化存储大小
     */
    private fun formatStorageSize(bytes: Long): Pair<String, String> {
        return when {
            bytes >= 1000L * 1000L * 1000L -> {
                val gb = bytes.toDouble() / (1000L * 1000L * 1000L)
                val formatted = if (gb >= 10.0) {
                    DecimalFormat("#").format(gb)
                } else {
                    DecimalFormat("#.#").format(gb)
                }
                Pair("$formatted GB", "GB")
            }

            bytes >= 1000L * 1000L -> {
                val mb = bytes.toDouble() / (1000L * 1000L)
                val formatted = DecimalFormat("#").format(mb)
                Pair("$formatted MB", "MB")
            }

            bytes >= 1000L -> {
                val kb = bytes.toDouble() / 1000L
                val formatted = DecimalFormat("#").format(kb)
                Pair("$formatted KB", "KB")
            }

            else -> {
                Pair("$bytes B", "B")
            }
        }
    }
}