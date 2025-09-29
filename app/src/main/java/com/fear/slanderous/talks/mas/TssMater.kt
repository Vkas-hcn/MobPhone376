package com.fear.slanderous.talks.mas

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.fear.slanderous.talks.R
import com.fear.slanderous.talks.databinding.TssMasterBinding

class TssMater : AppCompatActivity(), PermissionCallback {

    private lateinit var binding: TssMasterBinding

    private val permissionDelegate = PermissionDelegate(this, this)

    private val permissionManager by permissionDelegate
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            StorageInfoManager(this),
            NavigationManager(this)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionManager.hasStoragePermission() // This triggers the delegate
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        setupUI()
        setupObservers()
        setupClickListeners()

        viewModel.updateStorageInfo()
    }

    override fun onResume() {
        super.onResume()
        if (permissionManager.hasStoragePermission()) {
            binding.tssQuan.miss.isVisible = false
        }
        viewModel.updateStorageInfo()
    }

    private fun setupUI() {
        binding = DataBindingUtil.setContentView(this, R.layout.tss_master)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel // 绑定ViewModel到布局

        supportActionBar?.hide()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        setStatusBarHeightPadding()
    }
    private fun setStatusBarHeightPadding() {
        binding.titleBar.viewTreeObserver.addOnGlobalLayoutListener {
            val statusBarHeight = getStatusBarHeight()
            binding.titleBar.setPadding(0, statusBarHeight, 0, 0)
        }
    }

    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            val scale = resources.displayMetrics.density
            (24 * scale + 0.5f).toInt()
        }
    }
    private fun setupObservers() {
        viewModel.storageInfo.observe(this) { storageInfo ->
            binding.storageInfo = storageInfo
        }

        viewModel.onPermissionRequired = {
            showPermissionDialog()
        }
    }

    private fun setupClickListeners() {
        binding.flClean.setOnClickListener {
            viewModel.requestNavigation(
                NavigationAction.JunkClean,
                permissionManager.hasStoragePermission()
            )
        }

        binding.llImage.setOnClickListener {
            showScanDialogAndNavigate(NavigationAction.ImageClean)
        }

        binding.llFile.setOnClickListener {
            showScanDialogAndNavigate(NavigationAction.FileClean)
        }

        binding.settingsIcon.setOnClickListener {
            viewModel.requestNavigation(
                NavigationAction.Settings,
                true
            )
        }

        binding.tssQuan.miss.setOnClickListener {
        }

        binding.tssQuan.tvCancel.setOnClickListener {
            binding.tssQuan.miss.isVisible = false
            permissionManager.showPermissionDeniedDialog()
        }

        binding.tssQuan.tvYes.setOnClickListener {
            binding.tssQuan.miss.isVisible = false
            permissionManager.requestStoragePermission()
        }
    }
    private fun showScanDialogAndNavigate(action: NavigationAction) {
        if (!permissionManager.hasStoragePermission()) {
            showPermissionDialog()
            return
        }
        viewModel.requestNavigation(action, true)
    }
    private fun showPermissionDialog() {
        binding.tssQuan.miss.isVisible = true
    }

    override fun onPermissionGranted() {
        binding.tssQuan.miss.isVisible = false
        viewModel.onPermissionGranted()
    }

    override fun onPermissionDenied(shouldShowRationale: Boolean) {
        if (shouldShowRationale) {
            showPermissionDialog()
        } else {
            permissionManager.showPermissionDeniedDialog()
        }
    }
}