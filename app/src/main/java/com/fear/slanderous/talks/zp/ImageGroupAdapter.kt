package com.fear.slanderous.talks.zp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fear.slanderous.talks.R

/**
 * 图片分组Adapter - 重构后支持MVP模式
 */
class ImageGroupAdapter(
    private var imageGroups: List<ImageGroup>,
    private val onSelectionChanged: () -> Unit,
    private val onGroupSelectAll: (Int) -> Unit
) : RecyclerView.Adapter<ImageGroupAdapter.ViewHolder>() {

    /**
     * 更新数据
     */
    fun updateData(newGroups: List<ImageGroup>) {
        imageGroups = newGroups
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.tss_picture_group, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imageGroups[position], position)
    }

    override fun getItemCount() = imageGroups.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        private val cbSelectAll: ImageView = itemView.findViewById(R.id.cb_select_all)
        private val rvPictures: RecyclerView = itemView.findViewById(R.id.rv_pictures)

        fun bind(imageGroup: ImageGroup, groupPosition: Int) {
            tvDate.text = imageGroup.date

            val allSelected = imageGroup.images.isNotEmpty() &&
                    imageGroup.images.all { it.isSelected }

            cbSelectAll.setImageResource(
                if (allSelected) R.drawable.check_yuan_2 else R.drawable.discheck_yuan
            )

            cbSelectAll.setOnClickListener {
                onGroupSelectAll(groupPosition)
            }

            val imageAdapter = ImageAdapter(
                images = imageGroup.images,
                onSelectionChanged = onSelectionChanged
            )

            rvPictures.apply {
                layoutManager = GridLayoutManager(itemView.context, 3)
                adapter = imageAdapter
            }
        }
    }
}

