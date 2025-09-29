package com.fear.slanderous.talks.zp


import android.content.ContentResolver
import android.provider.MediaStore
import android.util.Log
import com.fear.slanderous.talks.zp.ImageGroup
import com.fear.slanderous.talks.zp.ImageItem
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Model实现类 - 处理所有数据相关的操作
 */
class TssZpModel(private val contentResolver: ContentResolver) : TssZpContract.Model {

    companion object {
        private const val TAG = "TssZpModel"
    }

    /**
     * 获取设备上的所有图片
     */
    override fun getAllImages(): List<ImageItem> {
        val images = mutableListOf<ImageItem>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_TAKEN
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        try {
            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val path = cursor.getString(pathColumn)
                    val size = cursor.getLong(sizeColumn)
                    val dateTaken = cursor.getLong(dateColumn)

                    // 只添加存在的文件
                    if (File(path).exists()) {
                        images.add(ImageItem(id, name, path, size, dateTaken))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading images", e)
        }

        return images
    }

    /**
     * 按日期对图片进行分组
     */
    override fun groupImagesByDate(images: List<ImageItem>): List<ImageGroup> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val groupedMap = images.groupBy { image ->
            val date = if (image.dateTaken > 0) {
                Date(image.dateTaken)
            } else {
                Date(File(image.path).lastModified())
            }
            dateFormat.format(date)
        }

        return groupedMap.map { (date, imageList) ->
            ImageGroup(date, imageList.toMutableList())
        }.sortedByDescending { it.date }
    }

    /**
     * 删除选中的图片
     */
    override fun deleteImages(images: List<ImageItem>): TssZpContract.DeleteResult {
        var successCount = 0
        var failedCount = 0
        val totalSize = images.sumOf { it.size }

        images.forEach { image ->
            try {
                val file = File(image.path)
                if (file.exists() && file.delete()) {
                    // 从MediaStore中删除记录
                    val deletedRows = contentResolver.delete(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        "${MediaStore.Images.Media._ID} = ?",
                        arrayOf(image.id.toString())
                    )

                    if (deletedRows > 0) {
                        successCount++
                    } else {
                        failedCount++
                        Log.w(TAG, "File deleted but MediaStore record not found: ${image.path}")
                    }
                } else {
                    failedCount++
                    Log.e(TAG, "Failed to delete file: ${image.path}")
                }
            } catch (e: Exception) {
                failedCount++
                Log.e(TAG, "Error deleting image: ${image.path}", e)
            }
        }

        return TssZpContract.DeleteResult(
            successCount = successCount,
            failedCount = failedCount,
            totalSize = totalSize
        )
    }

    /**
     * 计算选中图片的总大小
     */
    override fun calculateSelectedSize(imageGroups: List<ImageGroup>): Long {
        return imageGroups.sumOf { group ->
            group.images.filter { it.isSelected }.sumOf { it.size }
        }
    }

    /**
     * 检查是否所有图片都被选中
     */
    override fun isAllSelected(imageGroups: List<ImageGroup>): Boolean {
        if (imageGroups.isEmpty()) return false

        return imageGroups.all { group ->
            group.images.isNotEmpty() && group.images.all { it.isSelected }
        }
    }
}