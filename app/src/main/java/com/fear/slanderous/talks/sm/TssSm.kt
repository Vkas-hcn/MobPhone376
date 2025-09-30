package com.fear.slanderous.talks.sm

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.fear.slanderous.talks.R
import com.fear.slanderous.talks.databinding.TssSmBinding
import com.fear.slanderous.talks.js.TssJs

class TssSm : AppCompatActivity(), JunkCleanView {

    private lateinit var binding: TssSmBinding
    private lateinit var presenter: JunkCleanPresenter

    private val categoryAdapter by lazy {
        CategoryAdapter(
            onCategoryClick = { category ->
                presenter.toggleCategoryExpansion(category)
            },
            onCategorySelectClick = { category ->
                presenter.toggleCategorySelection(category)
            },
            onFileSelectClick = { file, category ->
                presenter.toggleFileSelection(file, category)
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setupViews()
        setupWindowInsets()

        // Initialize presenter
        presenter = JunkCleanPresenterImpl(JunkFileRepository(this))
        presenter.attachView(this)

        // Start scanning
        presenter.startScan()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.hide()
    }

    private fun setupViews() {
        binding = DataBindingUtil.setContentView(this, R.layout.tss_sm)

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(this@TssSm)
            adapter = categoryAdapter
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnCleanNow.setOnClickListener {
            presenter.startClean()
        }

        // Set initial UI state
        binding.apply {
            tvTitle.text = "Scanning"
            btnCleanNow.visibility = View.GONE
            progressScaning.visibility = View.GONE
        }
    }

    // ===== JunkCleanView Implementation =====

    override fun showScanning(progress: Int, currentPath: String) {
        binding.apply {
            progressScaning.visibility = View.VISIBLE
            progressScaning.progress = progress
            tvScanningPath.text = "Scanning: $currentPath"
            btnCleanNow.visibility = View.GONE
        }
    }

    override fun showScanCompleted(totalFiles: Int, totalSize: Long) {
        binding.apply {
            progressScaning.visibility = View.GONE
            tvScanningPath.text = "Scan completed - Found $totalFiles files"
            btnCleanNow.visibility = if (totalFiles > 0) View.VISIBLE else View.GONE
        }

        // Refresh the list after scan completion
        binding.rvCategories.postDelayed({
            categoryAdapter.notifyDataSetChanged()
        }, 100)
    }

    override fun showScanError(error: String) {
        binding.apply {
            progressScaning.visibility = View.GONE
            tvScanningPath.text = "Scan error: $error"
        }
        showToast("Scan failed: $error")
    }

    override fun showCleaning(progress: Int, currentFile: String) {
        binding.apply {
            tvScanningPath.text = "Cleaning: $currentFile"
            btnCleanNow.isEnabled = false
        }
    }

    override fun showCleanCompleted(deletedCount: Int, deletedSize: Long) {
        showToast("Cleaned $deletedCount files (${FileUtils.formatFileSize(deletedSize)})")
    }

    override fun showCleanError(error: String) {
        binding.btnCleanNow.isEnabled = true
        showToast("Clean failed: $error")
    }

    override fun updateCategories(categories: List<JunkCategory>) {
        categoryAdapter.submitList(categories.toList()) {
            categoryAdapter.notifyDataSetChanged()
        }
    }

    override fun updateTotalSize(formattedSize: String) {
        val parts = formattedSize.split(" ")
        if (parts.size == 2) {
            binding.tvScannedSize.text = parts[0]
            binding.tvScannedSizeUn.text = parts[1]
        } else {
            binding.tvScannedSize.text = formattedSize
            binding.tvScannedSizeUn.text = ""
        }
    }

    override fun updateSelectedSize(formattedSize: String) {
        // Can be used to show selected size if needed
    }

    override fun enableCleanButton(enable: Boolean) {
        binding.btnCleanNow.isEnabled = enable
    }

    override fun showProgressBar(show: Boolean, progress: Int) {
        binding.progressScaning.apply {
            visibility = if (show) View.VISIBLE else View.GONE
            this.progress = progress
        }
    }

    override fun updateScanningPath(path: String) {
        binding.tvScanningPath.text = path
    }

    override fun showCleanButton(show: Boolean) {
        binding.btnCleanNow.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun updateBackgroundForJunk(hasJunk: Boolean) {
        if (hasJunk) {
            binding.imgScanBg.setImageResource(R.drawable.have_junk)
            binding.viewBg.setBackgroundColor(Color.parseColor("#FF8E43"))
        } else {
            binding.imgScanBg.setImageResource(R.drawable.no_junk)
            binding.viewBg.setBackgroundColor(Color.parseColor("#066BFA"))
        }
    }

    override fun navigateToCleanSuccess(deletedSize: Long) {
        val intent = Intent(this, TssJs::class.java).apply {
            putExtra("CLEANED_SIZE", deletedSize)
            putExtra("jump_type", "junk")
        }
        startActivity(intent)
        finish()
    }

    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}