package com.fear.slanderous.talks.zp


import com.fear.slanderous.talks.zp.ImageGroup
import com.fear.slanderous.talks.zp.ImageItem

/**
 * MVP Contract - 定义View和Presenter的接口契约
 */
interface TssZpContract {

    /**
     * View接口 - 定义UI相关的操作
     */
    interface View {
        // 显示/隐藏加载动画
        fun showLoadingDialog()
        fun hideLoadingDialog()

        // 更新图片列表
        fun showImages(imageGroups: List<ImageGroup>)

        // 更新选中的图片大小
        fun updateSelectedSize(size: Long)

        // 更新全选按钮状态
        fun updateSelectAllIcon(allSelected: Boolean)

        // 显示删除确认对话框
        fun showDeleteConfirmDialog(selectedCount: Int, onConfirm: () -> Unit)

        // 显示错误信息
        fun showError(message: String)

        // 删除成功后跳转
        fun navigateToResult(cleanedSize: Long)

        // 结束页面
        fun finishActivity()
    }

    /**
     * Presenter接口 - 定义业务逻辑操作
     */
    interface Presenter {
        // 生命周期
        fun attachView(view: View)
        fun detachView()

        // 加载图片
        fun loadImages()

        // 选择操作
        fun toggleSelectAll()
        fun onImageSelectionChanged()
        fun onGroupSelectAllToggled(groupIndex: Int)

        // 删除操作
        fun onDeleteClick()
        fun confirmDelete()

        // 获取数据
        fun getImageGroups(): List<ImageGroup>
    }

    /**
     * Model接口 - 定义数据操作
     */
    interface Model {
        // 获取所有图片
        fun getAllImages(): List<ImageItem>

        // 按日期分组
        fun groupImagesByDate(images: List<ImageItem>): List<ImageGroup>

        // 删除图片
        fun deleteImages(images: List<ImageItem>): DeleteResult

        // 计算选中图片的总大小
        fun calculateSelectedSize(imageGroups: List<ImageGroup>): Long

        // 检查是否全选
        fun isAllSelected(imageGroups: List<ImageGroup>): Boolean
    }

    /**
     * 删除结果
     */
    data class DeleteResult(
        val successCount: Int,
        val failedCount: Int,
        val totalSize: Long
    )
}