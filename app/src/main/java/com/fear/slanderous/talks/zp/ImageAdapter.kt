package com.fear.slanderous.talks.zp


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fear.slanderous.talks.R
import com.google.android.material.button.MaterialButton
import java.io.File

class ImageAdapter(
    private val images: List<ImageItem>,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.tss_picture, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount() = images.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPicture: ImageView = itemView.findViewById(R.id.iv_picture)
        private val cbSelect: ImageView = itemView.findViewById(R.id.cb_select)
        private val tvSize: MaterialButton = itemView.findViewById(R.id.tv_size)

        fun bind(image: ImageItem) {
            // 加载图片
            Glide.with(itemView.context.applicationContext)
                .load(File(image.path))
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .centerCrop()
                .into(ivPicture)

            // 更新选中状态
            updateSelectionUI(image.isSelected)

            // 显示文件大小
            tvSize.text = formatFileSize(image.size)

            // 点击事件
            val clickListener = View.OnClickListener {
                image.isSelected = !image.isSelected
                updateSelectionUI(image.isSelected)
                onSelectionChanged()
            }

            itemView.setOnClickListener(clickListener)
            cbSelect.setOnClickListener(clickListener)
        }

        private fun updateSelectionUI(isSelected: Boolean) {
            cbSelect.setImageResource(
                if (isSelected) R.drawable.check_yuan else R.drawable.discheck_yuan
            )
        }

        private fun formatFileSize(bytes: Long): String {
            return when {
                bytes < 1024 -> "${bytes}B"
                bytes < 1024 * 1024 -> "${String.format("%.1f", bytes / 1024.0)}KB"
                bytes < 1024 * 1024 * 1024 -> "${String.format("%.1f", bytes / (1024.0 * 1024.0))}MB"
                else -> "${String.format("%.1f", bytes / (1024.0 * 1024.0 * 1024.0))}GB"
            }
        }
    }
}