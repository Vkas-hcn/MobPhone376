package com.fear.slanderous.talks.zp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.fear.slanderous.talks.R
import com.fear.slanderous.talks.databinding.TssZpBinding
import com.fear.slanderous.talks.js.TssJs
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class TssZp : AppCompatActivity() {
    private val binding by lazy { TssZpBinding.inflate(layoutInflater) }
    private lateinit var imageGroupAdapter: ImageGroupAdapter
    private val imageGroups = mutableListOf<ImageGroup>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback {
            finish()
        }
        showCleaningDialog()
        initViews()
        loadImages()
    }
    private fun showCleaningDialog() {
        binding.dialogType.root.setOnClickListener {  }
        binding.dialogType.tvBack.setOnClickListener { finish() }
        binding.dialogType.imgLogo.setImageResource( R.drawable.pic_icon)
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

        binding.dialogType.conClean.postDelayed({
            binding.dialogType.conClean.visibility = View.GONE
            binding.dialogType.imgBg1.clearAnimation()
        }, 1500)
    }

    private fun initViews() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.cbSelectAllGlobal.setOnClickListener {
            toggleSelectAll()
        }

        binding.btnCleanNow.setOnClickListener {
            showDeleteConfirmDialog()
        }

        imageGroupAdapter = ImageGroupAdapter(imageGroups) { updateSelectedSize() }
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(this@TssZp)
            adapter = imageGroupAdapter
        }
    }

    private fun loadImages() {
        val images = getAllImages()
        val groupedImages = groupImagesByDate(images)

        imageGroups.clear()
        imageGroups.addAll(groupedImages)
        imageGroupAdapter.notifyDataSetChanged()

        updateSelectedSize()
    }

    private fun getAllImages(): List<ImageItem> {
        val images = mutableListOf<ImageItem>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_TAKEN
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

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

                if (File(path).exists()) {
                    images.add(ImageItem(id, name, path, size, dateTaken))
                }
            }
        }

        return images
    }

    private fun groupImagesByDate(images: List<ImageItem>): List<ImageGroup> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val groupedMap = images.groupBy { image ->
            val date = if (image.dateTaken > 0) Date(image.dateTaken) else Date(File(image.path).lastModified())
            dateFormat.format(date)
        }

        return groupedMap.map { (date, images) ->
            ImageGroup(date, images.toMutableList())
        }.sortedByDescending { it.date }
    }

    private fun toggleSelectAll() {
        val allSelected = imageGroups.all { group ->
            group.images.all { it.isSelected }
        }

        imageGroups.forEach { group ->
            group.images.forEach { image ->
                image.isSelected = !allSelected
            }
        }

        imageGroupAdapter.notifyDataSetChanged()
        updateSelectAllIcon()
        updateSelectedSize()
    }

    private fun updateSelectAllIcon() {
        val allSelected = imageGroups.all { group ->
            group.images.all { it.isSelected }
        }

        binding.cbSelectAllGlobal.setImageResource(
            if (allSelected) R.drawable.check_yuan_2 else R.drawable.discheck_yuan
        )
    }

    private fun updateSelectedSize() {
        val totalSize = imageGroups.sumOf { group ->
            group.images.filter { it.isSelected }.sumOf { it.size }
        }

        val (size, unit) = formatFileSize(totalSize)
        binding.tvScannedSize.text = size
        binding.tvScannedSizeUn.text = unit

        updateSelectAllIcon()
    }

    private fun formatFileSize(bytes: Long): Pair<String, String> {
        return when {
            bytes < 1024 -> Pair(bytes.toString(), "B")
            bytes < 1024 * 1024 -> Pair(String.format("%.1f", bytes / 1024.0), "KB")
            bytes < 1024 * 1024 * 1024 -> Pair(String.format("%.1f", bytes / (1024.0 * 1024.0)), "MB")
            else -> Pair(String.format("%.1f", bytes / (1024.0 * 1024.0 * 1024.0)), "GB")
        }
    }

    private fun showDeleteConfirmDialog() {
        val selectedImages = imageGroups.flatMap { group ->
            group.images.filter { it.isSelected }
        }

        if (selectedImages.isEmpty()) {
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Delete confirmation")
            .setMessage("Are you sure you want to delete the selected ${selectedImages.size} images?")
            .setPositiveButton("Delete") { _, _ ->
                deleteSelectedImages(selectedImages)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteSelectedImages(selectedImages: List<ImageItem>) {
        var deletedCount = 0

        selectedImages.forEach { image ->
            try {
                val file = File(image.path)
                if (file.exists() && file.delete()) {
                    contentResolver.delete(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        "${MediaStore.Images.Media._ID} = ?",
                        arrayOf(image.id.toString())
                    )
                    deletedCount++
                }
            } catch (e: Exception) {
                Log.e("TssZp", "Failed to delete image: ${image.path}", e)
            }
        }
        val totalSize = imageGroups.sumOf { group ->
            group.images.filter { it.isSelected }.sumOf { it.size }
        }
        val intent = Intent(this, TssJs::class.java)
        intent.putExtra("CLEANED_SIZE", totalSize)
        intent.putExtra("jump_type", "image")
        startActivity(intent)
        finish()

    }
}

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