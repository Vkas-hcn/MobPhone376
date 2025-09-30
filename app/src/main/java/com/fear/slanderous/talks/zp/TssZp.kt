package com.fear.slanderous.talks.zp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fear.slanderous.talks.R
import com.fear.slanderous.talks.databinding.TssZpBinding
import com.fear.slanderous.talks.js.TssJs

/**
 * 重构后的Activity - 实现MVP的View层
 */
class TssZp : AppCompatActivity(), TssZpContract.View {

    private val binding by lazy { TssZpBinding.inflate(layoutInflater) }
    private lateinit var presenter: TssZpContract.Presenter
    private lateinit var imageGroupAdapter: ImageGroupAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupMVP()
        setupBackPress()
        initViews()

        // 通过Presenter加载数据
        presenter.loadImages()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    /**
     * 初始化MVP组件
     */
    private fun setupMVP() {
        val model = TssZpModel(contentResolver)
        presenter = TssZpPresenter(model)
        presenter.attachView(this)
    }

    /**
     * 设置返回键处理
     */
    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback {
            finishActivity()
        }
    }

    /**
     * 初始化视图
     */
    private fun initViews() {
        // 返回按钮
        binding.btnBack.setOnClickListener {
            finishActivity()
        }

        // 全选按钮
        binding.cbSelectAllGlobal.setOnClickListener {
            presenter.toggleSelectAll()
        }

        // 删除按钮
        binding.btnCleanNow.setOnClickListener {
            presenter.onDeleteClick()
        }

        // 初始化RecyclerView
        imageGroupAdapter = ImageGroupAdapter(
            imageGroups = emptyList(),
            onSelectionChanged = { presenter.onImageSelectionChanged() },
            onGroupSelectAll = { groupIndex ->
                presenter.onGroupSelectAllToggled(groupIndex)
            }
        )

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(this@TssZp)
            adapter = imageGroupAdapter
        }
    }

    // ========== View接口实现 ==========

    /**
     * 显示加载动画
     */
    override fun showLoadingDialog() {
        binding.dialogType.root.visibility = View.VISIBLE
        binding.dialogType.root.setOnClickListener { }
        binding.dialogType.tvBack.setOnClickListener { finishActivity() }
        binding.dialogType.imgLogo.setImageResource(R.drawable.pic_icon)
        binding.dialogType.conClean.visibility = View.VISIBLE

        // 启动旋转动画
        val rotateAnimation = android.view.animation.RotateAnimation(
            0f, 360f,
            android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
            android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
        )
        rotateAnimation.duration = 1500
        rotateAnimation.repeatCount = android.view.animation.Animation.INFINITE
        rotateAnimation.interpolator = android.view.animation.LinearInterpolator()

        binding.dialogType.imgBg1.startAnimation(rotateAnimation)
    }

    /**
     * 隐藏加载动画
     */
    override fun hideLoadingDialog() {


        binding.dialogType.conClean.visibility = View.GONE
        binding.dialogType.imgBg1.clearAnimation()
    }

    /**
     * 显示图片列表
     */
    override fun showImages(imageGroups: List<ImageGroup>) {
        imageGroupAdapter.updateData(imageGroups)
    }

    /**
     * 更新选中图片的大小显示
     */
    override fun updateSelectedSize(size: Long) {
        val (sizeValue, unit) = formatFileSize(size)
        binding.tvScannedSize.text = sizeValue
        binding.tvScannedSizeUn.text = unit
    }

    /**
     * 更新全选按钮图标
     */
    override fun updateSelectAllIcon(allSelected: Boolean) {
        binding.cbSelectAllGlobal.setImageResource(
            if (allSelected) R.drawable.check_yuan_2 else R.drawable.discheck_yuan
        )
    }

    /**
     * 显示删除确认对话框
     */
    override fun showDeleteConfirmDialog(selectedCount: Int, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Delete confirmation")
            .setMessage("Are you sure you want to delete the selected $selectedCount images?")
            .setPositiveButton("Delete") { _, _ ->
                onConfirm()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * 显示错误信息
     */
    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * 删除成功后跳转到结果页
     */
    override fun navigateToResult(cleanedSize: Long) {
        val intent = Intent(this, TssJs::class.java).apply {
            putExtra("CLEANED_SIZE", cleanedSize)
            putExtra("jump_type", "image")
        }
        startActivity(intent)
        finish()
    }

    /**
     * 结束Activity
     */
    override fun finishActivity() {
        finish()
    }

    // ========== 工具方法 ==========

    /**
     * 格式化文件大小
     */
    private fun formatFileSize(bytes: Long): Pair<String, String> {
        return when {
            bytes < 1024 -> Pair(bytes.toString(), "B")
            bytes < 1024 * 1024 -> Pair(String.format("%.1f", bytes / 1024.0), "KB")
            bytes < 1024 * 1024 * 1024 -> Pair(String.format("%.1f", bytes / (1024.0 * 1024.0)), "MB")
            else -> Pair(String.format("%.1f", bytes / (1024.0 * 1024.0 * 1024.0)), "GB")
        }
    }
}

// 数据类保持不变
data class ImageItem(
    val id: Long,
    val name: String,
    val path: String,
    val size: Long,
    val dateTaken: Long,
    var isSelected: Boolean = false
)

data class ImageGroup(
    val date: String,
    val images: MutableList<ImageItem>
)