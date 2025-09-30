package com.fear.slanderous.talks.zp


import com.fear.slanderous.talks.zp.ImageGroup
import kotlinx.coroutines.*

/**
 * Presenter实现类 - 处理业务逻辑，连接View和Model
 */
class TssZpPresenter(
    private val model: TssZpContract.Model
) : TssZpContract.Presenter {

    private var view: TssZpContract.View? = null
    private val imageGroups = mutableListOf<ImageGroup>()
    private val presenterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /**
     * 绑定View
     */
    override fun attachView(view: TssZpContract.View) {
        this.view = view
    }

    /**
     * 解绑View
     */
    override fun detachView() {
        view = null
        presenterScope.cancel()
    }

    /**
     * 加载图片数据
     */
    override fun loadImages() {
        presenterScope.launch {
            view?.showLoadingDialog()
            delay(1500)
            view?.hideLoadingDialog()
            try {
                val images = withContext(Dispatchers.IO) {
                    model.getAllImages()
                }

                val groupedImages = withContext(Dispatchers.Default) {
                    model.groupImagesByDate(images)
                }

                imageGroups.clear()
                imageGroups.addAll(groupedImages)

                view?.apply {
                    showImages(imageGroups)
                    updateSelectedSize(model.calculateSelectedSize(imageGroups))
                    updateSelectAllIcon(model.isAllSelected(imageGroups))
                }
            } catch (e: Exception) {
                view?.apply {
                    showError("Failed to load images: ${e.message}")
                }
            }
        }
    }

    /**
     * 切换全选状态
     */
    override fun toggleSelectAll() {
        val allSelected = model.isAllSelected(imageGroups)

        imageGroups.forEach { group ->
            group.images.forEach { image ->
                image.isSelected = !allSelected
            }
        }

        view?.apply {
            showImages(imageGroups)
            updateSelectedSize(model.calculateSelectedSize(imageGroups))
            updateSelectAllIcon(!allSelected)
        }
    }

    /**
     * 处理单个图片选择状态改变
     */
    override fun onImageSelectionChanged() {
        view?.apply {
            updateSelectedSize(model.calculateSelectedSize(imageGroups))
            updateSelectAllIcon(model.isAllSelected(imageGroups))
        }
    }

    /**
     * 处理分组全选切换
     */
    override fun onGroupSelectAllToggled(groupIndex: Int) {
        if (groupIndex < 0 || groupIndex >= imageGroups.size) return

        val group = imageGroups[groupIndex]
        val allSelected = group.images.all { it.isSelected }

        group.images.forEach { image ->
            image.isSelected = !allSelected
        }

        view?.apply {
            showImages(imageGroups)
            updateSelectedSize(model.calculateSelectedSize(imageGroups))
            updateSelectAllIcon(model.isAllSelected(imageGroups))
        }
    }

    /**
     * 点击删除按钮
     */
    override fun onDeleteClick() {
        val selectedImages = imageGroups.flatMap { group ->
            group.images.filter { it.isSelected }
        }

        if (selectedImages.isEmpty()) {
            view?.showError("No images selected")
            return
        }

        view?.showDeleteConfirmDialog(selectedImages.size) {
            confirmDelete()
        }
    }

    /**
     * 确认删除
     */
    override fun confirmDelete() {
        val selectedImages = imageGroups.flatMap { group ->
            group.images.filter { it.isSelected }
        }

        if (selectedImages.isEmpty()) return

        presenterScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    model.deleteImages(selectedImages)
                }

                if (result.failedCount > 0) {
                    view?.showError("Failed to delete ${result.failedCount} images")
                }

                view?.navigateToResult(result.totalSize)

            } catch (e: Exception) {
                view?.showError("Delete failed: ${e.message}")
            }
        }
    }

    /**
     * 获取当前的图片分组数据
     */
    override fun getImageGroups(): List<ImageGroup> {
        return imageGroups.toList()
    }
}