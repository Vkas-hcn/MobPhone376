package com.fear.slanderous.talks.dwj


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fear.slanderous.talks.R
import com.fear.slanderous.talks.databinding.ItemFileCleanBinding
import java.io.File

class FileAdapter(
    private val files: MutableList<FileItem>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    inner class FileViewHolder(var binding: ItemFileCleanBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(file: FileItem, position: Int) {
            binding.tvFileName.text = file.name
            binding.tvFileSize.text = formatFileSize(file.size)
            binding.ivSelectStatus.setImageResource(
                if (file.isSelected) {
                    R.drawable.cheak_icon
                } else {
                    R.drawable.discheak_icon
                }
            )
            binding.root.setOnClickListener {
                onItemClick(position)
            }
            binding.ivSelectStatus.setOnClickListener {
                onItemClick(position)
            }
            when (file.type) {
                FileType.Image -> {
                    loadImageThumbnail(file.path)
                }

                FileType.Video -> {
                    loadVideoThumbnail(file.path)
                }

                else -> {
                    Glide.with(this.itemView.context.applicationContext).clear(binding.ivFileIcon)
                    binding.ivFileIcon.setImageResource(getFileIcon(file.type))
                }
            }
        }

        private fun loadImageThumbnail(imagePath: String) {
            val requestOptions = RequestOptions()
                .override(200, 200)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .placeholder(R.drawable.kong_data)
                .error(R.drawable.kong_data)

            Glide.with(this.itemView.context.applicationContext)
                .load(File(imagePath))
                .apply(requestOptions)
                .into(binding.ivFileIcon)
        }

        private fun loadVideoThumbnail(videoPath: String) {
            val requestOptions = RequestOptions()
                .override(200, 200)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .placeholder(R.drawable.kong_data)
                .error(R.drawable.kong_data)

            Glide.with(this.itemView.context.applicationContext)
                .load(File(videoPath))
                .apply(requestOptions)
                .into(binding.ivFileIcon)
        }

        @SuppressLint("DefaultLocale")
        private fun formatFileSize(size: Long): String {
            return when {
                size >= 1024 * 1024 * 1024 -> String.format(
                    "%.1f GB",
                    size / (1024.0 * 1024.0 * 1024.0)
                )

                size >= 1024 * 1024 -> String.format("%.1f MB", size / (1024.0 * 1024.0))
                else -> String.format("%.1f KB", size / 1024.0)
            }
        }

        private fun getFileIcon(type: FileType): Int {
            return when(type){
                FileType.Image -> R.drawable.pic_icon
                FileType.Video -> R.drawable.video_icon
                FileType.Audio -> R.drawable.audio_icon
                FileType.Docs -> R.drawable.word_icon
                FileType.Download -> R.drawable.apk_icon
                FileType.Zip -> R.drawable.down_icon
                else -> R.drawable.down_icon
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileCleanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position], position)
    }

    override fun getItemCount(): Int = files.size

    override fun onViewRecycled(holder: FileViewHolder) {
        super.onViewRecycled(holder)
        Glide.with(holder.itemView.context.applicationContext).clear(holder.binding.ivFileIcon)
    }
}