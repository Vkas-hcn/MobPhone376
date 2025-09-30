package com.fear.slanderous.talks.sm


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fear.slanderous.talks.R
import com.fear.slanderous.talks.databinding.ItemCategoryBinding
import com.fear.slanderous.talks.databinding.ItemFileBinding

class CategoryAdapter(
    private val onCategoryClick: (JunkCategory) -> Unit,
    private val onCategorySelectClick: (JunkCategory) -> Unit,
    private val onFileSelectClick: (JunkFile, JunkCategory) -> Unit
) : ListAdapter<JunkCategory, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val fileAdapter by lazy {
            FileAdapter { file ->
                val category = getItem(adapterPosition)
                onFileSelectClick(file, category)
            }
        }

        init {
            binding.rvItemFile.apply {
                layoutManager = LinearLayoutManager(binding.root.context)
                adapter = fileAdapter
            }

            binding.llCategory.setOnClickListener {
                val category = getItem(adapterPosition)
                onCategoryClick(category)
            }

            binding.imgSelect.setOnClickListener {
                val category = getItem(adapterPosition)
                onCategorySelectClick(category)
            }
        }

        fun bind(category: JunkCategory) {
            // 手动设置数据，不使用data binding的自动绑定
            binding.apply {
                // 更新分类图标
                imgSelect.setImageResource(
                    if (category.isSelected) R.drawable.cheak_icon else R.drawable.discheak_icon
                )

                // 更新展开/折叠图标
                imgInstruct.setImageResource(
                    if (category.isExpanded) R.drawable.shang_icon else R.drawable.xia_icon
                )

                // 更新文件列表显示状态
                rvItemFile.visibility = if (category.isExpanded) View.VISIBLE else View.GONE

                // 设置分类名称
                tvTitle.text = category.name

                // 设置分类大小
                tvSize.text = category.formattedTotalSize

                // 更新文件列表
                if (category.isExpanded) {
                    fileAdapter.submitList(category.files.toList()) {
                        rvItemFile.visibility = View.VISIBLE
                        fileAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }
}

// CategoryDiffCallback 保持不变
class CategoryDiffCallback : DiffUtil.ItemCallback<JunkCategory>() {
    override fun areItemsTheSame(oldItem: JunkCategory, newItem: JunkCategory): Boolean {
        return oldItem.type == newItem.type
    }

    override fun areContentsTheSame(oldItem: JunkCategory, newItem: JunkCategory): Boolean {
        return oldItem == newItem &&
                oldItem.files.size == newItem.files.size &&
                oldItem.isExpanded == newItem.isExpanded &&
                oldItem.isSelected == newItem.isSelected
    }
}

// FileAdapter.kt - MVP版本
class FileAdapter(
    private val onFileClick: (JunkFile) -> Unit
) : ListAdapter<JunkFile, FileAdapter.FileViewHolder>(FileDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val file = getItem(position)
            holder.updateSelectionState(file)
        }
    }

    inner class FileViewHolder(
        private val binding: ItemFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val file = getItem(adapterPosition)
                    onFileClick(file)
                }
            }

            binding.imgFileSelect.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val file = getItem(adapterPosition)
                    onFileClick(file)
                }
            }
        }

        fun bind(file: JunkFile) {
            // 手动设置文件名
            binding.tvFileName.text = file.name

            // 更新选择状态
            updateSelectionState(file)
        }

        fun updateSelectionState(file: JunkFile) {
            binding.imgFileSelect.setImageResource(
                if (file.isSelected) R.drawable.cheak_icon else R.drawable.discheak_icon
            )
        }
    }
}

// FileDiffCallback 保持不变
class FileDiffCallback : DiffUtil.ItemCallback<JunkFile>() {
    override fun areItemsTheSame(oldItem: JunkFile, newItem: JunkFile): Boolean {
        return oldItem.path == newItem.path
    }

    override fun areContentsTheSame(oldItem: JunkFile, newItem: JunkFile): Boolean {
        return oldItem == newItem && oldItem.isSelected == newItem.isSelected
    }

    override fun getChangePayload(oldItem: JunkFile, newItem: JunkFile): Any? {
        return if (oldItem.isSelected != newItem.isSelected) {
            "selection_changed"
        } else {
            null
        }
    }
}