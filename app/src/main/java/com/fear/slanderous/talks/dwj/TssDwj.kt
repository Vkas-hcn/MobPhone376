package com.fear.slanderous.talks.dwj

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.fear.slanderous.talks.R
import com.fear.slanderous.talks.databinding.TssWjBinding
import com.google.android.material.snackbar.Snackbar
import com.fear.slanderous.talks.js.TssJs

class TssDwj : AppCompatActivity(), FileView, FileAdapterListener {
    private val binding by lazy { TssWjBinding.inflate(layoutInflater) }
    private lateinit var adapter: OptimizedFileAdapter
    private lateinit var presenter: FilePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // 初始化 Presenter
        presenter = FilePresenterImpl(
            scanFilesUseCase = ScanFilesUseCase(FileRepositoryImpl(this)),
            deleteFilesUseCase = DeleteFilesUseCase(FileRepositoryImpl(this)),
            filterFilesUseCase = FilterFilesUseCase()
        )
        presenter.attachView(this)

        setupViews()
        presenter.startScanning()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    private fun setupViews() {
        binding.textBack.setOnClickListener { finish() }
        binding.textBack.setOnLongClickListener {
            presenter.onBackLongClicked()
            true
        }

        adapter = OptimizedFileAdapter(this)
        binding.rvFiles.apply {
            layoutManager = LinearLayoutManager(this@TssDwj)
            adapter = this@TssDwj.adapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        binding.tvType.setOnClickListener { presenter.onFilterTypeClicked() }
        binding.tvSize.setOnClickListener { presenter.onFilterSizeClicked() }
        binding.tvTime.setOnClickListener { presenter.onFilterTimeClicked() }
        binding.btnDelete.setOnClickListener { presenter.onDeleteButtonClicked() }
    }

    // FileView 接口实现
    override fun showLoading() {
        // 加载中状态
    }

    override fun hideLoading() {
        // 隐藏加载状态
    }

    override fun showFiles(files: List<FileItem>) {
        adapter.submitList(files) {
            if (files.isNotEmpty()) {
                binding.rvFiles.scrollToPosition(0)
            }
        }
        binding.rvFiles.visibility = if (files.isEmpty()) View.INVISIBLE else View.VISIBLE
    }

    override fun showNoFiles() {
        binding.tvNoFiles.visibility = View.VISIBLE
    }

    override fun hideNoFiles() {
        binding.tvNoFiles.visibility = View.GONE
    }

    override fun updateDeleteButton(enabled: Boolean, count: Int) {
        binding.btnDelete.isEnabled = enabled
        binding.btnDelete.text = if (count > 0) "Delete ($count)" else "Delete"
    }

    override fun updateFilters(type: String, size: String, time: String) {
        binding.tvType.text = type
        binding.tvSize.text = size
        binding.tvTime.text = time
    }

    override fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun showFilterDialog(filterType: String, options: Array<String>) {
        AlertDialog.Builder(this)
            .setTitle("Select ${filterType.removeSuffix("s")}")
            .setItems(options) { _, which ->
                presenter.onFilterOptionSelected(filterType, options[which])
            }
            .show()
    }

    override fun showDeleteConfirmDialog(count: Int) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete $count file(s)?")
            .setPositiveButton("Delete") { _, _ ->
                presenter.onDeleteConfirmed()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun showSelectionOptionsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Selection Options")
            .setItems(arrayOf("Select All", "Clear All")) { _, which ->
                presenter.onSelectionOptionSelected(which)
            }
            .show()
    }

    override fun showCleaningDialog() {
        binding.dialogType.root.setOnClickListener { }
        binding.dialogType.tvBack.setOnClickListener { finish() }
        binding.dialogType.imgLogo.setImageResource(R.drawable.dwj_icon_main)
        binding.dialogType.conClean.visibility = View.VISIBLE
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

    override fun hideCleaningDialog() {
        binding.dialogType.conClean.visibility = View.GONE
        binding.dialogType.imgBg1.clearAnimation()
    }

    override fun navigateToFinishActivity(result: DeleteResult) {
        val intent = Intent(this, TssJs::class.java)
        intent.putExtra("CLEANED_SIZE", result.totalSize)
        intent.putExtra("jump_type", "file")
        startActivity(intent)
        finish()
    }

    // FileAdapterListener 接口实现
    override fun onItemClick(position: Int) {
        presenter.toggleFileSelection(position)
    }

    override fun onSelectionChanged(selectedCount: Int) {
        // Presenter 内部已处理
    }
}
