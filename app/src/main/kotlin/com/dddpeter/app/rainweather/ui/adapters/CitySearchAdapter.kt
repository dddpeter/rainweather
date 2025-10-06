package com.dddpeter.app.rainweather.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dddpeter.app.rainweather.data.models.CityInfo
import com.dddpeter.app.rainweather.databinding.ItemCitySearchResultBinding

/**
 * 城市搜索结果适配器
 */
class CitySearchAdapter(
    private val onCityClick: (CityInfo) -> Unit
) : ListAdapter<CityInfo, CitySearchAdapter.ViewHolder>(CityInfoDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCitySearchResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(private val binding: ItemCitySearchResultBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCityClick(getItem(position))
                }
            }
        }
        
        fun bind(cityInfo: CityInfo) {
            binding.tvCityName.text = cityInfo.getDisplayName()
        }
    }
    
    private class CityInfoDiffCallback : DiffUtil.ItemCallback<CityInfo>() {
        override fun areItemsTheSame(oldItem: CityInfo, newItem: CityInfo): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: CityInfo, newItem: CityInfo): Boolean {
            return oldItem == newItem
        }
    }
}

