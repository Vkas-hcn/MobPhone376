package com.fear.slanderous.talks.mas

import android.content.Context
import java.lang.ref.WeakReference

/**
 * MainPresenter实现类，负责处理所有业务逻辑
 */
class MainPresenter(
    private val storageInfoManager: StorageInfoManager,
    private val permissionManager: PermissionManager
) : MainContract.Presenter, StorageInfoCallback {

    // 使用弱引用持有View，避免内存泄漏
    private var viewRef: WeakReference<MainContract.View>? = null
    private val view: MainContract.View?
        get() = viewRef?.get()

    // 待处理的导航动作
    private var pendingNavigationAction: NavigationAction? = null

    override fun attachView(view: MainContract.View) {
        this.viewRef = WeakReference(view)
    }

    override fun detachView() {
        viewRef?.clear()
        viewRef = null
    }

    override fun updateStorageInfo() {
        storageInfoManager.updateStorageInfo(this)
    }

    override fun onCleanButtonClick() {
        requestNavigation(NavigationAction.JunkClean)
    }

    override fun onImageCleanClick() {

        requestNavigation(NavigationAction.ImageClean)
    }

    override fun onFileCleanClick() {
        requestNavigation(NavigationAction.FileClean)
    }

    override fun onSettingsClick() {
        // 设置页面不需要权限检查，直接导航
        view?.navigateToPage(NavigationAction.Settings)
    }

    override fun onPermissionGranted() {
        view?.hidePermissionDialog()

        // 如果有待处理的导航动作，执行它
        pendingNavigationAction?.let { action ->
            view?.navigateToPage(action)
            pendingNavigationAction = null
        }

        // 权限授予后更新存储信息
        updateStorageInfo()
    }

    override fun onPermissionDenied(shouldShowRationale: Boolean) {
        if (shouldShowRationale) {
            view?.showPermissionDialog()
        } else {
            view?.showPermissionDeniedDialog()
        }
    }

    override fun hasStoragePermission(): Boolean {
        return permissionManager.hasStoragePermission()
    }

    override fun onPermissionDialogConfirm() {
        view?.hidePermissionDialog()
        view?.requestStoragePermission()
    }

    override fun onPermissionDialogCancel() {
        view?.hidePermissionDialog()
        view?.showPermissionDeniedDialog()
    }

    override fun onResume() {
        if (hasStoragePermission()) {
            view?.hidePermissionDialog()
        }
        updateStorageInfo()
    }

    /**
     * 请求导航到指定页面
     * @param action 导航动作
     */
    private fun requestNavigation(action: NavigationAction) {
        if (hasStoragePermission()) {
            view?.navigateToPage(action)
        } else {
            pendingNavigationAction = action
            view?.showPermissionDialog()
        }
    }

    // StorageInfoCallback 实现
    override fun onStorageInfoUpdated(storageInfo: StorageInfo) {
        view?.updateStorageInfo(
            freeStorage = storageInfo.freeStorage,
            usedStorage = storageInfo.usedStorage,
            usedPercentage = storageInfo.usedPercentage,
            status = storageInfo.status
        )
    }

    override fun onStorageInfoError(error: String) {
        // 发生错误时显示默认值
        view?.updateStorageInfo(
            freeStorage = "-- GB",
            usedStorage = "-- GB",
            usedPercentage = 0,
            status = "Unknown"
        )
        view?.showError("Failed to get storage information")
    }
}