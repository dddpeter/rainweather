package com.dddpeter.app.rainweather.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dddpeter.app.rainweather.data.models.LifeIndex
import com.dddpeter.app.rainweather.databinding.ItemLifeIndexBinding

/**
 * 生活指数适配器
 */
class LifeIndexAdapter : ListAdapter<LifeIndex, LifeIndexAdapter.ViewHolder>(LifeIndexDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLifeIndexBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ViewHolder(private val binding: ItemLifeIndexBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(lifeIndex: LifeIndex) {
            binding.tvIcon.text = lifeIndex.getIcon()
            binding.tvName.text = lifeIndex.name
            binding.tvLevel.text = lifeIndex.level ?: "未知"
            binding.tvDetail.text = lifeIndex.detail ?: ""
        }
    }
    
    private class LifeIndexDiffCallback : DiffUtil.ItemCallback<LifeIndex>() {
        override fun areItemsTheSame(oldItem: LifeIndex, newItem: LifeIndex): Boolean {
            return oldItem.name == newItem.name
        }
        
        override fun areContentsTheSame(oldItem: LifeIndex, newItem: LifeIndex): Boolean {
            return oldItem == newItem
        }
    }
}
