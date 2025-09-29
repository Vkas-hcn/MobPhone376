package com.fear.slanderous.talks.mas

/**
 * MVP契约接口，定义View和Presenter的交互接口
 */
interface MainContract {

    /**
     * View接口，定义所有UI更新方法
     */
    interface View {
        // 更新存储信息显示
        fun updateStorageInfo(
            freeStorage: String,
            usedStorage: String,
            usedPercentage: Int,
            status: String
        )

        // 显示权限请求对话框
        fun showPermissionDialog()

        // 隐藏权限请求对话框
        fun hidePermissionDialog()

        // 显示权限被拒绝的对话框
        fun showPermissionDeniedDialog()

        // 导航到指定页面
        fun navigateToPage(action: NavigationAction)

        // 显示错误信息
        fun showError(message: String)

        // 请求存储权限
        fun requestStoragePermission()

        // 打开应用设置页面
        fun openAppSettings()
    }

    /**
     * Presenter接口，定义所有业务逻辑方法
     */
    interface Presenter {
        // 绑定View
        fun attachView(view: View)

        // 解绑View
        fun detachView()

        // 更新存储信息
        fun updateStorageInfo()

        // 处理清理按钮点击
        fun onCleanButtonClick()

        // 处理图片清理点击
        fun onImageCleanClick()

        // 处理文件清理点击
        fun onFileCleanClick()

        // 处理设置按钮点击
        fun onSettingsClick()

        // 处理权限授予
        fun onPermissionGranted()

        // 处理权限拒绝
        fun onPermissionDenied(shouldShowRationale: Boolean)

        // 检查是否有存储权限
        fun hasStoragePermission(): Boolean

        // 处理权限对话框确认
        fun onPermissionDialogConfirm()

        // 处理权限对话框取消
        fun onPermissionDialogCancel()

        // Activity恢复时调用
        fun onResume()
    }
}