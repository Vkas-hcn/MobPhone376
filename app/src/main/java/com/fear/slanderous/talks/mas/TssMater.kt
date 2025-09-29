package com.fear.slanderous.talks.mas

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.fear.slanderous.talks.R
import com.fear.slanderous.talks.dwj.TssDwj
import com.fear.slanderous.talks.ohs.fx.TssFx
import com.fear.slanderous.talks.sm.TssSm
import com.fear.slanderous.talks.zp.TssZp
import com.google.android.material.progressindicator.CircularProgressIndicator

/**
 * 主Activity，实现MVP的View接口
 * 负责UI展示和用户交互
 */
class TssMater : AppCompatActivity(), MainContract.View, PermissionCallback {

    // UI组件
    private lateinit var mainLayout: LinearLayout
    private lateinit var titleBar: View
    private lateinit var settingsIcon: ImageView
    private lateinit var progressCircle: CircularProgressIndicator
    private lateinit var progressText: TextView
    private lateinit var freeStorageText: TextView
    private lateinit var usedStorageText: TextView
    private lateinit var cleanButton: FrameLayout
    private lateinit var imageCleanLayout: LinearLayout
    private lateinit var fileCleanLayout: LinearLayout

    // 权限对话框
    private lateinit var permissionDialogLayout: View
    private lateinit var permissionCancelButton: View
    private lateinit var permissionConfirmButton: View

    // MVP组件
    private lateinit var presenter: MainContract.Presenter
    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置窗口标志
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setContentView(R.layout.tss_master)

        // 初始化组件
        initializeComponents()

        // 设置UI
        setupUI()

        // 设置点击监听器
        setupClickListeners()

        // 初始更新存储信息
        presenter.updateStorageInfo()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    /**
     * 初始化组件
     */
    private fun initializeComponents() {
        val permissionDelegate = PermissionDelegate(this, this)
        permissionManager = permissionDelegate.getValue(this, ::permissionManager)

        val storageInfoManager = StorageInfoManager(this)
        presenter = MainPresenter(storageInfoManager, permissionManager)
        presenter.attachView(this)

        // 查找视图
        findViews()
    }

    /**
     * 查找所有视图
     */
    private fun findViews() {
        mainLayout = findViewById(R.id.main)
        titleBar = findViewById(R.id.title_bar)
        settingsIcon = findViewById(R.id.settings_icon)
        progressCircle = findViewById(R.id.progress_circle)
        progressText = findViewById(R.id.progress_text)
        freeStorageText = findViewById(R.id.free_storage)
        usedStorageText = findViewById(R.id.used_storage)
        cleanButton = findViewById(R.id.fl_clean)
        imageCleanLayout = findViewById(R.id.ll_image)
        fileCleanLayout = findViewById(R.id.ll_file)

        // 权限对话框视图
        permissionDialogLayout = findViewById(R.id.miss)
        permissionCancelButton = findViewById(R.id.tv_cancel)
        permissionConfirmButton =findViewById(R.id.tv_yes)
    }

    /**
     * 设置UI
     */
    private fun setupUI() {
        // 隐藏ActionBar
        supportActionBar?.hide()

        // 设置系统栏
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        // 设置状态栏透明
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        // 设置状态栏高度padding
        setStatusBarHeightPadding()
    }

    /**
     * 设置状态栏高度padding
     */
    private fun setStatusBarHeightPadding() {
        titleBar.viewTreeObserver.addOnGlobalLayoutListener {
            val statusBarHeight = getStatusBarHeight()
            titleBar.setPadding(0, statusBarHeight, 0, 0)
        }
    }

    /**
     * 获取状态栏高度
     */
    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            val scale = resources.displayMetrics.density
            (24 * scale + 0.5f).toInt()
        }
    }

    /**
     * 设置点击监听器
     */
    private fun setupClickListeners() {
        cleanButton.setOnClickListener {
            presenter.onCleanButtonClick()
        }

        imageCleanLayout.setOnClickListener {
            presenter.onImageCleanClick()
        }

        fileCleanLayout.setOnClickListener {
            presenter.onFileCleanClick()
        }

        settingsIcon.setOnClickListener {
            presenter.onSettingsClick()
        }

        permissionCancelButton.setOnClickListener {
            presenter.onPermissionDialogCancel()
        }

        permissionConfirmButton.setOnClickListener {
            presenter.onPermissionDialogConfirm()
        }
    }

    // ========== MainContract.View 接口实现 ==========

    override fun updateStorageInfo(
        freeStorage: String,
        usedStorage: String,
        usedPercentage: Int,
        status: String
    ) {
        runOnUiThread {
            progressCircle.progress = usedPercentage
            progressText.text = usedPercentage.toString()
            freeStorageText.text = freeStorage
            usedStorageText.text = usedStorage
        }
    }

    override fun showPermissionDialog() {
        runOnUiThread {
            if (::permissionDialogLayout.isInitialized) {
                permissionDialogLayout.isVisible = true
            }
        }
    }

    override fun hidePermissionDialog() {
        runOnUiThread {
            if (::permissionDialogLayout.isInitialized) {
                permissionDialogLayout.isVisible = false
            }
        }
    }

    override fun showPermissionDeniedDialog() {
        runOnUiThread {
            permissionManager.showPermissionDeniedDialog()
        }
    }

    override fun navigateToPage(action: NavigationAction) {
        runOnUiThread {
            val intent = when (action) {
                is NavigationAction.JunkClean -> Intent(this, TssSm::class.java)
                is NavigationAction.ImageClean -> Intent(this, TssZp::class.java)
                is NavigationAction.FileClean -> Intent(this, TssDwj::class.java)
                is NavigationAction.Settings -> Intent(this, TssFx::class.java)
            }
            startActivity(intent)
        }
    }

    override fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun requestStoragePermission() {
        permissionManager.requestStoragePermission()
    }

    override fun openAppSettings() {
        // 由PermissionManager处理
    }

    // ========== PermissionCallback 接口实现 ==========

    override fun onPermissionGranted() {
        presenter.onPermissionGranted()
    }

    override fun onPermissionDenied(shouldShowRationale: Boolean) {
        presenter.onPermissionDenied(shouldShowRationale)
    }
}